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
    private TextView titleView;
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
        
        ImageView art = findViewById(R.id.np_art);
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
        // Reload queue excluding current song
        queueSongs.clear();
        List<Song> librarySongs = vn.edu.tdtu.lhqc.meowsic.SongStore.load(this);
        vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
        String currentTitle = pm.getCurrentTitle();
        
        for (Song song : librarySongs) {
            if (song.getUriString() != null && !song.getTitle().equals(currentTitle)) {
                queueSongs.add(song);
            }
        }
        
        if (queueAdapter != null) {
            queueAdapter.notifyDataSetChanged();
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
        if (recyclerViewQueue == null) return;

        // Load songs from library, excluding currently playing song
        queueSongs.clear();
        List<Song> librarySongs = vn.edu.tdtu.lhqc.meowsic.SongStore.load(this);
        vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
        String currentTitle = pm.getCurrentTitle();
        
        // Filter to only songs (not playlists) and exclude currently playing song
        for (Song song : librarySongs) {
            if (song.getUriString() != null && !song.getTitle().equals(currentTitle)) {
                queueSongs.add(song);
            }
        }

        queueAdapter = new QueueAdapter(queueSongs);
        recyclerViewQueue.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewQueue.setAdapter(queueAdapter);
        queueAdapter.setOnMoreClickListener((anchor, position) -> showQueueItemMenu(anchor, position));
        queueAdapter.setOnItemClickListener(position -> {
            if (position < 0 || position >= queueSongs.size()) return;
            Song s = queueSongs.get(position);
            String uri = s.getUriString();
            if (uri != null) {
                try {
                    vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get()
                        .play(this, android.net.Uri.parse(uri), s.getTitle(), s.getArtist());
                } catch (Exception ignored) {}
            }
        });
    }

    private void showQueueItemMenu(android.view.View anchor, int position) {
        if (position == RecyclerView.NO_POSITION) return;
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Remove from queue");
        popup.getMenu().add(0, 2, 1, "Add to Up Next");
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                // Remove the selected song
                if (position >= 0 && position < queueSongs.size()) {
                    queueSongs.remove(position);
                    queueAdapter.notifyItemRemoved(position);
                }
                return true;
            } else if (id == 2) {
                // Move the selected song to index 0 (next after current)
                if (position > 0 && position < queueSongs.size()) {
                    Song s = queueSongs.remove(position);
                    queueSongs.add(0, s);
                    queueAdapter.notifyDataSetChanged();
                }
                return true;
            }
            return false;
        });
        popup.show();
    }
}