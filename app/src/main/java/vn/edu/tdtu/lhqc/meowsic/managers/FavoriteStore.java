package vn.edu.tdtu.lhqc.meowsic.managers;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.models.Song;

public final class FavoriteStore {
    private static final String PREF = "favorite_store";
    private static final String KEY_SONGS = "songs";

    private FavoriteStore() {}

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
                    arr.put(o);
                } catch (JSONException ignored) {}
            }
        }
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_SONGS, arr.toString()).apply();
    }

    public static void addSong(Context context, Song song) {
        if (song == null || song.getUriString() == null) return;
        
        List<Song> existing = load(context);
        
        // Check if already exists
        for (Song s : existing) {
            if (s.getUriString() != null && s.getUriString().equals(song.getUriString())) {
                return; // Already exists
            }
        }
        
        existing.add(0, song);
        save(context, existing);
    }

    public static void removeSong(Context context, String uriString) {
        if (uriString == null) return;
        
        List<Song> existing = load(context);
        existing.removeIf(s -> s.getUriString() != null && s.getUriString().equals(uriString));
        save(context, existing);
    }

    public static boolean isFavorite(Context context, String uriString) {
        if (uriString == null) return false;
        
        List<Song> existing = load(context);
        for (Song s : existing) {
            if (s.getUriString() != null && s.getUriString().equals(uriString)) {
                return true;
            }
        }
        return false;
    }
}
