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

/**
 * Store for managing playlist data and their songs
 */
public final class PlaylistStore {
    private static final String PREF = "playlist_store";
    private static final String KEY_PLAYLISTS = "playlists";

    private PlaylistStore() {}

    /**
     * Load all songs for a specific playlist
     * @param context Android context
     * @param playlistName Name of the playlist
     * @return List of songs in the playlist
     */
    public static List<Song> loadPlaylistSongs(Context context, String playlistName) {
        List<Song> out = new ArrayList<>();
        if (playlistName == null) return out;
        
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_PLAYLISTS, null);
        if (raw == null || raw.isEmpty()) return out;
        
        try {
            JSONObject playlists = new JSONObject(raw);
            if (playlists.has(playlistName)) {
                JSONArray songs = playlists.getJSONArray(playlistName);
                for (int i = 0; i < songs.length(); i++) {
                    JSONObject o = songs.getJSONObject(i);
                    String title = o.optString("title", "Unknown");
                    String artist = o.optString("artist", "Unknown");
                    int imageRes = o.optInt("imageRes", R.drawable.meowsic_black_icon);
                    String uriString = o.optString("uri", null);
                    String albumArt = o.optString("albumArt", null);
                    
                    if (uriString != null && !uriString.isEmpty()) {
                        out.add(new Song(title, artist, imageRes, uriString, albumArt));
                    }
                }
            }
        } catch (JSONException ignored) {}
        return out;
    }

    /**
     * Save songs for a specific playlist
     * @param context Android context
     * @param playlistName Name of the playlist
     * @param songs List of songs to save
     */
    public static void savePlaylistSongs(Context context, String playlistName, List<Song> songs) {
        if (playlistName == null) return;
        
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_PLAYLISTS, null);
        
        try {
            JSONObject playlists = (raw != null && !raw.isEmpty()) 
                ? new JSONObject(raw) 
                : new JSONObject();
            
            JSONArray songsArray = new JSONArray();
            if (songs != null) {
                for (Song s : songs) {
                    JSONObject o = new JSONObject();
                    o.put("title", s.getTitle());
                    o.put("artist", s.getArtist());
                    o.put("imageRes", s.getImageRes());
                    String uri = s.getUriString();
                    if (uri != null) o.put("uri", uri);
                    String albumArt = s.getAlbumArtBase64();
                    if (albumArt != null) o.put("albumArt", albumArt);
                    songsArray.put(o);
                }
            }
            
            playlists.put(playlistName, songsArray);
            sp.edit().putString(KEY_PLAYLISTS, playlists.toString()).apply();
        } catch (JSONException ignored) {}
    }

    /**
     * Add a song to a playlist
     * @param context Android context
     * @param playlistName Name of the playlist
     * @param song Song to add
     */
    public static void addSongToPlaylist(Context context, String playlistName, Song song) {
        if (song == null || playlistName == null) return;
        List<Song> existing = loadPlaylistSongs(context, playlistName);
        
        // Check if song already exists in the playlist
        for (Song existingSong : existing) {
            if (existingSong.getUriString() != null && song.getUriString() != null &&
                existingSong.getUriString().equals(song.getUriString())) {
                return; // Song already exists, don't add duplicate
            }
        }
        
        existing.add(song);
        savePlaylistSongs(context, playlistName, existing);
    }

    /**
     * Add multiple songs to a playlist
     * @param context Android context
     * @param playlistName Name of the playlist
     * @param songs Songs to add
     */
    public static void addSongsToPlaylist(Context context, String playlistName, List<Song> songs) {
        if (songs == null || songs.isEmpty() || playlistName == null) return;
        List<Song> existing = loadPlaylistSongs(context, playlistName);
        
        // Create a set of existing URIs for efficient lookup
        java.util.Set<String> existingUris = new java.util.HashSet<>();
        for (Song existingSong : existing) {
            if (existingSong.getUriString() != null) {
                existingUris.add(existingSong.getUriString());
            }
        }
        
        // Only add songs that don't already exist
        boolean addedAny = false;
        for (Song song : songs) {
            if (song.getUriString() != null && !existingUris.contains(song.getUriString())) {
                existing.add(song);
                existingUris.add(song.getUriString()); // Add to set to avoid duplicates within the batch
                addedAny = true;
            }
        }
        
        if (addedAny) {
            savePlaylistSongs(context, playlistName, existing);
        }
    }

    /**
     * Remove a song from a playlist
     * @param context Android context
     * @param playlistName Name of the playlist
     * @param songUri URI of the song to remove
     */
    public static void removeSongFromPlaylist(Context context, String playlistName, String songUri) {
        if (songUri == null || playlistName == null) return;
        List<Song> existing = loadPlaylistSongs(context, playlistName);
        existing.removeIf(s -> s.getUriString() != null && s.getUriString().equals(songUri));
        savePlaylistSongs(context, playlistName, existing);
    }

    /**
     * Get all playlist names
     * @param context Android context
     * @return List of all playlist names
     */
    public static List<String> getAllPlaylistNames(Context context) {
        List<String> playlistNames = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_PLAYLISTS, null);
        if (raw == null || raw.isEmpty()) return playlistNames;
        
        try {
            JSONObject playlists = new JSONObject(raw);
            JSONArray names = playlists.names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    playlistNames.add(names.getString(i));
                }
            }
        } catch (JSONException ignored) {}
        
        return playlistNames;
    }

    /**
     * Delete an entire playlist
     * @param context Android context
     * @param playlistName Name of the playlist to delete
     */
    public static void deletePlaylist(Context context, String playlistName) {
        if (playlistName == null) return;
        
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_PLAYLISTS, null);
        if (raw == null || raw.isEmpty()) return;
        
        try {
            JSONObject playlists = new JSONObject(raw);
            playlists.remove(playlistName);
            sp.edit().putString(KEY_PLAYLISTS, playlists.toString()).apply();
        } catch (JSONException ignored) {}
    }

    /**
     * Clear all playlists
     * @param context Android context
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}

