package vn.edu.tdtu.lhqc.meowsic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for playlist songs with selection mode support
 */
public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.SongViewHolder> {
    private List<Song> songs;
    private OnSongClickListener onSongClickListener;
    private OnSongMoreClickListener onSongMoreClickListener;
    private OnSelectionChangedListener onSelectionChangedListener;
    private boolean selectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }
    
    public interface OnSongMoreClickListener {
        void onSongMoreClick(Song song, View view);
    }
    
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public PlaylistSongAdapter(List<Song> songs) {
        this.songs = new ArrayList<>(songs);
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.onSongClickListener = listener;
    }
    
    public void setOnSongMoreClickListener(OnSongMoreClickListener listener) {
        this.onSongMoreClickListener = listener;
    }
    
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.onSelectionChangedListener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.library_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
        
        // Load album art if available, otherwise use default image
        if (song.hasAlbumArt()) {
            try {
                byte[] decodedBytes = android.util.Base64.decode(song.getAlbumArtBase64(), android.util.Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.image.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.image.setImageResource(song.getImageRes());
            }
        } else {
            holder.image.setImageResource(song.getImageRes());
        }

        // Show/hide checkbox and more button based on selection mode
        if (selectionMode) {
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.arrow.setVisibility(View.GONE);
            holder.btnMore.setVisibility(View.GONE);
            holder.checkbox.setChecked(selectedPositions.contains(position));
            
            // In selection mode, clicking toggles selection
            holder.itemView.setOnClickListener(v -> {
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                    holder.checkbox.setChecked(false);
                } else {
                    selectedPositions.add(position);
                    holder.checkbox.setChecked(true);
                }
                notifySelectionChanged();
            });
            
            // Checkbox click also toggles selection
            holder.checkbox.setOnClickListener(v -> {
                if (holder.checkbox.isChecked()) {
                    selectedPositions.add(position);
                } else {
                    selectedPositions.remove(position);
                }
                notifySelectionChanged();
            });
        } else {
            holder.checkbox.setVisibility(View.GONE);
            holder.arrow.setVisibility(View.GONE);
            holder.btnMore.setVisibility(View.VISIBLE);
            holder.checkbox.setChecked(false);
            
            // Normal mode, clicking plays the song
            holder.itemView.setOnClickListener(v -> {
                if (onSongClickListener != null) {
                    onSongClickListener.onSongClick(song);
                }
            });
            
            // More button click
            holder.btnMore.setOnClickListener(v -> {
                if (onSongMoreClickListener != null) {
                    onSongMoreClickListener.onSongMoreClick(song, v);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    /**
     * Enable selection mode
     */
    public void enterSelectionMode() {
        selectionMode = true;
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    /**
     * Exit selection mode and clear selections
     */
    public void exitSelectionMode() {
        selectionMode = false;
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    /**
     * Check if in selection mode
     */
    public boolean isInSelectionMode() {
        return selectionMode;
    }

    /**
     * Get selected songs
     */
    public List<Song> getSelectedSongs() {
        List<Song> selected = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            if (pos < songs.size()) {
                selected.add(songs.get(pos));
            }
        }
        return selected;
    }

    /**
     * Get count of selected items
     */
    public int getSelectedCount() {
        return selectedPositions.size();
    }

    /**
     * Update the data
     */
    public void updateData(List<Song> newSongs) {
        this.songs.clear();
        this.songs.addAll(newSongs);
        selectedPositions.clear();
        notifyDataSetChanged();
    }
    
    /**
     * Notify listener about selection change
     */
    private void notifySelectionChanged() {
        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.onSelectionChanged(selectedPositions.size());
        }
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, artist;
        CheckBox checkbox;
        ImageView arrow;
        ImageView btnMore;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageItem);
            title = itemView.findViewById(R.id.textTitle);
            artist = itemView.findViewById(R.id.textType);
            checkbox = itemView.findViewById(R.id.checkbox_select);
            arrow = itemView.findViewById(R.id.arrow_icon);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}

