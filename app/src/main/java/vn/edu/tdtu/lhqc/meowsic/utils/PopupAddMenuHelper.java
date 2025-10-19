package vn.edu.tdtu.lhqc.meowsic.utils;

import android.app.Dialog;
import android.content.Context;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import vn.edu.tdtu.lhqc.meowsic.R;

public final class PopupAddMenuHelper {

    public interface Listener {
        void onCreatePlaylistSelected();
        void onImportMusicSelected();
        void onRemoveItemsSelected();
    }

    private PopupAddMenuHelper() {}

    public static void show(@NonNull Context context, @NonNull Listener listener) {
        // Create the dialog
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_menu);
        
        // Make dialog appear in center and have proper dimensions
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
            (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85f),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        );
        
        // Get views
        LinearLayout optionCreatePlaylist = dialog.findViewById(R.id.option_create_playlist);
        LinearLayout optionImportMusic = dialog.findViewById(R.id.option_import_music);
        LinearLayout optionRemoveSongs = dialog.findViewById(R.id.option_remove_songs);
        
        // Set up click listeners
        if (optionCreatePlaylist != null) {
            optionCreatePlaylist.setOnClickListener(v -> {
                listener.onCreatePlaylistSelected();
                dialog.dismiss();
            });
        }
        if (optionImportMusic != null) {
            optionImportMusic.setOnClickListener(v -> {
                listener.onImportMusicSelected();
                dialog.dismiss();
            });
        }
        if (optionRemoveSongs != null) {
            optionRemoveSongs.setOnClickListener(v -> {
                listener.onRemoveItemsSelected();
                dialog.dismiss();
            });
        }
        // Show dialog
        dialog.show();
    }
}


