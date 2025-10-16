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

// Display now playing screen with song information and playback controls
public class fragment_now_playing extends Fragment {

    // Interface for communicating with the activity
    public interface NowPlayingListener {
        void setFullscreenMode(boolean isFullscreen);
        void updateMinimizedPlayer(String songTitle, String artistName);
    }

    // UI Components
    private ImageView btnMinimize, btnMenu, btnPrevious, btnPlayPause, btnNext;
    private View upNextCard;
    private TextView songTitle, artistName;
    private TextView currentTimeText, totalTimeText;
    private SeekBar progressBar;

    // Song information
    private String currentSongTitle = "Song";
    private String currentArtistName = "Title";
    private String currentSongUriString = null;
    private vn.edu.tdtu.lhqc.meowsic.PlaybackManager.Listener playbackListener;
    private boolean isUserSeeking = false;
    
    // Listener for activity communication
    private NowPlayingListener listener;

    public fragment_now_playing() {
        // empty constructor
    }

    public static fragment_now_playing newInstance(String songTitle, String artistName) {
        fragment_now_playing fragment = new fragment_now_playing();
        Bundle args = new Bundle();
        args.putString("song_title", songTitle);
        args.putString("artist_name", artistName);
        fragment.setArguments(args);
        return fragment;
    }

    public static fragment_now_playing newInstance(String songTitle, String artistName, String songUriString) {
        fragment_now_playing fragment = new fragment_now_playing();
        Bundle args = new Bundle();
        args.putString("song_title", songTitle);
        args.putString("artist_name", artistName);
        args.putString("song_uri", songUriString);
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
        vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
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
        upNextCard = view.findViewById(R.id.up_next_card_root);
        
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
        
        // Set up progress bar (placeholder values)
        progressBar.setMax(100);
        progressBar.setProgress(0);
        
        // Initialize up next info
        updateUpNextInfo(view);
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

        // Previous button
        btnPrevious.setOnClickListener(v -> {
            // TODO: Implement previous song functionality
        });

        // Play/Pause button
        btnPlayPause.setOnClickListener(v -> {
            vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get().togglePlayPause();
        });

        // Next button
        btnNext.setOnClickListener(v -> {
            // TODO: Implement next song functionality
        });


        // Navigate to full queue when user taps Up Next card
        if (upNextCard != null) {
            upNextCard.setOnClickListener(v -> {
                if (getActivity() == null) return;
                android.content.Intent intent = new android.content.Intent(getActivity(), vn.edu.tdtu.lhqc.meowsic.activities.QueueActivity.class);
                startActivity(intent);
            });
        }

        // Progress bar
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
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
                vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
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
    }

    private void setPlayPauseIcon(boolean playing) {
        if (btnPlayPause == null) return;
        btnPlayPause.setImageResource(playing ? R.drawable.ic_pause_circle_24px : R.drawable.ic_play_circle_24px);
    }

    private void attachPlaybackListener() {
        vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
        
        // Set initial state based on actual playback state
        setPlayPauseIcon(pm.isPlaying());
        updateCurrentTime(pm.getPosition());
        if (totalTimeText != null) totalTimeText.setText(formatTime(pm.getDuration()));
        
        playbackListener = new vn.edu.tdtu.lhqc.meowsic.PlaybackManager.Listener() {
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
                // Update up next info when song changes
                if (getView() != null) updateUpNextInfo(getView());
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
    
    private void updateUpNextInfo(View view) {
        if (getContext() == null || view == null) return;
        
        // Get all songs from library
        java.util.List<vn.edu.tdtu.lhqc.meowsic.Song> allSongs = vn.edu.tdtu.lhqc.meowsic.SongStore.load(getContext());
        
        // Find the next song (first song that's not the current one)
        vn.edu.tdtu.lhqc.meowsic.Song nextSong = null;
        for (vn.edu.tdtu.lhqc.meowsic.Song song : allSongs) {
            if (song.getUriString() != null && !song.getTitle().equals(currentSongTitle)) {
                nextSong = song;
                break;
            }
        }
        
        // Update UI with next song info or show empty state
        TextView emptyMessage = view.findViewById(R.id.up_next_empty_message);
        TextView upNextTitle = view.findViewById(R.id.up_next_song_title);
        TextView upNextArtist = view.findViewById(R.id.up_next_song_artist);
        
        if (nextSong != null) {
            if (emptyMessage != null) emptyMessage.setVisibility(View.GONE);
            if (upNextTitle != null) {
                upNextTitle.setVisibility(View.VISIBLE);
                upNextTitle.setText(nextSong.getTitle());
            }
            if (upNextArtist != null) {
                upNextArtist.setVisibility(View.VISIBLE);
                upNextArtist.setText(nextSong.getArtist());
            }
        } else {
            if (emptyMessage != null) emptyMessage.setVisibility(View.VISIBLE);
            if (upNextTitle != null) upNextTitle.setVisibility(View.GONE);
            if (upNextArtist != null) upNextArtist.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) listener.setFullscreenMode(false);
        if (playbackListener != null) vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get().removeListener(playbackListener);
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

        // Set up click listeners
        if (optionAddToPlaylist != null) {
            optionAddToPlaylist.setOnClickListener(v -> {
                // TODO: Implement add to playlist functionality
                dialog.dismiss();
            });
        }
        // Show dialog
        dialog.show();
    }
}