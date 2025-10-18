package vn.edu.tdtu.lhqc.meowsic;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.util.HashSet;
import java.util.Set;

public final class PlaybackManager {
    public interface Listener {
        void onStateChanged(boolean isPlaying);
        void onProgress(int positionMs, int durationMs);
        void onMetadataChanged(String title, String artist);
        void onSongCompleted();
    }

    private static PlaybackManager INSTANCE;
    public static synchronized PlaybackManager get() {
        if (INSTANCE == null) INSTANCE = new PlaybackManager();
        return INSTANCE;
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<Listener> listeners = new HashSet<>();
    private MediaPlayer mediaPlayer;
    private boolean prepared = false;
    private String currentTitle = "";
    private String currentArtist = "";
    private String currentAlbumArt = null;
    private Uri currentUri;
    private Context appContext;

    private final Runnable progressRunnable = new Runnable() {
        @Override public void run() {
            if (mediaPlayer != null && prepared) {
                int dur = mediaPlayer.getDuration();
                int pos = mediaPlayer.getCurrentPosition();
                for (Listener l : listeners) l.onProgress(pos, dur);
                if (mediaPlayer.isPlaying()) handler.postDelayed(this, 500);
            }
        }
    };

    private PlaybackManager() {}

    public void addListener(Listener l) { if (l != null) listeners.add(l); }
    public void removeListener(Listener l) { if (l != null) listeners.remove(l); }

    public void play(Context context, Uri uri, String title, String artist) {
        play(context, uri, title, artist, null);
    }
    
    public void play(Context context, Uri uri, String title, String artist, String albumArtBase64) {
        releaseInternal();
        appContext = context.getApplicationContext();
        currentTitle = title == null ? "" : title;
        currentArtist = artist == null ? "" : artist;
        currentAlbumArt = albumArtBase64;
        currentUri = uri;
        notifyMetadata();
        
        // Add to recently played
        if (uri != null && title != null) {
            Song recentSong = new Song(title, artist == null ? "Unknown" : artist, R.drawable.meowsic_black_icon, uri.toString(), albumArtBase64);
            RecentlyPlayedStore.addSong(appContext, recentSong);
        }
        
        // Initialize QueueManager and always sync with library when playing starts
        QueueManager.getInstance(appContext);
        syncQueueWithLibrary();
        
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.setOnPreparedListener(mp -> {
                prepared = true;
                mp.start();
                notifyState();
                handler.post(progressRunnable);
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                notifyState();
                notifySongCompleted();
                playNext();  // Auto-play next song when current song completes
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            releaseInternal();
        }
    }
    
    /**
     * Sync QueueManager with the library songs
     */
    private void syncQueueWithLibrary() {
        if (appContext == null) return;
        
        // Load all songs from library
        java.util.List<Song> allSongs = SongStore.load(appContext);
        
        // Filter to only actual songs (with URIs)
        java.util.List<Song> songQueue = new java.util.ArrayList<>();
        int currentIndex = -1;
        String currentUriString = currentUri != null ? currentUri.toString() : null;
        
        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            if (song.getUriString() != null) {
                songQueue.add(song);
                // Mark current song index
                if (currentUriString != null && song.getUriString().equals(currentUriString)) {
                    currentIndex = songQueue.size() - 1;
                }
            }
        }
        
        // Set the queue in QueueManager
        QueueManager.setQueue(songQueue, currentIndex);
    }

    public void togglePlayPause() {
        if (mediaPlayer == null || !prepared) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
            handler.post(progressRunnable);
        }
        notifyState();
    }

    public boolean isPlaying() {
        return mediaPlayer != null && prepared && mediaPlayer.isPlaying();
    }

    public int getDuration() { return mediaPlayer != null && prepared ? mediaPlayer.getDuration() : 0; }
    public int getPosition() { return mediaPlayer != null && prepared ? mediaPlayer.getCurrentPosition() : 0; }

