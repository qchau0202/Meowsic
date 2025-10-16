package vn.edu.tdtu.lhqc.meowsic.ui;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;

import vn.edu.tdtu.lhqc.meowsic.R;

public final class PopupAddMenuHelper {

    public interface Listener {
        void onCreatePlaylistSelected();
        void onImportMusicSelected();
    }

    private PopupAddMenuHelper() {}

    public static void show(@NonNull Context context, @NonNull View anchor, @NonNull Listener listener) {
        PopupMenu popup = new PopupMenu(context, anchor);
        popup.getMenuInflater().inflate(R.menu.add_actions_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_create_playlist) {
                    listener.onCreatePlaylistSelected();
                    return true;
                } else if (id == R.id.action_import_music) {
                    listener.onImportMusicSelected();
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }
}


