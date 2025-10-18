package vn.edu.tdtu.lhqc.meowsic.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.Song;
import vn.edu.tdtu.lhqc.meowsic.QueueAdapter;
import android.widget.PopupMenu;

public class QueueActivity extends AppCompatActivity {

    private RecyclerView recyclerViewQueue;
    private QueueAdapter queueAdapter;
    private final List<Song> queueSongs = new ArrayList<>();
    private ImageView playPauseButton;
    private ImageView albumArtView;
    private TextView titleView;
    private TextView emptyStateView;
    private vn.edu.tdtu.lhqc.meowsic.PlaybackManager.Listener playbackListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_queue);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeHeader();
        setupRecycler();
    }

    private void initializeHeader() {
        // Get current playback info from PlaybackManager
        vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
        
        // Initialize views
        albumArtView = findViewById(R.id.np_art);
        titleView = findViewById(R.id.np_title);
        TextView current = findViewById(R.id.np_current);
        TextView total = findViewById(R.id.np_total);
        SeekBar progress = findViewById(R.id.np_progress);
        ImageView back = findViewById(R.id.btn_back_queue);
        playPauseButton = findViewById(R.id.np_play_pause);

        // Show currently playing song info
        if (titleView != null) {
            String currentTitle = pm.getCurrentTitle();
            titleView.setText(currentTitle != null && !currentTitle.isEmpty() ? currentTitle : "No song playing");
        }
        if (current != null) current.setText(formatTime(pm.getPosition()));
        if (total != null) total.setText(formatTime(pm.getDuration()));
        
        // Load album art for currently playing song
        loadCurrentSongAlbumArt();
        
        // Setup seekbar
        if (progress != null) {
            int duration = pm.getDuration();
            if (duration > 0) {
                progress.setMax(1000);
                progress.setProgress((int)(pm.getPosition() * 1000f / duration));
            }
            
            // Add seek listener for user drag
            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                private boolean wasPlaying = false;
                
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        int dur = pm.getDuration();
                        if (dur > 0) {
                            int newPos = (int)(dur * (progress / 1000f));
                            if (current != null) current.setText(formatTime(newPos));
                        }
                    }
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    wasPlaying = pm.isPlaying();
                }
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int dur = pm.getDuration();
                    if (dur > 0) {
                        int newPos = (int)(dur * (seekBar.getProgress() / 1000f));
                        pm.seekTo(newPos);
                    }
                }
            });
        }
        
        // Setup play/pause button
        if (playPauseButton != null) {
            playPauseButton.setImageResource(pm.isPlaying() ? R.drawable.ic_pause_circle_24px : R.drawable.ic_play_circle_24px);
            playPauseButton.setOnClickListener(v -> pm.togglePlayPause());
        }
        
        if (back != null) back.setOnClickListener(v -> finish());
        
        // Update progress continuously and handle metadata changes
        setupPlaybackListener(current, total, progress);
    }
    
    private void setupPlaybackListener(TextView current, TextView total, SeekBar progress) {
        playbackListener = new vn.edu.tdtu.lhqc.meowsic.PlaybackManager.Listener() {
            @Override
            public void onStateChanged(boolean isPlaying) {
                if (playPauseButton != null) {
                    playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause_circle_24px : R.drawable.ic_play_circle_24px);
                }
            }
            
            @Override
            public void onProgress(int positionMs, int durationMs) {
                if (current != null) current.setText(formatTime(positionMs));
                if (total != null) total.setText(formatTime(durationMs));
                if (progress != null && durationMs > 0 && !progress.isPressed()) {
                    progress.setProgress((int)(positionMs * 1000f / durationMs));
                }
            }
            
            @Override
            public void onMetadataChanged(String t, String a) {
                // Update title when song changes
                if (titleView != null) {
                    titleView.setText(t != null && !t.isEmpty() ? t : "No song playing");
                }
                // Refresh queue list to exclude new current song
                refreshQueue();
            }
            
            @Override
            public void onSongCompleted() {
                // Song finished, next song will auto-play
            }
        };
        vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get().addListener(playbackListener);
    }
    
    private void refreshQueue() {
        loadQueueSongs();
        if (queueAdapter != null) {
            queueAdapter.notifyDataSetChanged();
        }
        updateEmptyState();
    }
    
    private void loadQueueSongs() {
        // Clear and reload queue from QueueManager
        queueSongs.clear();
        
        // Initialize QueueManager and get the queue
        vn.edu.tdtu.lhqc.meowsic.QueueManager.getInstance(this);
        List<Song> queueFromManager = vn.edu.tdtu.lhqc.meowsic.QueueManager.getQueue();
        
        // Get current playing song URI to exclude it
        vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
        android.net.Uri currentUri = pm.getCurrentUri();
        String currentUriString = (currentUri != null) ? currentUri.toString() : null;
        int currentQueueIndex = vn.edu.tdtu.lhqc.meowsic.QueueManager.getCurrentIndex();
        
        // Add all songs from queue except the currently playing one
        for (int i = 0; i < queueFromManager.size(); i++) {
            // Skip the current song (at currentQueueIndex)
            if (i == currentQueueIndex) continue;
            
            Song song = queueFromManager.get(i);
            if (song.getUriString() != null) {
                queueSongs.add(song);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playbackListener != null) {
            vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get().removeListener(playbackListener);
        }
    }
    
    private String formatTime(int ms) {
        int s = Math.max(0, ms / 1000);
        int m = s / 60;
        s %= 60;
        return String.format(java.util.Locale.getDefault(), "%d:%02d", m, s);
    }

    private void setupRecycler() {
        recyclerViewQueue = findViewById(R.id.recyclerViewQueue);
        emptyStateView = findViewById(R.id.queue_empty_state);
        if (recyclerViewQueue == null) return;

        // Load queue songs
        loadQueueSongs();

        // Setup adapter
        queueAdapter = new QueueAdapter(queueSongs);
        recyclerViewQueue.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewQueue.setAdapter(queueAdapter);
        
        // Setup click listeners
        queueAdapter.setOnMoreClickListener((anchor, position) -> showQueueItemMenu(anchor, position));
        queueAdapter.setOnItemClickListener(position -> {
            if (position < 0 || position >= queueSongs.size()) return;
            Song s = queueSongs.get(position);
            String uri = s.getUriString();
            if (uri != null) {
                try {
                    vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get()
                        .play(this, android.net.Uri.parse(uri), s.getTitle(), s.getArtist(), s.getAlbumArtBase64());
                } catch (Exception ignored) {}
            }
        });
        
        // Update empty state visibility
        updateEmptyState();
    }

    private void showQueueItemMenu(android.view.View anchor, int position) {
        if (position == RecyclerView.NO_POSITION) return;
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Remove from queue");
        popup.getMenu().add(0, 2, 1, "Move to Up Next");
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                // Remove the selected song from both UI and QueueManager
                if (position >= 0 && position < queueSongs.size()) {
                    Song songToRemove = queueSongs.get(position);
                    String uriToRemove = songToRemove.getUriString();
                    
                    // Remove from UI
                    queueSongs.remove(position);
                    queueAdapter.notifyItemRemoved(position);
                    
                    // Remove from QueueManager
                    if (uriToRemove != null) {
                        vn.edu.tdtu.lhqc.meowsic.QueueManager.removeSong(uriToRemove);
                    }
                    
                    updateEmptyState();
                }
                return true;
            } else if (id == 2) {
                // Move to play next (right after current song in QueueManager)
                if (position >= 0 && position < queueSongs.size()) {
                    Song songToMove = queueSongs.get(position);
                    String uriToMove = songToMove.getUriString();
                    
                    // Remove from current position in QueueManager
                    if (uriToMove != null) {
                        vn.edu.tdtu.lhqc.meowsic.QueueManager.removeSong(uriToMove);
                        // Add to next position
                        vn.edu.tdtu.lhqc.meowsic.QueueManager.addNext(songToMove);
                    }
                    
                    // Refresh the UI
                    refreshQueue();
                }
                return true;
            }
            return false;
        });
        popup.show();
    }
    
    private void loadCurrentSongAlbumArt() {
        if (albumArtView == null) return;
        
        vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
        android.net.Uri currentUri = pm.getCurrentUri();
        
        if (currentUri == null) {
            albumArtView.setImageResource(R.drawable.billie_eilish);
            return;
        }
        
        // Load album art from library
        List<Song> allSongs = vn.edu.tdtu.lhqc.meowsic.SongStore.load(this);
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
    
    private void updateEmptyState() {
        if (emptyStateView == null || recyclerViewQueue == null) return;
        
        boolean isEmpty = queueSongs.isEmpty();
        emptyStateView.setVisibility(isEmpty ? android.view.View.VISIBLE : android.view.View.GONE);
        recyclerViewQueue.setVisibility(isEmpty ? android.view.View.GONE : android.view.View.VISIBLE);
    }
}