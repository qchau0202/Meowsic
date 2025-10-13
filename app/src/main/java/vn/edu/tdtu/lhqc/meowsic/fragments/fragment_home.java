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

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;

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

    // Search related views and variables
    private EditText searchInput;
    private LinearLayout searchDropdown;
    private TextView searchNoResults;
    private ListView searchResultsList;
    private Handler searchHandler;
    private Runnable searchRunnable;
    
    // Sample search data
    private List<SearchResult> searchData;

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
        
        // Initialize search data
        initializeSearchData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize search functionality
        initializeSearchViews(view);
        setupSearchFunctionality();
        
        return view;
    }
    
    private void initializeSearchData() {
        searchData = new ArrayList<>();
        searchData.add(new SearchResult("Meowsic Hits", "Various Artists", "album"));
        searchData.add(new SearchResult("Cat Vibes", "Feline Artists", "album"));
        searchData.add(new SearchResult("Melody Mix", "Music Producers", "album"));
        searchData.add(new SearchResult("Purr-fect Beats", "DJ Meowsic", "album"));
        searchData.add(new SearchResult("Chill Cat", "Ambient Cats", "artist"));
        searchData.add(new SearchResult("Jazz Meows", "Smooth Jazz Cats", "artist"));
        searchData.add(new SearchResult("Rock & Roll Cat", "Heavy Metal Cats", "artist"));
        searchData.add(new SearchResult("Electronic Beats", "Digital Cats", "playlist"));
        searchData.add(new SearchResult("Classical Meows", "Orchestral Cats", "playlist"));
        searchData.add(new SearchResult("Pop Cat Hits", "Mainstream Cats", "playlist"));
    }
    
    private void initializeSearchViews(View view) {
        searchInput = view.findViewById(R.id.search_input);
        searchDropdown = view.findViewById(R.id.search_dropdown);
        searchNoResults = view.findViewById(R.id.search_no_results);
        searchResultsList = view.findViewById(R.id.search_results_list);
        
        searchHandler = new Handler(Looper.getMainLooper());
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
    
    private void performSearch(String query) {
        List<SearchResult> results = new ArrayList<>();
        
        // Simple search logic - filter results based on query
        for (SearchResult item : searchData) {
            if (item.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                item.getSubtitle().toLowerCase().contains(query.toLowerCase())) {
                results.add(item);
            }
        }
        
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
                // Handle search result selection
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
        
        // Here you can navigate to the selected result or perform any action
        // For now, we'll just show a simple action
        // You can implement navigation logic here
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
        }
        
        @Override
        public int getCount() {
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