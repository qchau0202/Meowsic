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
        releaseInternal();
        appContext = context.getApplicationContext();
        currentTitle = title == null ? "" : title;
        currentArtist = artist == null ? "" : artist;
        currentUri = uri;
        notifyMetadata();
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
                playNextSong();
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            releaseInternal();
        }
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
    
    private void playNextSong() {
        if (appContext == null) return;
        
        // Load all songs from library
        java.util.List<Song> allSongs = SongStore.load(appContext);
        
        // Find current song index and play next one
        int currentIndex = -1;
        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            if (song.getUriString() != null && song.getTitle().equals(currentTitle)) {
                currentIndex = i;
                break;
            }
        }
        
        // Find next song with valid URI
        if (currentIndex >= 0) {
            for (int i = currentIndex + 1; i < allSongs.size(); i++) {
                Song nextSong = allSongs.get(i);
                if (nextSong.getUriString() != null) {
                    try {
                        play(appContext, Uri.parse(nextSong.getUriString()), 
                             nextSong.getTitle(), nextSong.getArtist());
                    } catch (Exception ignored) {}
                    return;
                }
            }
        }
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


