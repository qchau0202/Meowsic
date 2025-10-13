package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.Song;
import vn.edu.tdtu.lhqc.meowsic.SongAdapter;

public class fragment_library extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        TextView textViewInfo = view.findViewById(R.id.textViewInfo);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        SearchView searchView = view.findViewById(R.id.searchView);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewSongs);
        Button btnPlaylist = view.findViewById(R.id.button);
        Button btnArtist = view.findViewById(R.id.button2);
        Button btnAlbum = view.findViewById(R.id.button3);

        // Fake danh sách bài hát
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("Top Hits 2023", "Playlist", R.drawable.top_hits_2023));
        songs.add(new Song("Sơn Tùng MTP", "Artist", R.drawable.sontung));
        songs.add(new Song("Sky Tour", "Album", R.drawable.skytour));
        songs.add(new Song("Ai", "Album", R.drawable.ai));
        songs.add(new Song("G-Dragon", "Artist", R.drawable.gdragon));
        songs.add(new Song("Motivation Mix", "Playlist", R.drawable.motivation_playlist));


        // Setup RecyclerView
        SongAdapter adapter = new SongAdapter(songs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Filter buttons
        btnPlaylist.setOnClickListener(v -> adapter.filterByType("Playlist"));
        btnArtist.setOnClickListener(v -> adapter.filterByType("Artist"));
        btnAlbum.setOnClickListener(v -> adapter.filterByType("Album"));

        // Bấm icon kính lúp → hiện SearchView full, ẩn tiêu đề
        btnSearch.setOnClickListener(v -> {
            btnSearch.setVisibility(View.GONE);
            textViewInfo.setVisibility(View.GONE);

            searchView.setVisibility(View.VISIBLE);
            searchView.setIconified(false);
            searchView.requestFocus();
        });

        // Đóng search → hiện lại tiêu đề và icon
        searchView.setOnCloseListener(() -> {
            searchView.setVisibility(View.GONE);
            btnSearch.setVisibility(View.VISIBLE);
            textViewInfo.setVisibility(View.VISIBLE);
            return false; // false để clear text
        });

        // Bắt sự kiện search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filterByQuery(query);
                return true;


            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterByQuery(newText);
                return true;
            }
        });

        return view;
    }
}