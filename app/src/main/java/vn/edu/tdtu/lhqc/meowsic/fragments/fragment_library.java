package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        // Fake danh sách bài hát
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("Nắng Ấm Xa Dần", "Sơn Tùng MTP"));
        songs.add(new Song("Lạc Trôi", "Sơn Tùng MTP"));
        songs.add(new Song("Chúng Ta Không Thuộc Về Nhau", "Sơn Tùng MTP"));
        songs.add(new Song("Em Của Ngày Hôm Qua", "Sơn Tùng MTP"));
        songs.add(new Song("Chạy Ngay Đi", "Sơn Tùng MTP"));

        // Setup RecyclerView
        SongAdapter adapter = new SongAdapter(songs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

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
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        return view;
    }
}