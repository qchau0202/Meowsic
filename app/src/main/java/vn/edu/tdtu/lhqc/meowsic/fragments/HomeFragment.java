package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.managers.PlaybackManager;
import vn.edu.tdtu.lhqc.meowsic.models.Song;
import vn.edu.tdtu.lhqc.meowsic.adapters.SongAdapter;
import vn.edu.tdtu.lhqc.meowsic.managers.RecentlyPlayedStore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Search fragment
    private SearchFragment searchFragment;
    
    // Recently played
    private View recentlyPlayedSection;
    private RecyclerView recyclerRecentlyPlayed;
    private SongAdapter recentlyPlayedAdapter;
    private android.widget.ImageView btnClearRecent;
    private android.widget.TextView recentlyPlayedEmpty;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_home.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize views
        initializeViews(view);
        
        // Setup search functionality
        setupSearchFragment();
        setupRecentlyPlayed();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh recently played when returning to home
        loadRecentlyPlayed();
    }
    
    private void initializeViews(View view) {
        recentlyPlayedSection = view.findViewById(R.id.recently_played_section);
        recyclerRecentlyPlayed = view.findViewById(R.id.recycler_recently_played);
        btnClearRecent = view.findViewById(R.id.btn_clear_recent);
        recentlyPlayedEmpty = view.findViewById(R.id.recently_played_empty);
    }

    private void setupSearchFragment() {
        // Create search fragment
        searchFragment = new SearchFragment();
        
        // Set callback for search results
        searchFragment.setSearchResultCallback(new SearchFragment.SearchResultCallback() {
            @Override
            public void onSearchResultSelected(SearchFragment.SearchResult result) {
                // Handle search result selection in home
                handleHomeSearchResult(result);
            }
            
            @Override
            public void onSearchQueryChanged(String query) {
                // Handle query change if needed
            }
            
            @Override
            public List<SearchFragment.SearchResult> getSearchableData() {
                // Return the existing home data as searchable results
                return getHomeSearchableData();
            }
        });
        
        // Replace the search container in home layout with the search fragment
        getChildFragmentManager().beginTransaction()
            .replace(R.id.search_container, searchFragment)
            .commit();
    }
    
    private List<SearchFragment.SearchResult> getHomeSearchableData() {
        // Convert existing home data to searchable format
        List<SearchFragment.SearchResult> searchableData = new ArrayList<>();
        
        // Add the collection items from home
        searchableData.add(new SearchFragment.SearchResult("Meowsic Hits", "Various Artists", "album"));
        searchableData.add(new SearchFragment.SearchResult("Cat Vibes", "Feline Artists", "album"));
        searchableData.add(new SearchFragment.SearchResult("Melody Mix", "Music Producers", "album"));
        searchableData.add(new SearchFragment.SearchResult("Purr-fect Beats", "DJ Meowsic", "album"));
        
        // Add category cards
        searchableData.add(new SearchFragment.SearchResult("Top 100 cat songs", "Various Artists", "playlist"));
        searchableData.add(new SearchFragment.SearchResult("Meowsic newest album", "Various Artists", "album"));
        
        return searchableData;
    }
    
    private void handleHomeSearchResult(SearchFragment.SearchResult result) {
        // Handle search result selection in home context
        switch (result.getType()) {
            case "playlist":
                // Navigate to playlist screen or show playlist content
                break;
            case "artist":
                // Navigate to artist screen or show artist content
                break;
            case "album":
                // Navigate to album screen or show album content
                break;
            case "song":
                // Navigate to song or start playing
                break;
        }
    }
    
    private void setupRecentlyPlayed() {
        if (recyclerRecentlyPlayed == null) return;
        
        // Setup RecyclerView
        recyclerRecentlyPlayed.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Setup clear button
        if (btnClearRecent != null) {
            btnClearRecent.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Clear Recently Played")
                    .setMessage("Are you sure you want to clear your listening history?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        RecentlyPlayedStore.clear(requireContext());
                        loadRecentlyPlayed();
                        android.widget.Toast.makeText(requireContext(), 
                            "Recently played cleared", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });
        }
        
        // Load initial data
        loadRecentlyPlayed();
    }
    
    private void loadRecentlyPlayed() {
        if (recyclerRecentlyPlayed == null || recentlyPlayedSection == null || recentlyPlayedEmpty == null) return;
        
        List<Song> recentSongs = RecentlyPlayedStore.load(requireContext());
        
        // Always show the section
        recentlyPlayedSection.setVisibility(View.VISIBLE);
        
        if (recentSongs.isEmpty()) {
            // Show empty state message, hide RecyclerView and clear button
            recentlyPlayedEmpty.setVisibility(View.VISIBLE);
            recyclerRecentlyPlayed.setVisibility(View.GONE);
            if (btnClearRecent != null) {
                btnClearRecent.setVisibility(View.GONE);
            }
        } else {
            // Show RecyclerView and clear button, hide empty message
            recentlyPlayedEmpty.setVisibility(View.GONE);
            recyclerRecentlyPlayed.setVisibility(View.VISIBLE);
            if (btnClearRecent != null) {
                btnClearRecent.setVisibility(View.VISIBLE);
            }
            
            // Always recreate adapter to avoid state issues
            recentlyPlayedAdapter = new SongAdapter(recentSongs);
            recyclerRecentlyPlayed.setAdapter(recentlyPlayedAdapter);
            
            // Set click listener to play song
            recentlyPlayedAdapter.setOnSongClickListener(song -> {
                if (song == null || getActivity() == null) return;
                if (song.getUriString() != null) {
                    // Start playback via shared manager
                    try {
                        android.net.Uri uri = android.net.Uri.parse(song.getUriString());
                        PlaybackManager.get().play(
                            requireContext(), uri, song.getTitle(), song.getArtist(), song.getAlbumArtBase64()
                        );
                    } catch (Exception ignored) {}
                    
                    // Navigate to now playing
                    NowPlayingFragment target = NowPlayingFragment.newInstance(
                        song.getTitle(), song.getArtist(), song.getUriString()
                    );
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, target)
                        .addToBackStack(null)
                        .commit();
                }
            });
        }
    }
}