package vn.edu.tdtu.lhqc.meowsic.activities;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.fragments.HomeFragment;
import vn.edu.tdtu.lhqc.meowsic.fragments.LibraryFragment;
import vn.edu.tdtu.lhqc.meowsic.fragments.NowPlayingFragment;
import vn.edu.tdtu.lhqc.meowsic.fragments.ProfileFragment;
import vn.edu.tdtu.lhqc.meowsic.managers.PlaybackManager;
import vn.edu.tdtu.lhqc.meowsic.managers.SongStore;
import vn.edu.tdtu.lhqc.meowsic.models.Song;

public class HomeActivity extends AppCompatActivity implements NowPlayingFragment.NowPlayingListener {
    private LinearLayout navHomeItem, navLibraryItem, navProfileItem;
    private LinearLayout bottomNavigation;
    private LinearLayout minimizedPlayer;
    private int selectedNavItem = R.id.nav_home_item;
    private PlaybackManager.Listener miniPlayerListener;
    private boolean isFullscreenMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize navigation items
        initNavigationItems();
        
        // Initialize bottom navigation reference
        bottomNavigation = findViewById(R.id.bottom_navigation);
        minimizedPlayer = findViewById(R.id.minimized_player);
        
        // Initialize minimized player click listeners
        setupMinimizedPlayerListeners();

