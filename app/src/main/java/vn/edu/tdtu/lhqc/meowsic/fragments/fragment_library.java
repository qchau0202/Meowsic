package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

public class fragment_library extends Fragment {

    // Search related views and variables
    private EditText searchInput;
    private LinearLayout searchDropdown;
    private TextView searchNoResults;
    private ListView searchResultsList;
    private Handler searchHandler;
    private Runnable searchRunnable;
    
    // Filter buttons
    private MaterialButton btnPlaylist;
    private MaterialButton btnArtist;
    private MaterialButton btnAlbum;
    
    // RecyclerView and adapter
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    
    // Sample search data
    private List<SearchResult> searchData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Initialize views
        initializeViews(view);
        initializeSearchData();
        setupSearchFunctionality();
        setupFilterButtons();
        setupRecyclerView();

        return view;
    }
    
    private void initializeViews(View view) {
        // Search views
        searchInput = view.findViewById(R.id.search_input);
        searchDropdown = view.findViewById(R.id.search_dropdown);
        searchNoResults = view.findViewById(R.id.search_no_results);
        searchResultsList = view.findViewById(R.id.search_results_list);
        searchHandler = new Handler(Looper.getMainLooper());
        
        // Filter buttons
        btnPlaylist = view.findViewById(R.id.btn_playlist);
        btnArtist = view.findViewById(R.id.btn_artist);
        btnAlbum = view.findViewById(R.id.btn_album);
        
        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewSongs);
    }
    
    private void initializeSearchData() {
        searchData = new ArrayList<>();
        searchData.add(new SearchResult("Top Hits 2023", "Playlist", "playlist"));
        searchData.add(new SearchResult("Top Hits 2024", "Playlist", "playlist"));
        searchData.add(new SearchResult("Sơn Tùng MTP", "Artist", "artist"));
        searchData.add(new SearchResult("Sky Tour", "Album", "album"));
        searchData.add(new SearchResult("Ai", "Album", "album"));
        searchData.add(new SearchResult("G-Dragon", "Artist", "artist"));
        searchData.add(new SearchResult("Motivation Mix", "Playlist", "playlist"));
        searchData.add(new SearchResult("Meowsic Hits", "Various Artists", "playlist"));
        searchData.add(new SearchResult("Cat Vibes", "Feline Artists", "album"));
        searchData.add(new SearchResult("Melody Mix", "Music Producers", "playlist"));
        searchData.add(new SearchResult("Purr-fect Beats", "DJ Meowsic", "album"));
        searchData.add(new SearchResult("Best of 2023", "Playlist", "playlist"));
        searchData.add(new SearchResult("Chill Vibes", "Playlist", "playlist"));
        searchData.add(new SearchResult("Pop Hits", "Playlist", "playlist"));
        searchData.add(new SearchResult("Rock Classics", "Album", "album"));
        searchData.add(new SearchResult("Jazz Collection", "Album", "album"));
        searchData.add(new SearchResult("Electronic Music", "Album", "album"));
        searchData.add(new SearchResult("Classical Masters", "Artist", "artist"));
        searchData.add(new SearchResult("Indie Artists", "Artist", "artist"));
        searchData.add(new SearchResult("Hip Hop Legends", "Artist", "artist"));
    }
    
    private void setupSearchFunctionality() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                
                // Cancel previous search if exists
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                if (query.isEmpty()) {
                    hideSearchDropdown();
                } else {
                    // Delay search to avoid too frequent calls
                    searchRunnable = () -> performSearch(query);
                    searchHandler.postDelayed(searchRunnable, 500);
                }
            }
        });
    }
    
    private void setupFilterButtons() {
        // Set initial state - Playlist selected
        updateFilterButtonStates("Playlist");
        
        btnPlaylist.setOnClickListener(v -> {
            updateFilterButtonStates("Playlist");
            songAdapter.filterByType("Playlist");
        });
        
        btnArtist.setOnClickListener(v -> {
            updateFilterButtonStates("Artist");
            songAdapter.filterByType("Artist");
        });
        
        btnAlbum.setOnClickListener(v -> {
            updateFilterButtonStates("Album");
            songAdapter.filterByType("Album");
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
                btnPlaylist.setTextColor(getContext().getResources().getColor(R.color.primary_white));
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
    
    private void setupRecyclerView() {
        // Sample data for RecyclerView
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("Top Hits 2023", "Playlist", R.drawable.meowsic_black_icon));
        songs.add(new Song("Top Hits 2024", "Playlist", R.drawable.meowsic_black_icon));
        songs.add(new Song("Sơn Tùng MTP", "Artist", R.drawable.meowsic_black_icon));
        songs.add(new Song("Sky Tour", "Album", R.drawable.meowsic_black_icon));
        songs.add(new Song("Ai", "Album", R.drawable.meowsic_black_icon));
        songs.add(new Song("G-Dragon", "Artist", R.drawable.meowsic_black_icon));
        songs.add(new Song("Motivation Mix", "Playlist", R.drawable.meowsic_black_icon));
        songs.add(new Song("Meowsic Hits", "Various Artists", R.drawable.meowsic_black_icon));
        songs.add(new Song("Cat Vibes", "Feline Artists", R.drawable.meowsic_black_icon));
        songs.add(new Song("Melody Mix", "Music Producers", R.drawable.meowsic_black_icon));
        songs.add(new Song("Purr-fect Beats", "DJ Meowsic", R.drawable.meowsic_black_icon));
        songs.add(new Song("Best of 2023", "Playlist", R.drawable.meowsic_black_icon));
        songs.add(new Song("Chill Vibes", "Playlist", R.drawable.meowsic_black_icon));
        songs.add(new Song("Pop Hits", "Playlist", R.drawable.meowsic_black_icon));
        songs.add(new Song("Rock Classics", "Album", R.drawable.meowsic_black_icon));
        songs.add(new Song("Jazz Collection", "Album", R.drawable.meowsic_black_icon));
        songs.add(new Song("Electronic Music", "Album", R.drawable.meowsic_black_icon));
        songs.add(new Song("Classical Masters", "Artist", R.drawable.meowsic_black_icon));
        songs.add(new Song("Indie Artists", "Artist", R.drawable.meowsic_black_icon));
        songs.add(new Song("Hip Hop Legends", "Artist", R.drawable.meowsic_black_icon));

        // Setup RecyclerView
        songAdapter = new SongAdapter(songs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(songAdapter);
    }
    
    private void performSearch(String query) {
        List<SearchResult> results = new ArrayList<>();
        
        // Simple search logic - filter results based on query
        for (SearchResult item : searchData) {
            if (item.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                item.getSubtitle().toLowerCase().contains(query.toLowerCase())) {
                results.add(item);
            }
        }
        
        // Debug: Log the number of results found
        android.util.Log.d("LibrarySearch", "Query: '" + query + "', Results found: " + results.size());
        
        if (results.isEmpty()) {
            showNoResults();
        } else {
            showSearchResults(results);
        }
    }
    
    private void showSearchResults(List<SearchResult> results) {
        searchDropdown.setVisibility(View.VISIBLE);
        searchResultsList.setVisibility(View.VISIBLE);
        searchNoResults.setVisibility(View.GONE);
        
        // Create custom adapter for search results
        SearchResultAdapter adapter = new SearchResultAdapter(getContext(), results);
        searchResultsList.setAdapter(adapter);
        
        // Handle item clicks
        searchResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchResult selectedResult = results.get(position);
                onSearchResultSelected(selectedResult);
            }
        });
    }
    
    private void showNoResults() {
        searchDropdown.setVisibility(View.VISIBLE);
        searchResultsList.setVisibility(View.GONE);
        searchNoResults.setVisibility(View.VISIBLE);
    }
    
    private void hideSearchDropdown() {
        searchDropdown.setVisibility(View.GONE);
    }
    
    private void onSearchResultSelected(SearchResult result) {
        // Hide search dropdown
        hideSearchDropdown();
        
        // Clear search input
        searchInput.setText("");
        
        // Filter RecyclerView based on selected result type
        songAdapter.filterByType(result.getType());
        updateFilterButtonStates(result.getType());
    }
    
    // Simple data class for search results
    public static class SearchResult {
        private String title;
        private String subtitle;
        private String type;
        
        public SearchResult(String title, String subtitle, String type) {
            this.title = title;
            this.subtitle = subtitle;
            this.type = type;
        }
        
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public String getType() { return type; }
    }
    
    // Custom adapter for search results
    private class SearchResultAdapter extends BaseAdapter {
        private Context context;
        private List<SearchResult> results;
        
        public SearchResultAdapter(Context context, List<SearchResult> results) {
            this.context = context;
            this.results = results;
            android.util.Log.d("SearchAdapter", "Adapter created with " + results.size() + " items");
        }
        
        @Override
        public int getCount() {
            android.util.Log.d("SearchAdapter", "getCount() called, returning " + results.size());
            return results.size();
        }
        
        @Override
        public Object getItem(int position) {
            return results.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.search_result_item, parent, false);
            }
            
            SearchResult result = results.get(position);
            
            ImageView icon = view.findViewById(R.id.result_icon);
            TextView title = view.findViewById(R.id.result_title);
            TextView subtitle = view.findViewById(R.id.result_subtitle);
            
            title.setText(result.getTitle());
            subtitle.setText(result.getSubtitle());
            
            // Set appropriate icon based on type
            switch (result.getType()) {
                case "album":
                    icon.setImageResource(R.drawable.ic_library);
                    break;
                case "artist":
                    icon.setImageResource(R.drawable.ic_profile);
                    break;
                case "playlist":
                    icon.setImageResource(R.drawable.ic_home);
                    break;
                default:
                    icon.setImageResource(R.drawable.ic_library);
                    break;
            }
            
            return view;
        }
    }
}