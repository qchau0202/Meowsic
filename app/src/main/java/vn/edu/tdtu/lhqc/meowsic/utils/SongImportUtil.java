package vn.edu.tdtu.lhqc.meowsic.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.models.Song;

/**
 * Utility class for importing songs from MP3 files
 */
public final class SongImportUtil {
    
    private SongImportUtil() {
        // Utility class, no instantiation
    }
    
    /**
     * Build a Song object from an audio file URI by extracting metadata
     * 
     * @param context Context for accessing content resolver
     * @param uri URI of the audio file
     * @return Song object with extracted metadata, or null if extraction fails
     */
    public static Song buildSongFromUri(Context context, Uri uri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(context, uri);
            String title = valueOr(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE), "Unknown");
            String artist = valueOr(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST), "Unknown");
            
            // Extract album art
            String albumArtBase64 = null;
            byte[] art = mmr.getEmbeddedPicture();
            if (art != null) {
                // Encode to Base64 for storage
                albumArtBase64 = android.util.Base64.encodeToString(art, android.util.Base64.DEFAULT);
            }
            
            return new Song(title, artist, R.drawable.meowsic_black_icon, uri.toString(), albumArtBase64);
        } catch (Exception e) {
            return null;
        } finally {
            try { 
                mmr.release(); 
            } catch (Exception ignored) {}
        }
    }
    
    private static String valueOr(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}

