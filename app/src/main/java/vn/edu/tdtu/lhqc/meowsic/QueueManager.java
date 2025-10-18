package vn.edu.tdtu.lhqc.meowsic;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the playback queue for songs
 */
public final class QueueManager {
    private static final String PREFS_NAME = "queue_prefs";
    private static final String KEY_QUEUE = "queue";
    private static final String KEY_CURRENT_INDEX = "current_index";
    
    private static QueueManager instance;
    private List<Song> queue;
    private int currentIndex;
    private Context context;
    
    private QueueManager(Context context) {
        this.context = context.getApplicationContext();
        this.queue = new ArrayList<>();
        this.currentIndex = -1;
        loadQueue();
    }
    
    public static synchronized QueueManager getInstance(Context context) {
        if (instance == null) {
            instance = new QueueManager(context);
        }
        return instance;
    }
    
    /**
     * Add a song to play next (right after the current song)
     */
    public static void addNext(Song song) {
        if (instance == null) return;
        instance.addNextInternal(song);
    }
    
    /**
     * Add a song to the end of the queue
     */
    public static void addToQueue(Song song) {
        if (instance == null) return;
        instance.addToQueueInternal(song);
    }
    
    /**
     * Get the current queue
     */
    public static List<Song> getQueue() {
        if (instance == null) return new ArrayList<>();
        return new ArrayList<>(instance.queue);
    }
    
    /**
     * Get the current index in the queue
     */
    public static int getCurrentIndex() {
        if (instance == null) return -1;
        return instance.currentIndex;
    }
    
    /**
     * Set the queue and current index
     */
    public static void setQueue(List<Song> newQueue, int index) {
        if (instance == null) return;
        instance.setQueueInternal(newQueue, index);
    }
    
    /**
     * Get the next song in the queue
     */
    public static Song getNextSong() {
        if (instance == null) return null;
        return instance.getNextSongInternal();
    }
    
    /**
     * Get the previous song in the queue
     */
    public static Song getPreviousSong() {
        if (instance == null) return null;
        return instance.getPreviousSongInternal();
    }
    
    /**
     * Move to the next song in the queue
     */
    public static void moveNext() {
        if (instance == null) return;
        instance.moveNextInternal();
    }
    
    /**
     * Move to the previous song in the queue
     */
    public static void movePrevious() {
        if (instance == null) return;
        instance.movePreviousInternal();
    }
    
    /**
     * Clear the entire queue
     */
    public static void clear() {
        if (instance == null) return;
        instance.clearInternal();
    }
    
    /**
     * Remove a song from the queue by URI
     */
    public static void removeSong(String uri) {
        if (instance == null) return;
        instance.removeSongInternal(uri);
    }
    
    // Internal methods
    
    private void addNextInternal(Song song) {
        if (song == null) return;
        
        // If queue is empty or no current song, add to the beginning
        if (queue.isEmpty() || currentIndex < 0) {
            queue.add(song);
            currentIndex = 0;
        } else {
            // Insert right after the current song
            int insertPosition = currentIndex + 1;
            if (insertPosition > queue.size()) {
                queue.add(song);
            } else {
                queue.add(insertPosition, song);
            }
        }
        
        saveQueue();
    }
    
    private void addToQueueInternal(Song song) {
        if (song == null) return;
        
        // Add to the end of the queue
        queue.add(song);
        
        // If queue was empty, set current index to 0
        if (currentIndex < 0 && queue.size() == 1) {
            currentIndex = 0;
        }
        
        saveQueue();
    }
    
    private void setQueueInternal(List<Song> newQueue, int index) {
        queue.clear();
        if (newQueue != null) {
            queue.addAll(newQueue);
        }
        currentIndex = index;
        saveQueue();
    }
    
    private Song getNextSongInternal() {
        if (queue.isEmpty() || currentIndex < 0) return null;
        
        int nextIndex = currentIndex + 1;
        if (nextIndex < queue.size()) {
            return queue.get(nextIndex);
        }
        return null;
    }
    
    private Song getPreviousSongInternal() {
        if (queue.isEmpty() || currentIndex <= 0) return null;
        
        int prevIndex = currentIndex - 1;
        if (prevIndex >= 0 && prevIndex < queue.size()) {
            return queue.get(prevIndex);
        }
        return null;
    }
    
    private void moveNextInternal() {
        if (queue.isEmpty() || currentIndex < 0) return;
        
        int nextIndex = currentIndex + 1;
        if (nextIndex < queue.size()) {
            currentIndex = nextIndex;
            saveQueue();
        }
    }
    
    private void movePreviousInternal() {
        if (queue.isEmpty() || currentIndex <= 0) return;
        
        int prevIndex = currentIndex - 1;
        if (prevIndex >= 0) {
            currentIndex = prevIndex;
            saveQueue();
        }
    }
    
    private void clearInternal() {
        queue.clear();
        currentIndex = -1;
        saveQueue();
    }
    
    private void removeSongInternal(String uri) {
        if (uri == null || queue.isEmpty()) return;
        
        for (int i = queue.size() - 1; i >= 0; i--) {
            Song song = queue.get(i);
            if (uri.equals(song.getUriString())) {
                queue.remove(i);
                // Adjust current index if necessary
                if (i < currentIndex) {
                    currentIndex--;
                } else if (i == currentIndex && currentIndex >= queue.size()) {
                    currentIndex = queue.size() - 1;
                }
            }
        }
        
        saveQueue();
    }
    
    // Persistence methods
    
    private void loadQueue() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String queueJson = prefs.getString(KEY_QUEUE, "[]");
        currentIndex = prefs.getInt(KEY_CURRENT_INDEX, -1);
        
        try {
            JSONArray arr = new JSONArray(queueJson);
            queue.clear();
            
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String title = obj.optString("title", "Unknown");
                String artist = obj.optString("artist", "Unknown");
                int imageRes = obj.optInt("imageRes", R.drawable.meowsic_black_icon);
                String uriString = obj.optString("uri", null);
                String albumArt = obj.optString("albumArt", null);
                
                Song song = new Song(title, artist, imageRes, uriString, albumArt);
                queue.add(song);
            }
        } catch (Exception e) {
            queue.clear();
            currentIndex = -1;
        }
    }
    
    private void saveQueue() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        try {
            JSONArray arr = new JSONArray();
            for (Song song : queue) {
                JSONObject obj = new JSONObject();
                obj.put("title", song.getTitle());
                obj.put("artist", song.getArtist());
                obj.put("imageRes", song.getImageRes());
                obj.put("uri", song.getUriString());
                if (song.hasAlbumArt()) {
                    obj.put("albumArt", song.getAlbumArtBase64());
                }
                arr.put(obj);
            }
            
            prefs.edit()
                .putString(KEY_QUEUE, arr.toString())
                .putInt(KEY_CURRENT_INDEX, currentIndex)
                .apply();
        } catch (Exception e) {
            // Error saving queue
        }
    }
}

