package vn.edu.tdtu.lhqc.meowsic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    public interface OnMoreClickListener {
        void onMoreClick(View anchor, int position);
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final List<Song> queue;
    private OnMoreClickListener moreClickListener;
    private OnItemClickListener itemClickListener;

    public QueueAdapter(@NonNull List<Song> queue) {
        this.queue = queue;
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        this.moreClickListener = listener;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_queue_song, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Song s = queue.get(position);
        holder.title.setText(s.getTitle());
        holder.sub.setText(s.getArtist());
        holder.icon.setImageResource(s.getImageRes());

        holder.more.setOnClickListener(v -> {
            if (moreClickListener != null) moreClickListener.onMoreClick(v, holder.getBindingAdapterPosition());
        });
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return queue.size();
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView sub;
        ImageView more;
        QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.queue_art);
            title = itemView.findViewById(R.id.queue_title);
            sub = itemView.findViewById(R.id.queue_sub);
            more = itemView.findViewById(R.id.queue_more);
        }
    }
}


