package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.Song;
import vn.edu.tdtu.lhqc.meowsic.SongAdapter;

public class fragment_playlist extends Fragment {
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private TextView playlistTitle;
    private fragment_search searchFragment;
    private List<Song> playlistSongs = new ArrayList<>();

    // Playlist information
    private String playlistName = "Playlist";
    private String playlistDescription = "Description";

    // Placeholder for future playlist song data source

    public fragment_playlist() {
        // Required empty public constructor
    }

    public static fragment_playlist newInstance(String playlistName, String playlistDescription) {
        fragment_playlist fragment = new fragment_playlist();
        Bundle args = new Bundle();
        args.putString("playlist_name", playlistName);
        args.putString("playlist_description", playlistDescription);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playlistName = getArguments().getString("playlist_name", "Playlist");
            playlistDescription = getArguments().getString("playlist_description", "Description");
        }
        // No search initialization; this fragment only shows playlist songs
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupSearchFragment();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewSongs);
        playlistTitle = view.findViewById(R.id.playlist_title);
        
        // Update the playlist title with the passed name
        playlistTitle.setText(playlistName);
        
        // No search dropdown in this layout
    }
    
    private void setupRecyclerView() {
        // Sample data for RecyclerView - numbered songs
        playlistSongs.clear();
        for (int i = 1; i <= 20; i++) {
            playlistSongs.add(new Song("Song " + i, "Artist " + i, R.drawable.meowsic_black_icon));
        }

        // Setup RecyclerView
        songAdapter = new SongAdapter(playlistSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(songAdapter);
        
        // Navigate to Now Playing on song click
        songAdapter.setOnSongClickListener(song -> {
            if (song == null || getActivity() == null) return;
            fragment_now_playing target = fragment_now_playing.newInstance(song.getTitle(), song.getArtist());
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, target)
                    .addToBackStack(null)
                    .commit();
        });
    }    

    private void setupSearchFragment() {
        // Create search fragment
        searchFragment = new fragment_search();

        searchFragment.setSearchResultCallback(new fragment_search.SearchResultCallback() {
            @Override
            public void onSearchResultSelected(fragment_search.SearchResult result) {
                // No navigation from dropdown; keep list filtering only
            }

            @Override
            public void onSearchQueryChanged(String query) {
                if (songAdapter == null) return;
                if (query == null || query.trim().isEmpty()) {
                    songAdapter.restoreFullList();
                } else {
                    songAdapter.filterByQuery(query);
                }
            }

            @Override
            public List<fragment_search.SearchResult> getSearchableData() {
                List<fragment_search.SearchResult> results = new ArrayList<>();
                for (Song s : playlistSongs) {
                    results.add(new fragment_search.SearchResult(s.getTitle(), s.getArtist(), "song"));
                }
                return results;
            }
        });

        // Attach search fragment into container
        getChildFragmentManager().beginTransaction()
            .replace(R.id.search_container, searchFragment)
            .commit();
    }
}