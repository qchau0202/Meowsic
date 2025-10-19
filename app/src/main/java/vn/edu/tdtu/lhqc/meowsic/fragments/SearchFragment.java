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

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;

public class SearchFragment extends Fragment {
    
    // Search related views and variables
    private EditText searchInput;
    private LinearLayout searchDropdown;
    private TextView searchNoResults;
    private ListView searchResultsList;
    private Handler searchHandler;
    private Runnable searchRunnable;
    
    // Search callback
    private SearchResultCallback searchResultCallback;
    
    // Interface for handling search result callbacks
    public interface SearchResultCallback {
        void onSearchResultSelected(SearchResult result);
        void onSearchQueryChanged(String query);
        List<SearchResult> getSearchableData(); // Method to get existing data for searching
    }
    
    public SearchFragment() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        initializeViews(view);
        setupSearchFunctionality();
        
        return view;
    }
    
    private void initializeViews(View view) {
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
                
                // Notify callback about query change
                if (searchResultCallback != null) {
                    searchResultCallback.onSearchQueryChanged(query);
                }
            }
        });
    }
    
    private void performSearch(String query) {
        if (searchResultCallback == null) {
            showNoResults();
            return;
        }
        
        // Get searchable data from the calling fragment
        List<SearchResult> searchableData = searchResultCallback.getSearchableData();
        if (searchableData == null || searchableData.isEmpty()) {
            showNoResults();
            return;
        }
        
        List<SearchResult> results = new ArrayList<>();
        
        // Simple search logic - filter results based on query
        for (SearchResult item : searchableData) {
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
        
        // Notify callback about result selection
        if (searchResultCallback != null) {
            searchResultCallback.onSearchResultSelected(result);
        }
    }
    
    // Public methods for external control
    public void setSearchResultCallback(SearchResultCallback callback) {
        this.searchResultCallback = callback;
    }
    
    public void clearSearch() {
        if (searchInput != null) {
            searchInput.setText("");
        }
        hideSearchDropdown();
    }
    
    public String getCurrentQuery() {
        return searchInput != null ? searchInput.getText().toString().trim() : "";
    }
    
    // Simple data class for search results
    public static class SearchResult {
        private String title;
        private String subtitle;
        private String type;
        private String uriString;
        private String albumArtBase64;
        private int imageRes;
        
        public SearchResult(String title, String subtitle, String type) {
            this(title, subtitle, type, null, null, R.drawable.ic_library_music_24px);
        }
        
        public SearchResult(String title, String subtitle, String type, String uriString, String albumArtBase64, int imageRes) {
            this.title = title;
            this.subtitle = subtitle;
            this.type = type;
            this.uriString = uriString;
            this.albumArtBase64 = albumArtBase64;
            this.imageRes = imageRes;
        }
        
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public String getType() { return type; }
        public String getUriString() { return uriString; }
        public String getAlbumArtBase64() { return albumArtBase64; }
        public int getImageRes() { return imageRes; }
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
                view = inflater.inflate(R.layout.library_item, parent, false);
            }
            
            SearchResult result = results.get(position);
            
            // Disable clickable/focusable on the root view to allow ListView to handle clicks
            view.setClickable(false);
            view.setFocusable(false);
            
            ImageView imageView = view.findViewById(R.id.imageItem);
            TextView title = view.findViewById(R.id.textTitle);
            TextView subtitle = view.findViewById(R.id.textType);
            ImageView arrowIcon = view.findViewById(R.id.arrow_icon);
            
            title.setText(result.getTitle());
            subtitle.setText(result.getSubtitle());
            
            // Load album art if available, otherwise use default image
            if (result.getAlbumArtBase64() != null && !result.getAlbumArtBase64().isEmpty()) {
                try {
                    byte[] decodedBytes = android.util.Base64.decode(result.getAlbumArtBase64(), android.util.Base64.DEFAULT);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    imageView.setImageResource(result.getImageRes());
                }
            } else {
                imageView.setImageResource(result.getImageRes());
            }
            
            // Show/hide arrow based on type (playlists show arrow, songs don't)
            if (arrowIcon != null) {
                arrowIcon.setVisibility(result.getType().equalsIgnoreCase("Playlist") ? View.VISIBLE : View.GONE);
            }
            
            return view;
        }
    }
}