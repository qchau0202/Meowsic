package vn.edu.tdtu.lhqc.meowsic;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class RecentlyPlayedStore {
    private static final String PREF = "recently_played";
    private static final String KEY_SONGS = "songs";
    private static final int MAX_RECENT_SONGS = 10; // Keep last 10 songs

    private RecentlyPlayedStore() {}

    public static List<Song> load(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_SONGS, null);
        List<Song> out = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return out;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String title = o.optString("title", "Unknown");
                String artist = o.optString("artist", "Unknown");
                int imageRes = o.optInt("imageRes", R.drawable.meowsic_black_icon);
                String uriString = o.optString("uri", null);
                String albumArt = o.optString("albumArt", null);
                long timestamp = o.optLong("timestamp", System.currentTimeMillis());
                
                if (uriString != null && !uriString.isEmpty()) {
                    Song song = new Song(title, artist, imageRes, uriString, albumArt);
                    out.add(song);
                }
            }
        } catch (JSONException ignored) {}
        return out;
    }

    private static void save(Context context, List<Song> songs) {
        JSONArray arr = new JSONArray();
        if (songs != null) {
            for (Song s : songs) {
                try {
                    JSONObject o = new JSONObject();
                    o.put("title", s.getTitle());
                    o.put("artist", s.getArtist());
                    o.put("imageRes", s.getImageRes());
                    String uri = s.getUriString();
                    if (uri != null) o.put("uri", uri);
                    String albumArt = s.getAlbumArtBase64();
                    if (albumArt != null) o.put("albumArt", albumArt);
                    o.put("timestamp", System.currentTimeMillis());
                    arr.put(o);
                } catch (JSONException ignored) {}
            }
        }
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_SONGS, arr.toString()).apply();
    }

    // Add a song to recently played, removing duplicates and keeping only recent ones
    public static void addSong(Context context, Song song) {
        if (song == null || song.getUriString() == null) return;
        
        List<Song> existing = load(context);
        
        // Remove if already exists (to move it to top)
        existing.removeIf(s -> s.getUriString() != null && s.getUriString().equals(song.getUriString()));
        
        // Add to top
        existing.add(0, song);
        
        // Keep only the most recent MAX_RECENT_SONGS
        if (existing.size() > MAX_RECENT_SONGS) {
            existing = existing.subList(0, MAX_RECENT_SONGS);
        }
        
        save(context, existing);
    }

    // Remove specific songs by URI
    public static void removeSongs(Context context, java.util.Set<String> urisToRemove) {
        if (urisToRemove == null || urisToRemove.isEmpty()) return;
        
        List<Song> existing = load(context);
        existing.removeIf(song -> 
            song.getUriString() != null && urisToRemove.contains(song.getUriString())
        );
        
        clear(context);
        for (Song song : existing) {
            addSong(context, song);
        }
    }

    // Clear all recently played
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}

