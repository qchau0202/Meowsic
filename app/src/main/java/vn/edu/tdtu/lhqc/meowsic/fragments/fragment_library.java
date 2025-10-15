package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.Song;
import vn.edu.tdtu.lhqc.meowsic.SongAdapter;
import vn.edu.tdtu.lhqc.meowsic.ui.PopupAddMenuHelper;
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_playlist;

public class fragment_library extends Fragment {
    
    // Filter buttons
    private MaterialButton btnPlaylist;
    private MaterialButton btnArtist;
    private MaterialButton btnAlbum;
    private String currentSelectedType = null; // null => All (no filter)
    
    // RecyclerView and adapter
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    
    // Search fragment and data
    private fragment_search searchFragment;
    private List<Song> allLibraryData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Initialize views
        initializeViews(view);
        setupFilterButtons();
        setupRecyclerView();
        setupSearchFragment();
        setupAddMenu(view);

        // Start with no tab selected, show all
        if (songAdapter != null) songAdapter.restoreFullList();
        resetButtonsToInactive();
        currentSelectedType = null;

        return view;
    }

    private void setupAddMenu(View root) {
        View add = root.findViewById(R.id.btn_add_library);
        if (add != null) {
            add.setOnClickListener(v -> PopupAddMenuHelper.show(requireContext(), v, new PopupAddMenuHelper.Listener() {
                @Override
                public void onCreatePlaylistSelected() {
                    // TODO: Navigate to create playlist screen or show dialog
                }

                @Override
                public void onImportMusicSelected() {
                    // TODO: Launch file picker for mp3/mp4 import
                }
            }));
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Restore list; keep current selection (or none)
        if (songAdapter != null) songAdapter.restoreFullList();
        if (currentSelectedType == null) {
            resetButtonsToInactive();
        } else {
            updateFilterButtonStates(currentSelectedType);
            if (songAdapter != null) songAdapter.filterByType(currentSelectedType);
        }
    }
    
    private void initializeViews(View view) {
        // Filter buttons
        btnPlaylist = view.findViewById(R.id.btn_playlist);
        btnArtist = view.findViewById(R.id.btn_artist);
        btnAlbum = view.findViewById(R.id.btn_album);
        
        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewSongs);
    }
    
    private void setupFilterButtons() {
        btnPlaylist.setOnClickListener(v -> {
            if ("Playlist".equals(currentSelectedType)) {
                // Toggle off -> show all
                currentSelectedType = null;
                resetButtonsToInactive();
                if (songAdapter != null) songAdapter.restoreFullList();
            } else {
                currentSelectedType = "Playlist";
                updateFilterButtonStates("Playlist");
                if (songAdapter != null) songAdapter.filterByType("Playlist");
            }
        });

        btnArtist.setOnClickListener(v -> {
            if ("Artist".equals(currentSelectedType)) {
                currentSelectedType = null;
                resetButtonsToInactive();
                if (songAdapter != null) songAdapter.restoreFullList();
            } else {
                currentSelectedType = "Artist";
                updateFilterButtonStates("Artist");
                if (songAdapter != null) songAdapter.filterByType("Artist");
            }
        });

        btnAlbum.setOnClickListener(v -> {
            if ("Album".equals(currentSelectedType)) {
                currentSelectedType = null;
                resetButtonsToInactive();
                if (songAdapter != null) songAdapter.restoreFullList();
            } else {
                currentSelectedType = "Album";
                updateFilterButtonStates("Album");
                if (songAdapter != null) songAdapter.filterByType("Album");
            }
        });
    }
    
    private void updateFilterButtonStates(String selectedType) {
        // Reset all buttons to unselected state
        btnPlaylist.setBackgroundTintList(getContext().getColorStateList(R.color.card_shadow_bg));
        btnPlaylist.setTextColor(getContext().getColor(R.color.primary_black));
        btnArtist.setBackgroundTintList(getContext().getColorStateList(R.color.card_shadow_bg));
        btnArtist.setTextColor(getContext().getColor(R.color.primary_black));
        btnAlbum.setBackgroundTintList(getContext().getColorStateList(R.color.card_shadow_bg));
        btnAlbum.setTextColor(getContext().getColor(R.color.primary_black));
        // Set selected button
        switch (selectedType) {             
            case "Playlist":
                btnPlaylist.setBackgroundTintList(getContext().getColorStateList(R.color.primary_pink));
                btnPlaylist.setTextColor(getContext().getColor(R.color.primary_white));
                break;
            case "Artist":
                btnArtist.setBackgroundTintList(getContext().getColorStateList(R.color.primary_pink));
                btnArtist.setTextColor(getContext().getColor(R.color.primary_white));
                break;
            case "Album":
                btnAlbum.setBackgroundTintList(getContext().getColorStateList(R.color.primary_pink));
                btnAlbum.setTextColor(getContext().getColor(R.color.primary_white));
                break;
        }
    }

    private void resetButtonsToInactive() {
        btnPlaylist.setBackgroundTintList(getContext().getColorStateList(R.color.card_shadow_bg));
        btnPlaylist.setTextColor(getContext().getColor(R.color.primary_black));
        btnArtist.setBackgroundTintList(getContext().getColorStateList(R.color.card_shadow_bg));
        btnArtist.setTextColor(getContext().getColor(R.color.primary_black));
        btnAlbum.setBackgroundTintList(getContext().getColorStateList(R.color.card_shadow_bg));
        btnAlbum.setTextColor(getContext().getColor(R.color.primary_black));
    }
    
    private void setupRecyclerView() {
        // Sample data for RecyclerView
        allLibraryData = new ArrayList<>();
        allLibraryData.add(new Song("Top Hits 2021", "Various Artists", "Playlist", R.drawable.meowsic_black_icon));
        allLibraryData.add(new Song("Top Hits 2022", "Various Artists", "Playlist", R.drawable.meowsic_black_icon));
        allLibraryData.add(new Song("Top Hits 2023", "Various Artists", "Playlist", R.drawable.meowsic_black_icon));
        allLibraryData.add(new Song("Top Hits 2024", "Various Artists", "Playlist", R.drawable.meowsic_black_icon));
        allLibraryData.add(new Song("Top Hits 2025", "Various Artists", "Playlist", R.drawable.meowsic_black_icon));
        allLibraryData.add(new Song("Sơn Tùng MTP Playlist", "Sơn Tùng MTP", "Playlist", R.drawable.sontung));
        allLibraryData.add(new Song("Sky Tour", "Sơn Tùng MTP", "Album", R.drawable.skytour));
        allLibraryData.add(new Song("Ai", "tlinh", "Album", R.drawable.ai));
        allLibraryData.add(new Song("G-Dragon Playlist", "G-Dragon", "Playlist", R.drawable.gdragon));

        // Setup RecyclerView
        songAdapter = new SongAdapter(allLibraryData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(songAdapter);

        // Navigate to playlist view when a playlist item is clicked
        songAdapter.setOnSongClickListener(song -> {
            if (song == null || getActivity() == null) return;
            String type = song.getType();
            if ("Playlist".equalsIgnoreCase(type)) {
                fragment_playlist target = fragment_playlist.newInstance(song.getTitle(), song.getArtist());
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, target)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
    
    private void setupSearchFragment() {
        // Create search fragment
        searchFragment = new fragment_search();
        
        // Set callback for search results
        searchFragment.setSearchResultCallback(new fragment_search.SearchResultCallback() {
            @Override
            public void onSearchResultSelected(fragment_search.SearchResult result) {
                // Keep dropdown behavior only; do not filter the RecyclerView here
            }
            
            @Override
            public void onSearchQueryChanged(String query) {
                // Do not live-filter the RecyclerView on query change
            }
            
            @Override
            public List<fragment_search.SearchResult> getSearchableData() {
                // Return the existing library data as searchable results
                return getLibrarySearchableData();
            }
        });
        
        // Replace the search container in library layout with the search fragment
        getChildFragmentManager().beginTransaction()
            .replace(R.id.search_container, searchFragment)
            .commit();
    }
    
    private List<fragment_search.SearchResult> getLibrarySearchableData() {
        // Convert existing library Song data to searchable format
        List<fragment_search.SearchResult> searchableData = new ArrayList<>();
        
        if (allLibraryData != null) {
            for (Song song : allLibraryData) {
                searchableData.add(new fragment_search.SearchResult(
                    song.getTitle(),
                    song.getArtist(),
                    song.getType().toLowerCase()
                ));
            }
        }
        
        return searchableData;
    }
    
    private void handleLibrarySearchResult(fragment_search.SearchResult result) {
        // No-op: keep dropdown behavior only; do not alter the RecyclerView list
    }
}