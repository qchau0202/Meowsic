package vn.edu.tdtu.lhqc.meowsic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.models.Song;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> fullList;
    private List<Song> filteredList;
    private String currentType = "All";
    private OnSongClickListener onSongClickListener;
    private OnSongMoreClickListener onSongMoreClickListener;

    public void updateData(List<Song> allLibraryData) {
        if (allLibraryData == null) return;
        fullList.clear();
        fullList.addAll(allLibraryData);
        restoreFullList(); // Hiển thị toàn bộ, không filter
    }

    // Interface for handling song clicks
    public interface OnSongClickListener {
        void onSongClick(Song song);
    }
    
    // Interface for handling more button clicks
    public interface OnSongMoreClickListener {
        void onSongMoreClick(Song song, View view);
    }

    // Constructor
    public SongAdapter(List<Song> songList) {
        this.fullList = new ArrayList<>(songList);
        this.filteredList = new ArrayList<>(songList);
    }

    // Set click listener
    public void setOnSongClickListener(OnSongClickListener listener) {
        this.onSongClickListener = listener;
    }
    
    // Set more button click listener
    public void setOnSongMoreClickListener(OnSongMoreClickListener listener) {
        this.onSongMoreClickListener = listener;
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
        Song song = filteredList.get(position);
        holder.title.setText(song.getTitle());
        holder.type.setText(song.getArtist());
        
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
        
        // Show/hide more button and arrow icon based on item type
        boolean isSong = song.getUriString() != null && !song.getUriString().isEmpty() 
                         && !song.getType().equalsIgnoreCase("Playlist");
        
        if (holder.btnMore != null) {
            holder.btnMore.setVisibility(isSong ? View.VISIBLE : View.GONE);
            if (isSong) {
                holder.btnMore.setOnClickListener(v -> {
                    if (onSongMoreClickListener != null) {
                        onSongMoreClickListener.onSongMoreClick(song, v);
                    }
                });
            }
        }
        
        if (holder.arrowIcon != null) {
            holder.arrowIcon.setVisibility(song.getType().equalsIgnoreCase("Playlist") ? View.VISIBLE : View.GONE);
        }
        
        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (onSongClickListener != null) {
                onSongClickListener.onSongClick(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filterByType(String type) {
        currentType = type;
        applyFilters("");
    }

    public void filterByQuery(String query) {
        applyFilters(query);
    }
    // Hàm filter để SearchView gọi
    private void applyFilters(String query) {
        filteredList.clear();
        for (Song song : fullList) {
            boolean matchType = currentType.equals("All") || song.getType().equalsIgnoreCase(currentType);
            boolean matchQuery = query.isEmpty() ||
                    song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    song.getArtist().toLowerCase().contains(query.toLowerCase());
            if (matchType && matchQuery) {
                filteredList.add(song);
            }
        }
        notifyDataSetChanged();
    }
    
    // Method to restore full list when needed
    public void restoreFullList() {
        filteredList.clear();
        filteredList.addAll(fullList);
        currentType = "All";
        notifyDataSetChanged();
    }

    // Add a single song to the top of the list
    public void addSong(Song song) {
        if (song == null) return;
        fullList.add(0, song);
        restoreFullList();
    }

    // Add multiple songs to the top of the list
    public void addSongs(List<Song> songs) {
        if (songs == null || songs.isEmpty()) return;
        fullList.addAll(0, songs);
        restoreFullList();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView arrowIcon;
        ImageView btnMore;
        TextView title, type;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageItem);
            title = itemView.findViewById(R.id.textTitle);
            type = itemView.findViewById(R.id.textType);
            arrowIcon = itemView.findViewById(R.id.arrow_icon);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }

    private boolean isGridView = false;

    public void toggleViewType() {
        isGridView = !isGridView;
        notifyDataSetChanged();
    }

    public boolean isGridView() {
        return isGridView;
    }

    // Nếu bạn muốn inflate layout khác nhau cho grid/list, sửa lại onCreateViewHolder:
    @Override
    public int getItemViewType(int position) {
        return isGridView ? 1 : 0;
    }

    // ===== SORT METHODS =====
    public void sortByTitle(boolean ascending) {
        filteredList.sort((s1, s2) -> ascending
                ? s1.getTitle().compareToIgnoreCase(s2.getTitle())
                : s2.getTitle().compareToIgnoreCase(s1.getTitle()));
        notifyDataSetChanged();
    }

    public void sortByArtist(boolean ascending) {
        filteredList.sort((s1, s2) -> ascending
                ? s1.getArtist().compareToIgnoreCase(s2.getArtist())
                : s2.getArtist().compareToIgnoreCase(s1.getArtist()));
        notifyDataSetChanged();
    }

}
