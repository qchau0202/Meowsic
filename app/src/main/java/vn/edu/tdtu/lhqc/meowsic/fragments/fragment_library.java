package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

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
import java.util.Objects;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.Song;
import vn.edu.tdtu.lhqc.meowsic.SongAdapter;
import vn.edu.tdtu.lhqc.meowsic.ui.PopupAddMenuHelper;

public class fragment_library extends Fragment {
    
    // Filter buttons
    private MaterialButton btnAll;
    private MaterialButton btnPlaylist;
    private MaterialButton btnArtist;
    private String currentSelectedType = "All"; // "All" is default
    
    private ImageButton btnToggleView;
    private ImageButton btnSort;
    // RecyclerView and adapter
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private android.widget.TextView emptyState;
    private android.widget.TextView filterEmptyState;
    
    // Search fragment and data
    private fragment_search searchFragment;
    private List<Song> allLibraryData;

    private boolean isGridMode = false;
    private boolean isSelectionMode = false;
    private java.util.Set<Integer> selectedPositions = new java.util.HashSet<>();


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

        // Start with "All" tab selected
        if (songAdapter != null) songAdapter.restoreFullList();
        resetButtonsToInactive();
        setButtonActive(btnAll);
        currentSelectedType = "All";

        return view;
    }

    private void setupAddMenu(View root) {
        View add = root.findViewById(R.id.btn_add_library);
        if (add != null) {
            add.setOnClickListener(v -> PopupAddMenuHelper.show(requireContext(), new PopupAddMenuHelper.Listener() {
                @Override
                public void onCreatePlaylistSelected() {
                    showCreatePlaylistDialog();
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

                @Override
                public void onRemoveItemsSelected() {
                    showRemoveItemsDialog();
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
            
            // Get existing URIs to check for duplicates
            java.util.Set<String> existingUris = new java.util.HashSet<>();
            for (Song song : allLibraryData) {
                if (song.getUriString() != null) {
                    existingUris.add(song.getUriString());
                }
            }
            
            // Take persistable permission and ingest
            List<Song> imported = new ArrayList<>();
            for (android.net.Uri uri : uris) {
                String uriString = uri.toString();
                
                // Skip if this URI already exists in library
                if (existingUris.contains(uriString)) {
                    continue;
                }
                
                try {
                    int takeFlags = data.getFlags() & (android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION | android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
                // Extract metadata and add to list
                Song s = vn.edu.tdtu.lhqc.meowsic.SongImportUtil.buildSongFromUri(requireContext(), uri);
                if (s != null) {
                    imported.add(s);
                    existingUris.add(uriString); // Add to set to avoid duplicates in current import batch
                }
            }
            
            // Only persist and update UI if we have new songs
            if (!imported.isEmpty()) {
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
            } else {
                // Show message if all songs were duplicates
                android.widget.Toast.makeText(requireContext(), "Song(s) already in library", android.widget.Toast.LENGTH_SHORT).show();
            }
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
        btnAll = view.findViewById(R.id.btn_all);
        btnPlaylist = view.findViewById(R.id.btn_playlist);
        btnArtist = view.findViewById(R.id.btn_artist);
        
        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewSongs);
        emptyState = view.findViewById(R.id.empty_state);
        filterEmptyState = view.findViewById(R.id.filter_empty_state);
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
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            btnToggleView.setImageResource(R.drawable.ic_list_view_24px); // icon đổi
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            btnToggleView.setImageResource(R.drawable.ic_grid_view_24px);
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
        btnAll.setOnClickListener(v -> {
            currentSelectedType = "All";
            updateFilterButtonStates("All");
            if (songAdapter != null) songAdapter.restoreFullList();
            updateEmptyState();
        });
        
        btnPlaylist.setOnClickListener(v -> {
                currentSelectedType = "Playlist";
                updateFilterButtonStates("Playlist");
                if (songAdapter != null) songAdapter.filterByType("Playlist");
            updateEmptyState();
        });

        btnArtist.setOnClickListener(v -> {
                currentSelectedType = "Artist";
                updateFilterButtonStates("Artist");
                if (songAdapter != null) songAdapter.filterByType("Artist");
            updateEmptyState();
        });
    }
    
    private void updateFilterButtonStates(String selectedType) {
        resetButtonsToInactive();
        
        switch (selectedType) {             
            case "All":
                setButtonActive(btnAll);
                break;
            case "Playlist":
                setButtonActive(btnPlaylist);
                break;
            case "Artist":
                setButtonActive(btnArtist);
                break;
        }
    }

    private void resetButtonsToInactive() {
        setButtonInactive(btnAll);
        setButtonInactive(btnPlaylist);
        setButtonInactive(btnArtist);
    }
    
    private void setButtonActive(MaterialButton button) {
        if (button != null) {
            button.setBackgroundTintList(requireContext().getColorStateList(R.color.primary_pink));
            button.setTextColor(requireContext().getColor(R.color.primary_white));
        }
    }
    
    private void setButtonInactive(MaterialButton button) {
        if (button != null) {
            button.setBackgroundTintList(requireContext().getColorStateList(R.color.card_shadow_bg));
            button.setTextColor(requireContext().getColor(R.color.primary_black));
        }
    }
    
    private void setupRecyclerView() {
        // Load persisted songs so list survives navigation/app restarts
        allLibraryData = new ArrayList<>(vn.edu.tdtu.lhqc.meowsic.SongStore.load(requireContext()));

        // Setup RecyclerView
        songAdapter = new SongAdapter(allLibraryData);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                        vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get().play(requireContext(), uri, song.getTitle(), song.getArtist(), song.getAlbumArtBase64());
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
        
        // Handle more button clicks for songs
        songAdapter.setOnSongMoreClickListener((song, view) -> {
            showSongMenu(song, view);
        });
    }

    private void updateEmptyState() {
        if (emptyState == null || filterEmptyState == null || songAdapter == null) return;
        
        boolean isLibraryEmpty = (allLibraryData == null || allLibraryData.isEmpty());
        boolean isFilteredListEmpty = songAdapter.getItemCount() == 0;
        boolean hasFilter = currentSelectedType != null;
        
        // Show main empty state only if library is completely empty
        emptyState.setVisibility(isLibraryEmpty ? View.VISIBLE : View.GONE);
        
        // Show filter empty state if library has items but filter returns no results
        filterEmptyState.setVisibility((!isLibraryEmpty && isFilteredListEmpty && hasFilter) ? View.VISIBLE : View.GONE);
        
        // Hide RecyclerView if either empty state is showing
        if (recyclerView != null) {
            recyclerView.setVisibility((isLibraryEmpty || (isFilteredListEmpty && hasFilter)) ? View.GONE : View.VISIBLE);
        }
    }
    
    private void setupSearchFragment() {
        // Create search fragment
        searchFragment = new fragment_search();
        
        // Set callback for search results
        searchFragment.setSearchResultCallback(new fragment_search.SearchResultCallback() {
            @Override
            public void onSearchResultSelected(fragment_search.SearchResult result) {
                // Handle search result selection
                handleSearchResultClick(result);
            }
            
            @Override
            public void onSearchQueryChanged(String query) {
                // Query changes handled by search fragment
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
                    song.getType().toLowerCase(),
                    song.getUriString(),
                    song.getAlbumArtBase64(),
                    song.getImageRes()
                ));
            }
        }
        
        return searchableData;
    }
    
    private void handleSearchResultClick(fragment_search.SearchResult result) {
        // Find the actual song from library
        Song clickedSong = null;
        if (allLibraryData != null) {
            for (Song song : allLibraryData) {
                if (song.getTitle().equals(result.getTitle()) && 
                    song.getArtist().equals(result.getSubtitle())) {
                    clickedSong = song;
                    break;
                }
            }
        }
        
        if (clickedSong == null) return;
        
        // Handle based on type
        if (clickedSong.getType().equalsIgnoreCase("Playlist")) {
            // Navigate to playlist fragment
            fragment_playlist playlistFragment = fragment_playlist.newInstance(
                clickedSong.getTitle(),
                clickedSong.getArtist()
            );
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, playlistFragment)
                .addToBackStack(null)
                .commit();
        } else if (clickedSong.getUriString() != null) {
            // Play the song
            try {
                android.net.Uri uri = android.net.Uri.parse(clickedSong.getUriString());
                vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get().play(
                    requireContext(), 
                    uri, 
                    clickedSong.getTitle(), 
                    clickedSong.getArtist(),
                    clickedSong.getAlbumArtBase64()
                );
                
                // Navigate to now playing
                fragment_now_playing nowPlayingFragment = fragment_now_playing.newInstance(
                    clickedSong.getTitle(),
                    clickedSong.getArtist(),
                    clickedSong.getUriString()
                );
                requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, nowPlayingFragment)
                    .addToBackStack(null)
                    .commit();
            } catch (Exception e) {
                android.widget.Toast.makeText(requireContext(), 
                    "Failed to play song", 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showRemoveItemsDialog() {
        if (allLibraryData == null || allLibraryData.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "No items to remove", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog with item list and checkboxes
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Remove Items");

        // Create a scroll view with checkboxes
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        // Add "Select All" checkbox
        final android.widget.CheckBox selectAllCheckbox = new android.widget.CheckBox(requireContext());
        selectAllCheckbox.setText("Select All");
        selectAllCheckbox.setTextSize(16);
        selectAllCheckbox.setPadding(0, 10, 0, 20);
        layout.addView(selectAllCheckbox);

        // Add separator
        android.view.View separator = new android.view.View(requireContext());
        separator.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2
        ));
        separator.setBackgroundColor(0xFFCCCCCC);
        layout.addView(separator);

        // Create checkboxes for each item
        final List<android.widget.CheckBox> checkboxes = new ArrayList<>();
        for (int i = 0; i < allLibraryData.size(); i++) {
            Song song = allLibraryData.get(i);
            android.widget.CheckBox checkbox = new android.widget.CheckBox(requireContext());
            String displayText = song.getTitle() + " - " + song.getArtist();
            if ("Playlist".equalsIgnoreCase(song.getType())) {
                displayText = song.getTitle() + " (Playlist)";
            }
            checkbox.setText(displayText);
            checkbox.setTag(i); // Store position
            checkbox.setPadding(0, 10, 0, 10);
            checkboxes.add(checkbox);
            layout.addView(checkbox);
        }

        // Setup "Select All" functionality
        selectAllCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (android.widget.CheckBox cb : checkboxes) {
                cb.setChecked(isChecked);
            }
        });

        // Wrap in ScrollView
        android.widget.ScrollView scrollView = new android.widget.ScrollView(requireContext());
        scrollView.addView(layout);
        builder.setView(scrollView);

        builder.setPositiveButton("Remove", (dialog, which) -> {
            // Collect selected positions
            List<Integer> positionsToRemove = new ArrayList<>();
            for (android.widget.CheckBox cb : checkboxes) {
                if (cb.isChecked()) {
                    positionsToRemove.add((Integer) cb.getTag());
                }
            }

            if (positionsToRemove.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "No items selected", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            // Sort in reverse order to remove from end to start
            Collections.sort(positionsToRemove, Collections.reverseOrder());

            // Collect URIs of songs being removed for cleanup
            java.util.Set<String> removedUris = new java.util.HashSet<>();
            for (int pos : positionsToRemove) {
                if (pos >= 0 && pos < allLibraryData.size()) {
                    Song song = allLibraryData.get(pos);
                    if (song.getUriString() != null) {
                        removedUris.add(song.getUriString());
                    }
                }
            }

            // Remove items
            for (int pos : positionsToRemove) {
                if (pos >= 0 && pos < allLibraryData.size()) {
                    allLibraryData.remove(pos);
                }
            }

            // Check if currently playing song was removed
            vn.edu.tdtu.lhqc.meowsic.PlaybackManager pm = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
            android.net.Uri currentUri = pm.getCurrentUri();
            if (currentUri != null && removedUris.contains(currentUri.toString())) {
                // Stop playback if current song was removed
                pm.release();
            }

            // Clean up recently played - remove songs that were deleted
            cleanupRecentlyPlayed(removedUris);

            // Update storage and UI
            vn.edu.tdtu.lhqc.meowsic.SongStore.save(requireContext(), allLibraryData);
            songAdapter.updateData(allLibraryData);
            updateEmptyState();

            // Terminate playback if all songs were removed
            if (allLibraryData.isEmpty() || allSongsArePlaylistsOnly()) {
                pm.release();
            }

            android.widget.Toast.makeText(requireContext(), 
                positionsToRemove.size() + " item(s) removed", 
                android.widget.Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void cleanupRecentlyPlayed(java.util.Set<String> removedUris) {
        if (removedUris.isEmpty()) return;
        vn.edu.tdtu.lhqc.meowsic.RecentlyPlayedStore.removeSongs(requireContext(), removedUris);
    }
    
    private boolean allSongsArePlaylistsOnly() {
        for (Song song : allLibraryData) {
            if (song.getUriString() != null) {
                return false; // Found at least one actual song
            }
        }
        return true; // Only playlists or empty
    }

    private void showCreatePlaylistDialog() {
        // Create custom dialog
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_create_playlist);
        
        // Make dialog transparent background and proper size
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
            (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.85f),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        );
        
        // Get views
        com.google.android.material.textfield.TextInputLayout playlistNameLayout = 
            dialog.findViewById(R.id.playlist_name_layout);
        com.google.android.material.textfield.TextInputEditText playlistNameInput = 
            dialog.findViewById(R.id.playlist_name_input);
        com.google.android.material.button.MaterialButton btnCreate = 
            dialog.findViewById(R.id.btn_create_playlist);
        com.google.android.material.button.MaterialButton btnCancel = 
            dialog.findViewById(R.id.btn_cancel_playlist);
        
        // Setup create button
        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                String playlistName = playlistNameInput != null ? 
                    playlistNameInput.getText().toString().trim() : "";
                
                if (playlistName.isEmpty()) {
                    if (playlistNameLayout != null) {
                        playlistNameLayout.setError("Playlist name is required");
                    }
                    return;
                }
                
                // Clear error if any
                if (playlistNameLayout != null) {
                    playlistNameLayout.setError(null);
                }
                
                // Tạo playlist mới (dùng class Song làm đại diện playlist)
                Song newPlaylist = new Song(
                    playlistName,
                    "Playlist", // Artist field tạm dùng để hiển thị loại
                    R.drawable.ic_library_music_24px, // Icon playlist
                    null // Không có URI
                );
                newPlaylist.setType("Playlist");
                
                // Lưu vào danh sách
                allLibraryData.add(0, newPlaylist);
                songAdapter.updateData(allLibraryData);
                updateEmptyState();
                vn.edu.tdtu.lhqc.meowsic.SongStore.addAtTop(requireContext(), newPlaylist);
                
                // Thông báo
                android.widget.Toast.makeText(requireContext(), 
                    "Playlist \"" + playlistName + "\" created", 
                    android.widget.Toast.LENGTH_SHORT).show();
                
                dialog.dismiss();
            });
        }
        
        // Setup cancel button
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }
        
        // Show keyboard automatically
        if (playlistNameInput != null) {
            playlistNameInput.requestFocus();
            dialog.getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
            );
        }
        
        dialog.show();
    }
    
    // Show song menu when more button is clicked
    private void showSongMenu(Song song, View anchor) {
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_song_menu);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setGravity(android.view.Gravity.CENTER);

        LinearLayout optionAddToPlaylist = dialog.findViewById(R.id.option_add_to_playlist);
        LinearLayout optionPlayNext = dialog.findViewById(R.id.option_play_next);
        LinearLayout optionAddToQueue = dialog.findViewById(R.id.option_add_to_queue);
        LinearLayout optionRemove = dialog.findViewById(R.id.option_remove);

        // Add to Playlist
        if (optionAddToPlaylist != null) {
            optionAddToPlaylist.setOnClickListener(v -> {
                dialog.dismiss();
                showPlaylistSelectionDialog(song);
            });
        }

        // Play Next
        if (optionPlayNext != null) {
            optionPlayNext.setOnClickListener(v -> {
                dialog.dismiss();
                // Initialize QueueManager and add song
                vn.edu.tdtu.lhqc.meowsic.QueueManager.getInstance(requireContext());
                vn.edu.tdtu.lhqc.meowsic.QueueManager.addNext(song);
                android.widget.Toast.makeText(requireContext(), 
                    "\"" + song.getTitle() + "\" will play next", 
                    android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        // Add to Queue
        if (optionAddToQueue != null) {
            optionAddToQueue.setOnClickListener(v -> {
                dialog.dismiss();
                // Initialize QueueManager and add song
                vn.edu.tdtu.lhqc.meowsic.QueueManager.getInstance(requireContext());
                vn.edu.tdtu.lhqc.meowsic.QueueManager.addToQueue(song);
                android.widget.Toast.makeText(requireContext(), 
                    "\"" + song.getTitle() + "\" added to queue", 
                    android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        // Remove
        if (optionRemove != null) {
            optionRemove.setOnClickListener(v -> {
                dialog.dismiss();
                showRemoveConfirmationDialog(song);
            });
        }

        dialog.show();
    }
    
    // Show playlist selection dialog
    private void showPlaylistSelectionDialog(Song song) {
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_playlist);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setGravity(android.view.Gravity.CENTER);

        LinearLayout playlistContainer = dialog.findViewById(R.id.playlist_container);
        TextView noPlaylistsText = dialog.findViewById(R.id.no_playlists_text);

        // Get all playlists
        java.util.List<String> playlists = vn.edu.tdtu.lhqc.meowsic.PlaylistStore.getAllPlaylistNames(requireContext());

        if (playlists.isEmpty()) {
            if (noPlaylistsText != null) {
                noPlaylistsText.setVisibility(View.VISIBLE);
            }
        } else {
            if (playlistContainer != null) {
                for (String playlistName : playlists) {
                    TextView playlistOption = new TextView(requireContext());
                    playlistOption.setText(playlistName);
                    playlistOption.setTextSize(16);
                    playlistOption.setTextColor(getResources().getColor(R.color.primary_black, null));
                    playlistOption.setPadding(48, 40, 48, 40);
                    playlistOption.setBackground(getResources().getDrawable(android.R.drawable.list_selector_background, null));
                    playlistOption.setClickable(true);
                    playlistOption.setFocusable(true);
                    
                    playlistOption.setOnClickListener(v -> {
                        addSongToPlaylist(song, playlistName);
                        dialog.dismiss();
                    });
                    
                    playlistContainer.addView(playlistOption);
                }
            }
        }

        dialog.show();
    }
    
    // Add song to a playlist
    private void addSongToPlaylist(Song song, String playlistName) {
        if (song == null || playlistName == null) return;
        
        // Load existing songs in the playlist
        java.util.List<Song> playlistSongs = vn.edu.tdtu.lhqc.meowsic.PlaylistStore.loadPlaylistSongs(requireContext(), playlistName);
        
        // Check if song already exists in the playlist
        for (Song existingSong : playlistSongs) {
            if (existingSong.getUriString() != null && existingSong.getUriString().equals(song.getUriString())) {
                android.widget.Toast.makeText(requireContext(), 
                    "Song already in playlist", 
                    android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Add song to playlist
        playlistSongs.add(song);
        vn.edu.tdtu.lhqc.meowsic.PlaylistStore.savePlaylistSongs(requireContext(), playlistName, playlistSongs);
        
        android.widget.Toast.makeText(requireContext(), 
            "Added to \"" + playlistName + "\"", 
            android.widget.Toast.LENGTH_SHORT).show();
    }
    
    // Show confirmation dialog before removing
    private void showRemoveConfirmationDialog(Song song) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Remove Song")
            .setMessage("Are you sure you want to remove \"" + song.getTitle() + "\"?")
            .setPositiveButton("Remove", (dialogInterface, i) -> {
                removeSong(song);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    // Remove song from library
    private void removeSong(Song song) {
        if (song == null) return;
        
        // Collect URIs to remove
        java.util.Set<String> urisToRemove = new java.util.HashSet<>();
        if (song.getUriString() != null) {
            urisToRemove.add(song.getUriString());
        }
        
        // Stop playback if the current song is being removed
        vn.edu.tdtu.lhqc.meowsic.PlaybackManager playbackManager = vn.edu.tdtu.lhqc.meowsic.PlaybackManager.get();
        if (playbackManager.isPlaying() && song.getUriString() != null) {
            android.net.Uri currentUri = playbackManager.getCurrentUri();
            if (currentUri != null && song.getUriString().equals(currentUri.toString())) {
                playbackManager.release();
            }
        }
        
        // Remove from library
        allLibraryData.remove(song);
        songAdapter.updateData(allLibraryData);
        updateEmptyState();
        vn.edu.tdtu.lhqc.meowsic.SongStore.save(requireContext(), allLibraryData);
        
        // Clean up recently played
        cleanupRecentlyPlayed(urisToRemove);
        
        // Check if all songs have been removed
        if (allSongsArePlaylistsOnly()) {
            playbackManager.release();
        }
        
        android.widget.Toast.makeText(requireContext(), 
            "\"" + song.getTitle() + "\" removed", 
            android.widget.Toast.LENGTH_SHORT).show();
    }
}