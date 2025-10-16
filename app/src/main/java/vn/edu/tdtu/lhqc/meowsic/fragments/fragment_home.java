package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import vn.edu.tdtu.lhqc.meowsic.ui.PopupAddMenuHelper;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.Song;
import vn.edu.tdtu.lhqc.meowsic.SongAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_home extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Search fragment
    private fragment_search searchFragment;
    // (Songs section moved to Library)

    public fragment_home() {
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
    public static fragment_home newInstance(String param1, String param2) {
        fragment_home fragment = new fragment_home();
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
        
        // Setup search functionality
        setupSearchFragment();
        setupAddMenu(view);
        // Songs list is shown in Library screen
        
        return view;
    }

    private void setupAddMenu(View root) {
        View add = root.findViewById(R.id.btn_add_home);
        if (add != null) {
            add.setOnClickListener(v -> PopupAddMenuHelper.show(requireContext(), new PopupAddMenuHelper.Listener() {
                @Override
                public void onCreatePlaylistSelected() {
                    // TODO: Navigate to create playlist screen or show dialog
                }

                @Override
                public void onImportMusicSelected() {

                }
            }));
        }
    }
    
    // (Songs section setup removed; handled in Library)

    private void setupSearchFragment() {
        // Create search fragment
        searchFragment = new fragment_search();
        
        // Set callback for search results
        searchFragment.setSearchResultCallback(new fragment_search.SearchResultCallback() {
            @Override
            public void onSearchResultSelected(fragment_search.SearchResult result) {
                // Handle search result selection in home
                handleHomeSearchResult(result);
            }
            
            @Override
            public void onSearchQueryChanged(String query) {
                // Handle query change if needed
            }
            
            @Override
            public List<fragment_search.SearchResult> getSearchableData() {
                // Return the existing home data as searchable results
                return getHomeSearchableData();
            }
        });
        
        // Replace the search container in home layout with the search fragment
        getChildFragmentManager().beginTransaction()
            .replace(R.id.search_container, searchFragment)
            .commit();
    }
    
    private List<fragment_search.SearchResult> getHomeSearchableData() {
        // Convert existing home data to searchable format
        List<fragment_search.SearchResult> searchableData = new ArrayList<>();
        
        // Add the collection items from home
        searchableData.add(new fragment_search.SearchResult("Meowsic Hits", "Various Artists", "album"));
        searchableData.add(new fragment_search.SearchResult("Cat Vibes", "Feline Artists", "album"));
        searchableData.add(new fragment_search.SearchResult("Melody Mix", "Music Producers", "album"));
        searchableData.add(new fragment_search.SearchResult("Purr-fect Beats", "DJ Meowsic", "album"));
        
        // Add category cards
        searchableData.add(new fragment_search.SearchResult("Top 100 cat songs", "Various Artists", "playlist"));
        searchableData.add(new fragment_search.SearchResult("Meowsic newest album", "Various Artists", "album"));
        
        return searchableData;
    }
    
    private void handleHomeSearchResult(fragment_search.SearchResult result) {
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
}