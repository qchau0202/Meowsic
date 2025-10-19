package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.managers.PlaybackManager;
import vn.edu.tdtu.lhqc.meowsic.managers.RefreshManager;
import vn.edu.tdtu.lhqc.meowsic.managers.SongStore;
import vn.edu.tdtu.lhqc.meowsic.models.Song;

// Display now playing screen with song information and playback controls
public class NowPlayingFragment extends Fragment {

    // Interface for communicating with the activity
    public interface NowPlayingListener {
        void setFullscreenMode(boolean isFullscreen);
        void updateMinimizedPlayer(String songTitle, String artistName);
    }

    // UI Components
    private ImageView btnMinimize, btnMenu, btnFavorite, btnPrevious, btnPlayPause, btnNext;
    private ImageView btnShuffle, btnRepeat;
    private ImageView albumArtImage;
    private TextView songTitle, artistName;
    private TextView currentTimeText, totalTimeText;
    private TextView currentSpeedText;
    private SeekBar progressBar;
    private View upNextSection;

    // Song information
    private String currentSongTitle = "Song";
    private String currentArtistName = "Title";
    private String currentSongUriString = null;
    private String currentAlbumArtBase64 = null;
    private PlaybackManager.Listener playbackListener;
    private boolean isUserSeeking = false;
    
    // Listener for activity communication
    private NowPlayingListener listener;

    public NowPlayingFragment() {
        // empty constructor
    }

    public static NowPlayingFragment newInstance(String songTitle, String artistName) {
        NowPlayingFragment fragment = new NowPlayingFragment();
        Bundle args = new Bundle();
        args.putString("song_title", songTitle);
        args.putString("artist_name", artistName);
        fragment.setArguments(args);
        return fragment;
    }

    public static NowPlayingFragment newInstance(String songTitle, String artistName, String songUriString) {
        NowPlayingFragment fragment = new NowPlayingFragment();
        Bundle args = new Bundle();
        args.putString("song_title", songTitle);
        args.putString("artist_name", artistName);
        args.putString("song_uri", songUriString);
        fragment.setArguments(args);
        return fragment;
    }
    