        // load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            setSelectedNavItem(R.id.nav_home_item);
        }

        // Set click listeners for navigation items
        navHomeItem.setOnClickListener(v -> onNavigationItemClick(R.id.nav_home_item, new HomeFragment()));
        navLibraryItem.setOnClickListener(v -> onNavigationItemClick(R.id.nav_library_item, new LibraryFragment()));
        navProfileItem.setOnClickListener(v -> onNavigationItemClick(R.id.nav_profile_item, new ProfileFragment()));
    }
    
    private void initNavigationItems() {
        navHomeItem = findViewById(R.id.nav_home_item);
        navLibraryItem = findViewById(R.id.nav_library_item);
        navProfileItem = findViewById(R.id.nav_profile_item);
        
        // Set up navigation items with proper icons and text
        setupNavItem(navHomeItem, R.drawable.ic_home_24px, "Home");
        setupNavItem(navLibraryItem, R.drawable.ic_library_music_24px, "Library");
        setupNavItem(navProfileItem, R.drawable.ic_profile_24px, "Profile");
    }
    
    private void setupNavItem(LinearLayout navItem, int iconRes, String text) {
        ImageView icon = navItem.findViewById(R.id.nav_icon);
        TextView textView = navItem.findViewById(R.id.nav_text);
        
        icon.setImageResource(iconRes);
        textView.setText(text);
    }
    
    private void onNavigationItemClick(int itemId, Fragment fragment) {
        setSelectedNavItem(itemId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    
    private void setSelectedNavItem(int itemId) {
        // Reset all items to unselected state
        setNavItemState(navHomeItem, false);
        setNavItemState(navLibraryItem, false);
        setNavItemState(navProfileItem, false);
        
        // Set selected item
        selectedNavItem = itemId;
        if (itemId == R.id.nav_home_item) {
            setNavItemState(navHomeItem, true);
        } else if (itemId == R.id.nav_library_item) {
            setNavItemState(navLibraryItem, true);
        } else if (itemId == R.id.nav_profile_item) {
            setNavItemState(navProfileItem, true);
        }
    }
    
    private void setNavItemState(LinearLayout navItem, boolean isSelected) {
        ImageView icon = navItem.findViewById(R.id.nav_icon);
        TextView textView = navItem.findViewById(R.id.nav_text);
        
        if (isSelected) {
            icon.setColorFilter(getResources().getColor(R.color.primary_pink, null));
            textView.setTextColor(getResources().getColor(R.color.primary_pink, null));
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            icon.setColorFilter(getResources().getColor(R.color.primary_grey, null));
            textView.setTextColor(getResources().getColor(R.color.primary_grey, null));
            textView.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    public void onBackPressedDispatcher() {
        // Check if there are fragments in the back stack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // Pop the back stack instead of finishing the activity
            getSupportFragmentManager().popBackStack();
        } else {
            // No fragments in back stack, finish the activity
            super.getOnBackPressedDispatcher();
        }
    }

    private void setupMinimizedPlayerListeners() {
        if (minimizedPlayer == null) return;
        
        // Set up one-time listener for PlaybackManager updates
        final TextView miniSongTitle = minimizedPlayer.findViewById(R.id.mini_song_title);
        final TextView miniCurrentTime = minimizedPlayer.findViewById(R.id.mini_current_time);
        final TextView miniTotalTime = minimizedPlayer.findViewById(R.id.mini_total_time);
        final SeekBar miniProgress = minimizedPlayer.findViewById(R.id.mini_progress);
        final ImageView miniPlayPause = minimizedPlayer.findViewById(R.id.mini_play_pause);
        final ImageView miniAlbumArt = minimizedPlayer.findViewById(R.id.mini_album_art);
        
        miniPlayerListener = new PlaybackManager.Listener() {
            @Override
            public void onStateChanged(boolean isPlaying) {
                if (miniPlayPause != null) {
                    miniPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause_circle_24px : R.drawable.ic_play_circle_24px);
                }
                // Update minimized player visibility based on whether there's a current song
                updateMinimizedPlayerVisibility();
            }
            
            @Override
            public void onProgress(int positionMs, int durationMs) {
                if (miniCurrentTime != null) miniCurrentTime.setText(formatTime(positionMs));
                if (miniTotalTime != null) miniTotalTime.setText(formatTime(durationMs));
                if (miniProgress != null && durationMs > 0) {
                    miniProgress.setProgress((int)(positionMs * 1000f / durationMs));
                }
            }
            
            @Override
            public void onMetadataChanged(String title, String artist) {
                if (miniSongTitle != null) miniSongTitle.setText(title);
                // Load album art when metadata changes
                loadMiniPlayerAlbumArt(miniAlbumArt);
                // Update visibility when metadata changes (song starts/stops)
                updateMinimizedPlayerVisibility();
            }
            
            @Override
            public void onSongCompleted() {
                // Auto-next handled by PlaybackManager
                // Visibility will be updated by onStateChanged or onMetadataChanged
            }
        };
        
        PlaybackManager.get().addListener(miniPlayerListener);
        
        // Load initial album art
        loadMiniPlayerAlbumArt(miniAlbumArt);
        
        // Set initial icon state
        if (miniPlayPause != null) {
            boolean isPlaying = PlaybackManager.get().isPlaying();
            miniPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause_circle_24px : R.drawable.ic_play_circle_24px);
        }
        
        // Set initial visibility state
        updateMinimizedPlayerVisibility();
        
        // Play/pause button
        if (miniPlayPause != null) {
            miniPlayPause.setOnClickListener(v -> {
                PlaybackManager.get().togglePlayPause();
            });
        }
        
        // Expand to full player when user taps anywhere on the minimized container
        minimizedPlayer.setOnClickListener(v -> {
            PlaybackManager pm = PlaybackManager.get();
            String uriString = pm.getCurrentUri() != null ? pm.getCurrentUri().toString() : null;
            NowPlayingFragment nowPlayingFragment = NowPlayingFragment.newInstance(
                pm.getCurrentTitle(), 
                pm.getCurrentArtist(),
                uriString
            );
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, nowPlayingFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
    
    private String formatTime(int ms) {
        int s = Math.max(0, ms / 1000);
        int m = s / 60;
        s %= 60;
        return String.format(java.util.Locale.getDefault(), "%d:%02d", m, s);
    }
    
    private NowPlayingFragment getCurrentNowPlayingFragment() {
        // Try to find if there's a now playing fragment in the back stack
        // For now, we'll create a new one - you can optimize this later
        return null;
    }

    @Override
    public void setFullscreenMode(boolean isFullscreen) {
        this.isFullscreenMode = isFullscreen;
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (bottomNavigation != null && fragmentContainer != null) {
            if (isFullscreen) {
                // Hide bottom navigation and minimized player for fullscreen mode
                bottomNavigation.setVisibility(View.GONE);
                if (minimizedPlayer != null) {
                    minimizedPlayer.setVisibility(View.GONE);
                }
                
                // Update fragment container constraints to fill the entire screen
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fragmentContainer.getLayoutParams();
                if (params != null) {
                    params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                    fragmentContainer.setLayoutParams(params);
                }
            } else {
                // Show bottom navigation for normal mode
                bottomNavigation.setVisibility(View.VISIBLE);
                
                // Update minimized player visibility based on whether there's a current song
                updateMinimizedPlayerVisibility();
                
                // Restore fragment container constraints to stop above minimized player
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fragmentContainer.getLayoutParams();
                if (params != null) {
                    params.bottomToTop = R.id.minimized_player;
                    params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
                    fragmentContainer.setLayoutParams(params);
                }
            }
        }
    }

    @Override
    public void updateMinimizedPlayer(String songTitle, String artistName) {
        // The listener set up in setupMinimizedPlayerListeners() will handle all updates automatically
        // This method is kept for interface compatibility but doesn't need to do anything
        // since PlaybackManager already notifies the miniPlayerListener when metadata changes
    }
    
    /**
     * Updates the minimized player visibility based on whether there's a current song playing
     * and whether the user is in fullscreen mode (now playing)
     */
    private void updateMinimizedPlayerVisibility() {
        if (minimizedPlayer == null) return;
        
        // Don't show minimized player if in fullscreen mode (now playing)
        if (isFullscreenMode) {
            hideMinimizedPlayer();
            return;
        }
        
        PlaybackManager pm = PlaybackManager.get();
        boolean hasCurrentSong = pm.getCurrentUri() != null && 
                                pm.getCurrentTitle() != null && 
                                !pm.getCurrentTitle().isEmpty();
        
        if (hasCurrentSong) {
            showMinimizedPlayer();
        } else {
            hideMinimizedPlayer();
        }
    }
    
    /**
     * Shows the minimized player and updates layout constraints
     */
    private void showMinimizedPlayer() {
        if (minimizedPlayer == null) return;
        
        minimizedPlayer.setVisibility(View.VISIBLE);
        
        // Update fragment container constraints to stop above minimized player
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fragmentContainer.getLayoutParams();
            if (params != null) {
                params.bottomToTop = R.id.minimized_player;
                params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
                fragmentContainer.setLayoutParams(params);
            }
        }
    }
    
    /**
     * Hides the minimized player and updates layout constraints
     */
    private void hideMinimizedPlayer() {
        if (minimizedPlayer == null) return;
        
        minimizedPlayer.setVisibility(View.GONE);
        
        // Update fragment container constraints to extend to bottom navigation
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fragmentContainer.getLayoutParams();
            if (params != null) {
                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                params.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                fragmentContainer.setLayoutParams(params);
            }
        }
    }
    
    private void loadMiniPlayerAlbumArt(ImageView albumArtView) {
        if (albumArtView == null) return;
        
        PlaybackManager pm = PlaybackManager.get();
        android.net.Uri currentUri = pm.getCurrentUri();
        
        if (currentUri == null) {
            albumArtView.setImageResource(R.drawable.billie_eilish);
            return;
        }
        
        // Load album art from library
        java.util.List<Song> allSongs = SongStore.load(this);
        for (Song song : allSongs) {
            if (song.getUriString() != null && song.getUriString().equals(currentUri.toString())) {
                if (song.hasAlbumArt()) {
                    try {
                        byte[] decodedBytes = android.util.Base64.decode(song.getAlbumArtBase64(), android.util.Base64.DEFAULT);
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        albumArtView.setImageBitmap(bitmap);
                        return;
                    } catch (Exception ignored) {}
                }
                break;
            }
        }
        
        // Fallback to default image
        albumArtView.setImageResource(R.drawable.billie_eilish);
    }
}