    public void seekTo(int positionMs) {
        if (mediaPlayer != null && prepared) mediaPlayer.seekTo(positionMs);
    }

    public String getCurrentTitle() { return currentTitle; }
    public String getCurrentArtist() { return currentArtist; }
    public Uri getCurrentUri() { return currentUri; }

    private void notifyState() {
        boolean p = isPlaying();
        for (Listener l : listeners) l.onStateChanged(p);
    }

    private void notifyMetadata() {
        for (Listener l : listeners) l.onMetadataChanged(currentTitle, currentArtist);
    }
    
    private void notifySongCompleted() {
        for (Listener l : listeners) l.onSongCompleted();
    }
    
    /**
     * Play the next song - uses QueueManager if available, otherwise uses library order
     */
    public void playNext() {
        if (appContext == null) return;
        
        // Initialize QueueManager
        QueueManager.getInstance(appContext);
        
        // Try to get next song from QueueManager first
        Song nextSong = QueueManager.getNextSong();
        if (nextSong != null && nextSong.getUriString() != null) {
            QueueManager.moveNext();
            playSong(nextSong);
            return;
        }
        
        // Check if queue is completely empty (no songs at all)
        if (QueueManager.getQueue().isEmpty()) {
            // Terminate playback if no songs in queue
            releaseInternal();
            // Clear the queue to ensure clean state
            QueueManager.clear();
            return;
        }
        
        // Fallback to library order if queue has songs but no next song
        playSongAtOffset(1);
    }
    
    /**
     * Play the previous song - uses QueueManager if available, otherwise uses library order
     */
    public void playPrevious() {
        if (appContext == null) return;
        
        // Initialize QueueManager
        QueueManager.getInstance(appContext);
        
        // Try to get previous song from QueueManager first
        Song prevSong = QueueManager.getPreviousSong();
        if (prevSong != null && prevSong.getUriString() != null) {
            QueueManager.movePrevious();
            playSong(prevSong);
            return;
        }
        
        // Fallback to library order if queue is empty or no previous song
        playSongAtOffset(-1);
    }
    
    /**
     * Play a song at a specific offset from the current song in the library
     * @param offset positive for next songs, negative for previous songs
     */
    private void playSongAtOffset(int offset) {
        if (appContext == null) return;
        
        // Load all songs from library
        java.util.List<Song> allSongs = SongStore.load(appContext);
        
        // Find current song index
        int currentIndex = findCurrentSongIndex(allSongs);
        if (currentIndex < 0) return;
        
        // Find target song with valid URI
        int direction = offset > 0 ? 1 : -1;
        int startIndex = currentIndex + direction;
        int endIndex = offset > 0 ? allSongs.size() : -1;
        
        for (int i = startIndex; i != endIndex; i += direction) {
            Song targetSong = allSongs.get(i);
            if (targetSong.getUriString() != null) {
                playSong(targetSong);
                return;
            }
        }
    }
    
    /**
     * Play a specific song
     * @param song the song to play
     */
    private void playSong(Song song) {
        try {
            play(appContext, Uri.parse(song.getUriString()), 
                 song.getTitle(), song.getArtist(), song.getAlbumArtBase64());
        } catch (Exception ignored) {}
    }
    
    /**
     * Find the index of the currently playing song in the library
     * @param songs list of all songs
     * @return index of current song, or -1 if not found
     */
    private int findCurrentSongIndex(java.util.List<Song> songs) {
        if (currentUri == null) return -1;
        String currentUriString = currentUri.toString();
        
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            if (song.getUriString() != null && song.getUriString().equals(currentUriString)) {
                return i;
            }
        }
        return -1;
    }

    public void release() { releaseInternal(); }

    private void releaseInternal() {
        prepared = false;
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (Exception ignored) {}
            try { mediaPlayer.release(); } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        handler.removeCallbacks(progressRunnable);
    }
}


