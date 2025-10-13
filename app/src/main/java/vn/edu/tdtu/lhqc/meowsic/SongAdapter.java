package vn.edu.tdtu.lhqc.meowsic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> fullList;
    private List<Song> filteredList;
    private String currentType = "All";


    // Constructor
    public SongAdapter(List<Song> songList) {
        this.fullList = new ArrayList<>(songList);
        this.filteredList = new ArrayList<>(songList);
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = filteredList.get(position);
        holder.title.setText(song.getTitle());
        holder.type.setText(song.getType());
        holder.image.setImageResource(song.getImageRes());
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
                    song.getType().toLowerCase().contains(query.toLowerCase());
            if (matchType && matchQuery) {
                filteredList.add(song);
            }
        }
        notifyDataSetChanged();
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
}
