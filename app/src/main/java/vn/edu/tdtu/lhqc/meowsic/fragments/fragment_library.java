package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.Song;
import vn.edu.tdtu.lhqc.meowsic.SongAdapter;
import vn.edu.tdtu.lhqc.meowsic.ui.PopupAddMenuHelper;

public class fragment_library extends Fragment {
    
    // Filter buttons
    private MaterialButton btnPlaylist;
    private MaterialButton btnArtist;
    private MaterialButton btnAlbum;
    private String currentSelectedType = null; // null => All (no filter)

    private ImageButton btnToggleView;
    private ImageButton btnSort;
    // RecyclerView and adapter
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private android.widget.TextView emptyState;
    
    // Search fragment and data
    private fragment_search searchFragment;
    private List<Song> allLibraryData;

    private boolean isGridMode = false;


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
        setupViewAndSortButtons();

        // Start with no tab selected, show all
        if (songAdapter != null) songAdapter.restoreFullList();
        resetButtonsToInactive();
        currentSelectedType = null;

        return view;
    }

    private void setupAddMenu(View root) {
        View add = root.findViewById(R.id.btn_add_library);
        if (add != null) {
            add.setOnClickListener(v -> PopupAddMenuHelper.show(requireContext(), new PopupAddMenuHelper.Listener() {
                @Override
                public void onCreatePlaylistSelected() {
                    // Hiển thị dialog nhập tên playlist
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
                    builder.setTitle("Create Playlist");

                    // Tạo ô nhập tên
                    final android.widget.EditText input = new android.widget.EditText(requireContext());
                    input.setHint("Enter playlist name");
                    input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    // Nút "Tạo"
                    builder.setPositiveButton("Create", (dialog, which) -> {
                        String playlistName = input.getText().toString().trim();
                        if (playlistName.isEmpty()) {
                            android.widget.Toast.makeText(requireContext(), "Please enter a playlist name", android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Tạo playlist mới (dùng class Song làm đại diện playlist)
                        Song newPlaylist = new Song(
                                playlistName,
                                "Playlist", // Artist field tạm dùng để hiển thị loại
                                R.drawable.playlist, // Icon playlist (tạo icon trong drawable)
                                null // Không có URI
                        );
                        newPlaylist.setType("Playlist");

                        // Lưu vào danh sách
                        allLibraryData.add(0, newPlaylist);
                        songAdapter.updateData(allLibraryData);
                        updateEmptyState();
                        vn.edu.tdtu.lhqc.meowsic.SongStore.addAtTop(requireContext(), newPlaylist);


                        // Thông báo
                        android.widget.Toast.makeText(requireContext(), "Playlist created", android.widget.Toast.LENGTH_SHORT).show();
                    });

                    // Nút "Hủy"
                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                    builder.show();
                }

                @Override
                public void onImportMusicSelected() {
                    // Launch system picker for audio files
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
                    intent.setType("audio/*");
                    intent.putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    startActivityForResult(intent, 1011);
                }
            }));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1011 && resultCode == android.app.Activity.RESULT_OK && data != null) {
            java.util.List<android.net.Uri> uris = new java.util.ArrayList<>();
            if (data.getData() != null) {
                uris.add(data.getData());
            } else if (data.getClipData() != null) {
                android.content.ClipData clip = data.getClipData();
                for (int i = 0; i < clip.getItemCount(); i++) {
                    uris.add(clip.getItemAt(i).getUri());
                }
            }
            // Take persistable permission and ingest
            List<Song> imported = new ArrayList<>();
            for (android.net.Uri uri : uris) {
                try {
                    int takeFlags = data.getFlags() & (android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION | android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
                // Extract metadata and add to list
                Song s = buildImportedSong(uri);
                if (s != null) imported.add(s);
            }
            // Persist to simple local store
            vn.edu.tdtu.lhqc.meowsic.SongStore.addAllAtTop(requireContext(), imported);
            if (songAdapter != null) {
                // Add newest first
                for (int i = imported.size() - 1; i >= 0; i--) {
                    songAdapter.addSong(imported.get(i));
                }
                allLibraryData.addAll(0, imported);
                updateEmptyState();
            } else {
                allLibraryData.addAll(0, imported);
                updateEmptyState();
            }
        }
    }

    private Song buildImportedSong(android.net.Uri uri) {
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            mmr.setDataSource(requireContext(), uri);
            String title = valueOr(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE), "Unknown");
            String artist = valueOr(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST), "Unknown");
            // For UI thumbnail fallback use app icon; album art could be extracted later
            return new vn.edu.tdtu.lhqc.meowsic.Song(title, artist, R.drawable.meowsic_black_icon, uri.toString());
        } catch (Exception ignored) {
            return null;
        } finally {
            try { mmr.release(); } catch (Exception ignored) {}
        }
    }

    private String valueOr(String s, String def) {
        if (s == null || s.trim().isEmpty()) return def;
        return s;
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
        emptyState = view.findViewById(R.id.empty_state);
        btnToggleView = view.findViewById(R.id.btn_toggle_view);
        btnSort = view.findViewById(R.id.btn_sort);
    }

    private void setupViewAndSortButtons() {
        if (btnToggleView != null) {
            btnToggleView.setOnClickListener(v -> toggleViewMode());
        }
        if (btnSort != null) {
            btnSort.setOnClickListener(v -> showSortMenu(v));
        }
    }

    private void toggleViewMode() {
        isGridMode = !isGridMode;
        if (isGridMode) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            btnToggleView.setImageResource(R.drawable.ic_list_view); // icon đổi
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            btnToggleView.setImageResource(R.drawable.ic_grid_view);
        }
        recyclerView.setAdapter(songAdapter);
    }

    // 🔹 Hiển thị menu sắp xếp
    private void showSortMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        popupMenu.getMenu().add("Sort A → Z");
        popupMenu.getMenu().add("Sort Z → A");
        popupMenu.getMenu().add("Sort by Date (Newest)");
        popupMenu.getMenu().add("Sort by Date (Oldest)");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.contains("A → Z")) {
                Collections.sort(allLibraryData, Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER));
            } else if (title.contains("Z → A")) {
                Collections.sort(allLibraryData, (a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle()));
            } else if (title.contains("Newest")) {
                Collections.sort(allLibraryData, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
            } else if (title.contains("Oldest")) {
                Collections.sort(allLibraryData, Comparator.comparingLong(Song::getCreatedAt));
            }
            songAdapter.updateData(allLibraryData);
            return true;
        });

        popupMenu.show();
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
        // Load persisted songs so list survives navigation/app restarts
        allLibraryData = new ArrayList<>(vn.edu.tdtu.lhqc.meowsic.SongStore.load(requireContext()));

        allLibraryData.add(new Song("Shape of You", "Ed Sheeran", R.drawable.ic_music_note_24px, "Pop"));
        allLibraryData.add(new Song("Blinding Lights", "The Weeknd", R.drawable.ic_music_note_24px, "Pop"));
        allLibraryData.add(new Song("Bohemian Rhapsody", "Queen", R.drawable.ic_music_note_24px, "Rock"));
        allLibraryData.add(new Song("Bad Guy", "Billie Eilish", R.drawable.ic_music_note_24px, "Alternative"));
        allLibraryData.add(new Song("Lose Yourself", "Eminem", R.drawable.ic_music_note_24px, "Hip-Hop"));

        vn.edu.tdtu.lhqc.meowsic.SongStore.save(requireContext(), allLibraryData);
        // Setup RecyclerView
        songAdapter = new SongAdapter(allLibraryData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(songAdapter);
        updateEmptyState();

        // Navigate: songs -> now playing; playlists -> playlist fragment
        songAdapter.setOnSongClickListener(song -> {
            if (song == null || getActivity() == null) return;
            String type = song.getType();
            if ("Playlist".equalsIgnoreCase(type)) {
                fragment_playlist target = fragment_playlist.newInstance(song.getTitle(), song.getArtist());
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, target)
                        .addToBackStack(null)
                        .commit();
            } else {
                // Pass URI when available to play the actual file
                fragment_now_playing target;
                if (song.getUriString() != null) {
                    // Start playback via shared manager so minimized player also reflects state
                    try {
                        android.net.Uri uri = android.net.Uri.parse(song.getUriString());
                        vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get().play(requireContext(), uri, song.getTitle(), song.getArtist());
                    } catch (Exception ignored) {}
                    target = fragment_now_playing.newInstance(song.getTitle(), song.getArtist(), song.getUriString());
                } else {
                    target = fragment_now_playing.newInstance(song.getTitle(), song.getArtist());
                }
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, target)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void updateEmptyState() {
        if (emptyState == null) return;
        emptyState.setVisibility((allLibraryData == null || allLibraryData.isEmpty()) ? View.VISIBLE : View.GONE);
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