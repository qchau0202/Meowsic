package vn.edu.tdtu.lhqc.meowsic;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class SongStore {
    private static final String PREF = "song_store";
    private static final String KEY_SONGS = "songs";

    private SongStore() {}

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
                String type = o.optString("type", "song");
                int imageRes = o.optInt("imageRes", R.drawable.meowsic_black_icon);
                String uriString = o.optString("uri", null);
                if (uriString != null && !uriString.isEmpty()) {
                    out.add(new Song(title, artist, imageRes, uriString));
                } else {
                    out.add(new Song(title, artist, type, imageRes));
                }
            }
        } catch (JSONException ignored) {}
        return out;
    }

    public static void save(Context context, List<Song> songs) {
        JSONArray arr = new JSONArray();
        if (songs != null) {
            for (Song s : songs) {
                try {
                    JSONObject o = new JSONObject();
                    o.put("title", s.getTitle());
                    o.put("artist", s.getArtist());
                    o.put("type", s.getType());
                    o.put("imageRes", s.getImageRes());
                    String uri = s.getUriString();
                    if (uri != null) o.put("uri", uri);
                    arr.put(o);
                } catch (JSONException ignored) {}
            }
        }
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_SONGS, arr.toString()).apply();
    }

    public static void addAllAtTop(Context context, List<Song> newSongs) {
        List<Song> existing = load(context);
        if (newSongs != null && !newSongs.isEmpty()) {
            existing.addAll(0, newSongs);
            save(context, existing);
        }
    }
}


