package vn.edu.tdtu.lhqc.meowsic.activities;
import android.graphics.Color;
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
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_home;
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_library;
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_now_playing;
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_profile;

public class HomeActivity extends AppCompatActivity implements fragment_now_playing.NowPlayingListener {
    private LinearLayout navHomeItem, navLibraryItem, navProfileItem;
    private LinearLayout bottomNavigation;
    private LinearLayout minimizedPlayer;
    private int selectedNavItem = R.id.nav_home_item;
    private vn.edu.tdtu.lhqc.meowsic.PlaybackManager.Listener miniPlayerListener;
    
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
                    .replace(R.id.fragment_container, new fragment_home())
                    .commit();
            setSelectedNavItem(R.id.nav_home_item);
        }

        // Set click listeners for navigation items
        navHomeItem.setOnClickListener(v -> onNavigationItemClick(R.id.nav_home_item, new fragment_home()));
        navLibraryItem.setOnClickListener(v -> onNavigationItemClick(R.id.nav_library_item, new fragment_library()));
        navProfileItem.setOnClickListener(v -> onNavigationItemClick(R.id.nav_profile_item, new fragment_profile()));
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
        
        miniPlayerListener = new vn.edu.tdtu.lhqc.meowsic.PlaybackManager.Listener() {
            @Override
            public void onStateChanged(boolean isPlaying) {
                if (miniPlayPause != null) {
                    miniPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause_circle_24px : R.drawable.ic_play_circle_24px);
                }
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
            }
            
            @Override
            public void onSongCompleted() {
                // Auto-next handled by PlaybackManager
            }
        };
        
        vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get().addListener(miniPlayerListener);
        
        // Set initial icon state
        if (miniPlayPause != null) {
            boolean isPlaying = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get().isPlaying();
            miniPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause_circle_24px : R.drawable.ic_play_circle_24px);
        }
        
        // Play/pause button
        if (miniPlayPause != null) {
            miniPlayPause.setOnClickListener(v -> {
                vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get().togglePlayPause();
            });
        }
        
        // Expand to full player when user taps anywhere on the minimized container
        minimizedPlayer.setOnClickListener(v -> {
            vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
            String uriString = pm.getCurrentUri() != null ? pm.getCurrentUri().toString() : null;
            fragment_now_playing nowPlayingFragment = fragment_now_playing.newInstance(
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
    
    private fragment_now_playing getCurrentNowPlayingFragment() {
        // Try to find if there's a now playing fragment in the back stack
        // For now, we'll create a new one - you can optimize this later
        return null;
    }

    @Override
    public void setFullscreenMode(boolean isFullscreen) {
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
                // Show bottom navigation and minimized player for normal mode
                bottomNavigation.setVisibility(View.VISIBLE);
                if (minimizedPlayer != null) {
                    minimizedPlayer.setVisibility(View.VISIBLE);
                }
                
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
}