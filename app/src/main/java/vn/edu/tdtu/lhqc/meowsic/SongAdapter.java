package vn.edu.tdtu.lhqc.meowsic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> fullList;
    private List<Song> filteredList;
    private String currentType = "All";
    private OnSongClickListener onSongClickListener;

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

    // Constructor
    public SongAdapter(List<Song> songList) {
        this.fullList = new ArrayList<>(songList);
        this.filteredList = new ArrayList<>(songList);
    }

    // Set click listener
    public void setOnSongClickListener(OnSongClickListener listener) {
        this.onSongClickListener = listener;
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
        holder.image.setImageResource(song.getImageRes());
        
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
        TextView title, type;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageItem);
            title = itemView.findViewById(R.id.textTitle);
            type = itemView.findViewById(R.id.textType);
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