    public static NowPlayingFragment newInstance(String songTitle, String artistName, String songUriString, String albumArtBase64) {
        NowPlayingFragment fragment = new NowPlayingFragment();
        Bundle args = new Bundle();
        args.putString("song_title", songTitle);
        args.putString("artist_name", artistName);
        args.putString("song_uri", songUriString);
        args.putString("album_art", albumArtBase64);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSongTitle = getArguments().getString("song_title", "Song");
            currentArtistName = getArguments().getString("artist_name", "Artist");
            currentSongUriString = getArguments().getString("song_uri", null);
            currentAlbumArtBase64 = getArguments().getString("album_art", null);
        }
    }

    @Override
    public void onAttach(android.content.Context context) {
        super.onAttach(context);
        if (context instanceof NowPlayingListener) {
            listener = (NowPlayingListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_now_playing, container, false);
        
        initializeViews(view);
        setupClickListeners();
        updateSongInfo();
        
        // Start playback via PlaybackManager only if it's a new song
        PlaybackManager pm = PlaybackManager.get();
        if (currentSongUriString != null) {
            Uri newUri = Uri.parse(currentSongUriString);
            Uri currentUri = pm.getCurrentUri();
            // Only start playback if it's a different song or nothing is playing
            if (currentUri == null || !currentUri.equals(newUri)) {
                try { 
                    pm.play(requireContext(), newUri, currentSongTitle, currentArtistName); 
                } catch (Exception ignored) {}
            }
        }
        attachPlaybackListener();
        
        // Enable fullscreen mode when fragment is created
        if (listener != null) {
            listener.setFullscreenMode(true);
            listener.updateMinimizedPlayer(currentSongTitle, currentArtistName);
        }
        
        return view;
    }

    private void initializeViews(View view) {
        // Header buttons
        btnMinimize = view.findViewById(R.id.btn_minimize);
        btnMenu = view.findViewById(R.id.btn_menu);
         btnFavorite = view.findViewById(R.id.btn_favorite);
        
        // Album art
        albumArtImage = view.findViewById(R.id.album_art);
        
        // Song information
        songTitle = view.findViewById(R.id.song_title);
        artistName = view.findViewById(R.id.artist_name);

        // Progress
        progressBar = view.findViewById(R.id.progress_bar);
        currentTimeText = view.findViewById(R.id.current_time);
        totalTimeText = view.findViewById(R.id.total_time);
        
        // Playback controls
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btnNext = view.findViewById(R.id.btn_next);
        btnShuffle = view.findViewById(R.id.btn_shuffle);
        btnRepeat = view.findViewById(R.id.btn_repeat);
        
        // Up next section
        upNextSection = view.findViewById(R.id.up_next_section);
        
        // Speed display
        currentSpeedText = view.findViewById(R.id.current_speed_text);
        
        // Set up progress bar (placeholder values)
        progressBar.setMax(100);
        progressBar.setProgress(0);
        
        // Load album art
        loadAlbumArt();
        
        // Initialize button states
        updateShuffleButton();
        updateRepeatButton();
        updateSpeedDisplay();
    }
    
    private void loadAlbumArt() {
        if (albumArtImage == null) return;
        
        // Try to get album art from current song
        if (currentAlbumArtBase64 != null && !currentAlbumArtBase64.isEmpty()) {
            try {
                byte[] decodedBytes = android.util.Base64.decode(currentAlbumArtBase64, android.util.Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                albumArtImage.setImageBitmap(bitmap);
                return;
            } catch (Exception ignored) {}
        }
        
        // If no album art from arguments, try to load from library
        if (currentSongUriString != null && getContext() != null) {
            java.util.List<Song> allSongs = SongStore.load(getContext());
            for (Song song : allSongs) {
                if (song.getUriString() != null && song.getUriString().equals(currentSongUriString)) {
                    if (song.hasAlbumArt()) {
                        try {
                            byte[] decodedBytes = android.util.Base64.decode(song.getAlbumArtBase64(), android.util.Base64.DEFAULT);
                            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            albumArtImage.setImageBitmap(bitmap);
                            return;
                        } catch (Exception ignored) {}
                    }
                    break;
                }
            }
        }
        
        // Fallback to default image
        albumArtImage.setImageResource(R.drawable.billie_eilish);
    }
    
    /**
     * Update album art for the currently playing song
     */
    private void updateAlbumArtForCurrentSong() {
        if (albumArtImage == null || getContext() == null) return;
        
        PlaybackManager pm = PlaybackManager.get();
        Uri currentUri = pm.getCurrentUri();
        
        if (currentUri == null) {
            // No song playing, use default image
            albumArtImage.setImageResource(R.drawable.billie_eilish);
            return;
        }
        
        // Find the current song in the library and load its album art
        java.util.List<Song> allSongs = SongStore.load(getContext());
        for (Song song : allSongs) {
            if (song.getUriString() != null && song.getUriString().equals(currentUri.toString())) {
                if (song.hasAlbumArt()) {
                    try {
                        byte[] decodedBytes = android.util.Base64.decode(song.getAlbumArtBase64(), android.util.Base64.DEFAULT);
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        albumArtImage.setImageBitmap(bitmap);
                        return;
                    } catch (Exception ignored) {}
                }
                break;
            }
        }
        
        // Fallback to default image if no album art found
        albumArtImage.setImageResource(R.drawable.billie_eilish);
    }

    private void setupClickListeners() {
        // Minimize button
        btnMinimize.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Exit fullscreen mode and update minimized player before navigating back
                if (listener != null) {
                    listener.updateMinimizedPlayer(currentSongTitle, currentArtistName);
                    listener.setFullscreenMode(false);
                }
                // Use fragment manager to handle back navigation properly
                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    getActivity().onBackPressed();
                }
            }
        });

        // Menu button
        btnMenu.setOnClickListener(v -> showPopupMenu(v));

        // Favorite button
        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> toggleFavorite());
        }

        // Previous button
        btnPrevious.setOnClickListener(v -> {
            PlaybackManager.get().playPrevious();
        });

        // Play/Pause button
        btnPlayPause.setOnClickListener(v -> {
            PlaybackManager.get().togglePlayPause();
        });

        // Next button
        btnNext.setOnClickListener(v -> {
            PlaybackManager.get().playNext();
        });

        // Shuffle button
        if (btnShuffle != null) {
            btnShuffle.setOnClickListener(v -> {
                PlaybackManager.get().toggleShuffle();
                updateShuffleButton();
            });
        }

        // Repeat button
        if (btnRepeat != null) {
            btnRepeat.setOnClickListener(v -> {
                PlaybackManager.get().toggleRepeat();
                updateRepeatButton();
            });
        }

        // Up next section - navigate to queue
        if (upNextSection != null) {
            upNextSection.setOnClickListener(v -> {
                if (getActivity() == null) return;
                android.content.Intent intent = new android.content.Intent(getActivity(), vn.edu.tdtu.lhqc.meowsic.activities.QueueActivity.class);
                
                // Add slide animation from bottom to top
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_up_from_bottom, R.anim.slide_down_to_top);
            });
        }

        // Progress bar
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    PlaybackManager pm = PlaybackManager.get();
                    int dur = pm.getDuration();
                    if (dur > 0) {
                        int pos = (int)(dur * (progress / 100f));
                        // Update time display while dragging
                        updateCurrentTime(pos);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                // Seek to the new position when user releases
                PlaybackManager pm = PlaybackManager.get();
                int dur = pm.getDuration();
                if (dur > 0) {
                    int pos = (int)(dur * (seekBar.getProgress() / 100f));
                    pm.seekTo(pos);
                }
            }
        });
    }

    private void updateSongInfo() {
        songTitle.setText(currentSongTitle);
        artistName.setText(currentArtistName);
        updateFavoriteButton();
    }

    private void setPlayPauseIcon(boolean playing) {
        if (btnPlayPause == null) return;
        btnPlayPause.setImageResource(playing ? R.drawable.ic_pause_circle_24px : R.drawable.ic_play_circle_24px);
    }

    private void attachPlaybackListener() {
        PlaybackManager pm = PlaybackManager.get();
        
        // Set initial state based on actual playback state
        setPlayPauseIcon(pm.isPlaying());
        updateCurrentTime(pm.getPosition());
        if (totalTimeText != null) totalTimeText.setText(formatTime(pm.getDuration()));
        
        playbackListener = new PlaybackManager.Listener() {
            @Override public void onStateChanged(boolean isPlaying) { setPlayPauseIcon(isPlaying); }
            @Override public void onProgress(int positionMs, int durationMs) {
                if (!isUserSeeking) {
                    updateCurrentTime(positionMs);
                    if (durationMs > 0) progressBar.setProgress((int)(positionMs * 100f / durationMs));
                }
                if (totalTimeText != null) totalTimeText.setText(formatTime(durationMs));
            }
            @Override public void onMetadataChanged(String title, String artist) {
                songTitle.setText(title);
                artistName.setText(artist);
                currentSongTitle = title;
                currentArtistName = artist;
                updateFavoriteButton();
                
                // Update album art when song changes
                updateAlbumArtForCurrentSong();
            }
            @Override public void onSongCompleted() {
                // Auto-next handled by PlaybackManager
            }
        };
        pm.addListener(playbackListener);
    }

    private void updateCurrentTime(int millis) {
        if (currentTimeText != null) currentTimeText.setText(formatTime(millis));
    }

    private String formatTime(int millis) {
        int totalSec = Math.max(0, millis / 1000);
        int m = totalSec / 60;
        int s = totalSec % 60;
        return String.format(java.util.Locale.getDefault(), "%d:%02d", m, s);
    }
    

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) listener.setFullscreenMode(false);
        if (playbackListener != null) PlaybackManager.get().removeListener(playbackListener);
    }

    private void showPopupMenu(View anchor) {
        // Create the dialog
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_now_playing_menu);
        
        // Make dialog appear in center and have proper dimensions
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
            (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.85f),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        );
        
        // Get views
        LinearLayout optionAddToPlaylist = dialog.findViewById(R.id.option_add_to_playlist);
        LinearLayout optionPlaybackSpeed = dialog.findViewById(R.id.option_playback_speed);

        // Set up click listeners
        if (optionAddToPlaylist != null) {
            optionAddToPlaylist.setOnClickListener(v -> {
                dialog.dismiss();
                showPlaylistSelectionDialog();
            });
        }
        
        if (optionPlaybackSpeed != null) {
            optionPlaybackSpeed.setOnClickListener(v -> {
                dialog.dismiss();
                showPlaybackSpeedDialog();
            });
        }
        
        // Show dialog
        dialog.show();
    }
    
    private void showPlaybackSpeedDialog() {
        // Create the playback speed dialog
        Dialog speedDialog = new Dialog(requireContext());
        speedDialog.setContentView(R.layout.dialog_playback_speed);
        
        // Make dialog appear in center and have proper dimensions
        speedDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        speedDialog.getWindow().setLayout(
            (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.85f),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        );
        
        // Get views
        TextView speedValueText = speedDialog.findViewById(R.id.speed_value_text);
        SeekBar speedSlider = speedDialog.findViewById(R.id.speed_slider);
        com.google.android.material.button.MaterialButton speed05x = speedDialog.findViewById(R.id.speed_0_5x);
        com.google.android.material.button.MaterialButton speed1x = speedDialog.findViewById(R.id.speed_1x);
        com.google.android.material.button.MaterialButton speed15x = speedDialog.findViewById(R.id.speed_1_5x);
        com.google.android.material.button.MaterialButton speed2x = speedDialog.findViewById(R.id.speed_2x);
        com.google.android.material.button.MaterialButton btnClose = speedDialog.findViewById(R.id.btn_close_speed);
        
        // Get current playback speed and set initial values
        float currentSpeed = PlaybackManager.get().getPlaybackSpeed();
        int currentProgress = (int) ((currentSpeed - 0.25f) / 0.25f);
        currentProgress = Math.max(0, Math.min(7, currentProgress));
        
        // Set initial slider position
        if (speedSlider != null) {
            speedSlider.setProgress(currentProgress);
        }
        
        // Update speed value text initially
        if (speedValueText != null) {
            speedValueText.setText(String.format(java.util.Locale.getDefault(), "%.2fx", currentSpeed));
        }
        
        // Setup slider listener
        if (speedSlider != null) {
            speedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float speed = 0.25f + (progress * 0.25f);
                    if (speedValueText != null) {
                        speedValueText.setText(String.format(java.util.Locale.getDefault(), "%.2fx", speed));
                    }
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Apply speed to playback
                    float speed = 0.25f + (seekBar.getProgress() * 0.25f);
                    PlaybackManager.get().setPlaybackSpeed(speed);
                    updateSpeedDisplay();
                }
            });
        }
        
        // Setup preset button listeners
        if (speed05x != null) {
            speed05x.setOnClickListener(v -> {
                if (speedSlider != null) speedSlider.setProgress(1);
                PlaybackManager.get().setPlaybackSpeed(0.5f);
                updateSpeedDisplay();
            });
        }
        
        if (speed1x != null) {
            speed1x.setOnClickListener(v -> {
                if (speedSlider != null) speedSlider.setProgress(3);
                PlaybackManager.get().setPlaybackSpeed(1.0f);
                updateSpeedDisplay();
            });
        }
        
        if (speed15x != null) {
            speed15x.setOnClickListener(v -> {
                if (speedSlider != null) speedSlider.setProgress(5); // 1.5x = 0.25 + (5 * 0.25)
                PlaybackManager.get().setPlaybackSpeed(1.5f);
                updateSpeedDisplay();
            });
        }
        
        if (speed2x != null) {
            speed2x.setOnClickListener(v -> {
                if (speedSlider != null) speedSlider.setProgress(7); // 2.0x = 0.25 + (7 * 0.25)
                PlaybackManager.get().setPlaybackSpeed(2.0f);
                updateSpeedDisplay();
            });
        }
        
        // Setup close button
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> speedDialog.dismiss());
        }
        
        // Show dialog
        speedDialog.show();
    }
    
    /**
     * Update shuffle button appearance based on current state
     */
    private void updateShuffleButton() {
        if (btnShuffle == null) return;
        
        PlaybackManager pm = PlaybackManager.get();
        boolean isShuffleOn = pm.isShuffleMode();
        
        // Change tint color to indicate active state
        if (isShuffleOn) {
            btnShuffle.setColorFilter(getResources().getColor(R.color.primary_pink, null));
        } else {
            btnShuffle.setColorFilter(getResources().getColor(R.color.secondary_grey, null));
        }
    }
    
    /**
     * Update repeat button appearance based on current state
     */
    private void updateRepeatButton() {
        if (btnRepeat == null) return;
        
        PlaybackManager pm = PlaybackManager.get();
        boolean isRepeatOn = pm.isRepeatMode();
        
        // Change tint color to indicate active state
        if (isRepeatOn) {
            btnRepeat.setColorFilter(getResources().getColor(R.color.primary_pink, null));
        } else {
            btnRepeat.setColorFilter(getResources().getColor(R.color.secondary_grey, null));
        }
    }
    
    /**
     * Update speed display based on current playback speed
     */
    private void updateSpeedDisplay() {
        if (currentSpeedText != null) {
            PlaybackManager pm = PlaybackManager.get();
            float speed = pm.getPlaybackSpeed();
            if (speed != 1.0f) {
                currentSpeedText.setText(String.format(java.util.Locale.getDefault(), "%.1fx", speed));
                currentSpeedText.setVisibility(View.VISIBLE);
            } else {
                currentSpeedText.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Show playlist selection dialog for adding current song to a playlist
     */
    private void showPlaylistSelectionDialog() {
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_playlist);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setGravity(android.view.Gravity.CENTER);

        LinearLayout playlistContainer = dialog.findViewById(R.id.playlist_container);
        TextView noPlaylistsText = dialog.findViewById(R.id.no_playlists_text);

        // Get current song information
        PlaybackManager pm = PlaybackManager.get();
        String currentTitle = pm.getCurrentTitle();
        String currentArtist = pm.getCurrentArtist();
        android.net.Uri currentUri = pm.getCurrentUri();
        
        if (currentTitle == null || currentTitle.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "No song is currently playing", android.widget.Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Get all playlists
        java.util.List<String> playlists = vn.edu.tdtu.lhqc.meowsic.managers.PlaylistStore.getAllPlaylistNames(requireContext());

        if (playlists.isEmpty()) {
            if (noPlaylistsText != null) {
                noPlaylistsText.setVisibility(android.view.View.VISIBLE);
                noPlaylistsText.setText("No playlists available.\nCreate one in the Library.");
            }
        } else {
            if (playlistContainer != null) {
                for (String playlistName : playlists) {
                    // Inflate playlist option layout
                    View playlistOptionView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_playlist_option, playlistContainer, false);
                    
                    // Set playlist name
                    TextView playlistNameText = playlistOptionView.findViewById(R.id.playlist_name);
                    if (playlistNameText != null) {
                        playlistNameText.setText(playlistName);
                    }
                    
                    // Set click listener
                    playlistOptionView.setOnClickListener(v -> {
                        addCurrentSongToPlaylist(playlistName);
                        dialog.dismiss();
                    });
                    
                    playlistContainer.addView(playlistOptionView);
                }
            }
        }

        dialog.show();
    }
    
    /**
     * Add the current playing song to a playlist
     */
    private void addCurrentSongToPlaylist(String playlistName) {
        PlaybackManager pm = PlaybackManager.get();
        String currentTitle = pm.getCurrentTitle();
        String currentArtist = pm.getCurrentArtist();
        android.net.Uri currentUri = pm.getCurrentUri();
        
        if (currentTitle == null || currentTitle.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "No song is currently playing", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create song object for the current playing song
        Song currentSong = new Song(
            currentTitle, 
            currentArtist != null ? currentArtist : "Unknown", 
            R.drawable.meowsic_black_icon, 
            currentUri != null ? currentUri.toString() : null, 
            pm.getCurrentAlbumArt() // Get album art from PlaybackManager
        );
        
        // Check if song already exists in the playlist
        java.util.List<Song> existingSongs = vn.edu.tdtu.lhqc.meowsic.managers.PlaylistStore.loadPlaylistSongs(requireContext(), playlistName);
        boolean songExists = false;
        for (Song existingSong : existingSongs) {
            if (existingSong.getUriString() != null && currentUri != null &&
                existingSong.getUriString().equals(currentUri.toString())) {
                songExists = true;
                break;
            }
        }
        
        if (songExists) {
            android.widget.Toast.makeText(requireContext(), 
                "\"" + currentTitle + "\" is already in " + playlistName, 
                android.widget.Toast.LENGTH_SHORT).show();
        } else {
            // Add to playlist
            vn.edu.tdtu.lhqc.meowsic.managers.PlaylistStore.addSongToPlaylist(requireContext(), playlistName, currentSong);
            
            // Notify all listeners that data has changed
            RefreshManager.notifyDataChanged();
            
            android.widget.Toast.makeText(requireContext(), 
                "\"" + currentTitle + "\" added to " + playlistName, 
                android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private void toggleFavorite() {
        if (currentSongUriString == null) return;
        
        boolean isFavorite = vn.edu.tdtu.lhqc.meowsic.managers.FavoriteStore.isFavorite(requireContext(), currentSongUriString);
        
        if (isFavorite) {
            vn.edu.tdtu.lhqc.meowsic.managers.FavoriteStore.removeSong(requireContext(), currentSongUriString);
            android.widget.Toast.makeText(requireContext(), "Removed from favorites", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            Song currentSong = new Song(currentSongTitle, currentArtistName, R.drawable.meowsic_black_icon, currentSongUriString, currentAlbumArtBase64);
            vn.edu.tdtu.lhqc.meowsic.managers.FavoriteStore.addSong(requireContext(), currentSong);
            android.widget.Toast.makeText(requireContext(), "Added to favorites", android.widget.Toast.LENGTH_SHORT).show();
        }
        
        updateFavoriteButton();
        RefreshManager.notifyDataChanged();
    }
    
    private void updateFavoriteButton() {
        if (btnFavorite == null || currentSongUriString == null) return;
        
        boolean isFavorite = vn.edu.tdtu.lhqc.meowsic.managers.FavoriteStore.isFavorite(requireContext(), currentSongUriString);
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled_24px : R.drawable.ic_favorite_24px);
    }
}