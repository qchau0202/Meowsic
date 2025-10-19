package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.managers.PlaybackManager;
import vn.edu.tdtu.lhqc.meowsic.managers.QueueManager;
import vn.edu.tdtu.lhqc.meowsic.managers.RefreshManager;
import vn.edu.tdtu.lhqc.meowsic.managers.SongStore;
import vn.edu.tdtu.lhqc.meowsic.models.Song;
import vn.edu.tdtu.lhqc.meowsic.managers.PlaylistStore;
import vn.edu.tdtu.lhqc.meowsic.adapters.PlaylistSongAdapter;
import vn.edu.tdtu.lhqc.meowsic.utils.SongImportUtil;

public class PlaylistFragment extends Fragment implements RefreshManager.RefreshListener {
    private RecyclerView recyclerView;
    private PlaylistSongAdapter songAdapter;
    private TextView playlistTitle, emptyState;
    private ImageView btnMenu;
    private SearchFragment searchFragment;
    private List<Song> playlistSongs = new ArrayList<>();
    private View selectionToolbar;
    private TextView selectionCount;
    private ImageView btnCancelSelection;
    private TextView btnRemoveSelected;

    // Playlist information
    private String playlistName = "Playlist";
    private String playlistDescription = "Description";

    public PlaylistFragment() {
        // Required empty public constructor
    }

    public static PlaylistFragment newInstance(String playlistName, String playlistDescription) {
        PlaylistFragment fragment = new PlaylistFragment();
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
        setupButtons();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload playlist songs when returning to the fragment
        loadPlaylistSongs();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // Register for refresh notifications
        RefreshManager.addListener(this);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        // Unregister from refresh notifications
        RefreshManager.removeListener(this);
    }
    
    @Override
    public void onDataChanged() {
        // Called when data changes occur - refresh the playlist
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(this::refreshPlaylistData);
        }
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewSongs);
        playlistTitle = view.findViewById(R.id.playlist_title);
        emptyState = view.findViewById(R.id.empty_state_playlist);
        btnMenu = view.findViewById(R.id.btn_menu_playlist);
        selectionToolbar = view.findViewById(R.id.selection_toolbar);
        selectionCount = view.findViewById(R.id.selection_count);
        btnCancelSelection = view.findViewById(R.id.btn_cancel_selection);
        btnRemoveSelected = view.findViewById(R.id.btn_remove_selected);
        
        // Update the playlist title with the passed name
        playlistTitle.setText(playlistName);
        
        // Setup selection toolbar buttons
        if (btnCancelSelection != null) {
            btnCancelSelection.setOnClickListener(v -> exitSelectionMode());
        }
        
        if (btnRemoveSelected != null) {
            btnRemoveSelected.setOnClickListener(v -> removeSelectedSongs());
        }
    }
    
    private void setupRecyclerView() {
        // Load playlist songs from persistent storage
        loadPlaylistSongs();

        // Setup RecyclerView with custom adapter
        songAdapter = new PlaylistSongAdapter(playlistSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(songAdapter);
        
        // Navigate to Now Playing on song click
        songAdapter.setOnSongClickListener(song -> {
            if (song == null || getActivity() == null) return;
            // Pass URI when available to play the actual file
            NowPlayingFragment target;
            if (song.getUriString() != null) {
                // Start playback via shared manager so minimized player also reflects state
                try {
                    android.net.Uri uri = android.net.Uri.parse(song.getUriString());
                    PlaybackManager.get().play(requireContext(), uri, song.getTitle(), song.getArtist(), song.getAlbumArtBase64());
                } catch (Exception ignored) {}
                target = NowPlayingFragment.newInstance(song.getTitle(), song.getArtist(), song.getUriString());
            } else {
                target = NowPlayingFragment.newInstance(song.getTitle(), song.getArtist());
            }
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, target)
                    .addToBackStack(null)
                    .commit();
        });
        
        // Handle more button clicks for songs
        songAdapter.setOnSongMoreClickListener((song, view) -> {
            showSongMenu(song, view);
        });
        
        // Listen to selection changes
        songAdapter.setOnSelectionChangedListener(this::updateSelectionCount);
        
        updateEmptyState();
    }
    
    private void loadPlaylistSongs() {
        playlistSongs.clear();
        playlistSongs.addAll(PlaylistStore.loadPlaylistSongs(requireContext(), playlistName));
        if (songAdapter != null) {
            songAdapter.updateData(playlistSongs);
        }
        updateEmptyState();
    }
    
    /**
     * Public method to refresh the playlist data from external calls
     */
    public void refreshPlaylistData() {
        loadPlaylistSongs();
    }
    
    private void updateEmptyState() {
        if (emptyState == null || recyclerView == null) return;
        
        if (playlistSongs.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void setupButtons() {
        // Menu button to show options
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> showPlaylistMenu());
        }
    }
    
    private void showPlaylistMenu() {
        // Create the dialog
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_playlist_menu);
        
        // Make dialog appear in center and have proper dimensions
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
            (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.85f),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        );
        
        // Get views
        android.widget.LinearLayout optionAddSongs = dialog.findViewById(R.id.option_add_songs);
        android.widget.LinearLayout optionRemoveSongs = dialog.findViewById(R.id.option_remove_songs);
        
        // Set up click listeners
        if (optionAddSongs != null) {
            optionAddSongs.setOnClickListener(v -> {
                dialog.dismiss();
                showAddSongsDialog();
            });
        }
        
        if (optionRemoveSongs != null) {
            optionRemoveSongs.setOnClickListener(v -> {
                dialog.dismiss();
                showRemoveSongsDialog();
            });
        }
        
        // Show dialog
        dialog.show();
    }
    
    private void showAddSongsDialog() {
        // Get all songs from library that are not playlists
        List<Song> allSongs = SongStore.load(requireContext());
        
        // Create a set of URIs for songs already in the playlist
        java.util.Set<String> existingUris = new java.util.HashSet<>();
        for (Song song : playlistSongs) {
            if (song.getUriString() != null) {
                existingUris.add(song.getUriString());
            }
        }
        
        // Filter to only songs that are not already in the playlist
        List<Song> availableSongs = new ArrayList<>();
        for (Song song : allSongs) {
            if (song.getUriString() != null && !existingUris.contains(song.getUriString())) {
                availableSongs.add(song);
            }
        }
        
        if (availableSongs.isEmpty()) {
            showImportSongsDialog();
            return;
        }
        
        // Create dialog with multi-select list
        String[] songTitles = new String[availableSongs.size()];
        for (int i = 0; i < availableSongs.size(); i++) {
            songTitles[i] = availableSongs.get(i).getTitle() + " - " + availableSongs.get(i).getArtist();
        }
        
        boolean[] checkedItems = new boolean[availableSongs.size()];
        List<Song> selectedSongs = new ArrayList<>();
        
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Songs to " + playlistName)
            .setMultiChoiceItems(songTitles, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedSongs.add(availableSongs.get(which));
                } else {
                    selectedSongs.remove(availableSongs.get(which));
                }
            })
            .setPositiveButton("Add", (dialog, which) -> {
                if (!selectedSongs.isEmpty()) {
                    PlaylistStore.addSongsToPlaylist(requireContext(), playlistName, selectedSongs);
                    loadPlaylistSongs();
                    
                    // Notify all listeners that data has changed
                    RefreshManager.notifyDataChanged();
                    
                    android.widget.Toast.makeText(requireContext(), 
                        selectedSongs.size() + " song(s) added to playlist", 
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showRemoveSongsDialog() {
        if (playlistSongs.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), 
                "No songs to remove", 
                android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Enter selection mode
        enterSelectionMode();
    }
    
    private void enterSelectionMode() {
        if (songAdapter != null) {
            songAdapter.enterSelectionMode();
            
            // Show selection toolbar, hide normal toolbar
            if (selectionToolbar != null) selectionToolbar.setVisibility(View.VISIBLE);
            if (btnMenu != null) btnMenu.setVisibility(View.GONE);
            if (playlistTitle != null) playlistTitle.setVisibility(View.GONE);
            
            updateSelectionCount(0);
        }
    }
    
    private void exitSelectionMode() {
        if (songAdapter != null) {
            songAdapter.exitSelectionMode();
            
            // Hide selection toolbar, show normal toolbar
            if (selectionToolbar != null) selectionToolbar.setVisibility(View.GONE);
            if (btnMenu != null) btnMenu.setVisibility(View.VISIBLE);
            if (playlistTitle != null) playlistTitle.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateSelectionCount(int count) {
        if (selectionCount != null) {
            selectionCount.setText(count + " selected");
        }
    }
    
    private void removeSelectedSongs() {
        if (songAdapter == null) return;
        
        List<Song> selectedSongs = songAdapter.getSelectedSongs();
        
        if (selectedSongs.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), 
                "No songs selected", 
                android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Confirm removal
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Remove Songs")
            .setMessage("Remove " + selectedSongs.size() + " song(s) from " + playlistName + "?")
            .setPositiveButton("Remove", (dialog, which) -> {
                // Collect URIs of songs being removed
                java.util.Set<String> removedUris = new java.util.HashSet<>();
                
                // Remove selected songs
                for (Song song : selectedSongs) {
                    if (song.getUriString() != null) {
                        removedUris.add(song.getUriString());
                        PlaylistStore.removeSongFromPlaylist(requireContext(), playlistName, song.getUriString());
                    }
                }
                
                // Check if currently playing song was removed
                PlaybackManager pm = PlaybackManager.get();
                android.net.Uri currentUri = pm.getCurrentUri();
                if (currentUri != null && removedUris.contains(currentUri.toString())) {
                    // Stop playback if current song was removed
                    pm.release();
                }
                
                // Exit selection mode and reload
                exitSelectionMode();
                loadPlaylistSongs();
                
                // Terminate playback if all songs in playlist were removed
                if (playlistSongs.isEmpty()) {
                    pm.release();
                }
                
                android.widget.Toast.makeText(requireContext(), 
                    selectedSongs.size() + " song(s) removed from playlist", 
                    android.widget.Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }    

    private void setupSearchFragment() {
        // Create search fragment
        searchFragment = new SearchFragment();

        searchFragment.setSearchResultCallback(new SearchFragment.SearchResultCallback() {
            @Override
            public void onSearchResultSelected(SearchFragment.SearchResult result) {
                // No navigation from dropdown; keep list filtering only
            }

            @Override
            public void onSearchQueryChanged(String query) {
                // Search filtering is handled by the search fragment's dropdown
                // No need to filter the RecyclerView for playlists
            }

            @Override
            public List<SearchFragment.SearchResult> getSearchableData() {
                List<SearchFragment.SearchResult> results = new ArrayList<>();
                for (Song s : playlistSongs) {
                    results.add(new SearchFragment.SearchResult(s.getTitle(), s.getArtist(), "song"));
                }
                return results;
            }
        });

        // Attach search fragment into container
        getChildFragmentManager().beginTransaction()
            .replace(R.id.search_container, searchFragment)
            .commit();
    }
    
    private void showImportSongsDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("No Songs Available")
            .setMessage("There are no songs to add to this playlist. Would you like to import songs from your device?")
            .setPositiveButton("Import", (dialog, which) -> {
                // Launch system picker for audio files
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                intent.putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(intent, 2001); // Different request code from library (1011)
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001 && resultCode == android.app.Activity.RESULT_OK && data != null) {
            java.util.List<android.net.Uri> uris = new java.util.ArrayList<>();
            if (data.getData() != null) {
                uris.add(data.getData());
            } else if (data.getClipData() != null) {
                android.content.ClipData clip = data.getClipData();
                for (int i = 0; i < clip.getItemCount(); i++) {
                    uris.add(clip.getItemAt(i).getUri());
                }
            }
            
            // Get existing URIs from library to check for duplicates
            List<Song> allLibrarySongs = SongStore.load(requireContext());
            java.util.Set<String> existingUris = new java.util.HashSet<>();
            for (Song song : allLibrarySongs) {
                if (song.getUriString() != null) {
                    existingUris.add(song.getUriString());
                }
            }
            
            // Take persistable permission and import songs
            List<Song> imported = new ArrayList<>();
            for (android.net.Uri uri : uris) {
                String uriString = uri.toString();
                
                // Skip if this URI already exists in library
                if (existingUris.contains(uriString)) {
                    continue;
                }
                
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
                
                // Extract metadata and add to list
                Song s = SongImportUtil.buildSongFromUri(requireContext(), uri);
                if (s != null) {
                    imported.add(s);
                    existingUris.add(uriString); // Add to set to avoid duplicates in current import batch
                }
            }
            
            // Save imported songs to library
            if (!imported.isEmpty()) {
                SongStore.addAllAtTop(requireContext(), imported);
                
                // Add imported songs to this playlist
                PlaylistStore.addSongsToPlaylist(requireContext(), playlistName, imported);
                
                // Reload playlist
                loadPlaylistSongs();
                
                android.widget.Toast.makeText(requireContext(), 
                    imported.size() + " song(s) imported and added to playlist", 
                    android.widget.Toast.LENGTH_SHORT).show();
            } else {
                android.widget.Toast.makeText(requireContext(), 
                    "All selected songs are already in the library", 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        }
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
                QueueManager.getInstance(requireContext());
                QueueManager.addNext(song);
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
                QueueManager.getInstance(requireContext());
                QueueManager.addToQueue(song);
                android.widget.Toast.makeText(requireContext(), 
                    "\"" + song.getTitle() + "\" added to queue", 
                    android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        // Remove (from playlist, not from library)
        if (optionRemove != null) {
            optionRemove.setOnClickListener(v -> {
                dialog.dismiss();
                showRemoveFromPlaylistConfirmation(song);
            });
        }

        dialog.show();
    }
    
    // Show playlist selection dialog for adding song to another playlist
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
        java.util.List<String> playlists = PlaylistStore.getAllPlaylistNames(requireContext());
        
        // Remove current playlist from list
        playlists.remove(playlistName);

        if (playlists.isEmpty()) {
            if (noPlaylistsText != null) {
                noPlaylistsText.setVisibility(View.VISIBLE);
                noPlaylistsText.setText("No other playlists available.\nCreate one in the Library.");
            }
        } else {
            if (playlistContainer != null) {
                for (String targetPlaylist : playlists) {
                    TextView playlistOption = new TextView(requireContext());
                    playlistOption.setText(targetPlaylist);
                    playlistOption.setTextSize(16);
                    playlistOption.setTextColor(getResources().getColor(R.color.primary_black, null));
                    playlistOption.setPadding(48, 40, 48, 40);
                    playlistOption.setBackground(getResources().getDrawable(android.R.drawable.list_selector_background, null));
                    playlistOption.setClickable(true);
                    playlistOption.setFocusable(true);
                    
                    playlistOption.setOnClickListener(v -> {
                        addSongToPlaylist(song, targetPlaylist);
                        dialog.dismiss();
                    });
                    
                    playlistContainer.addView(playlistOption);
                }
            }
        }

        dialog.show();
    }
    
    // Add song to another playlist
    private void addSongToPlaylist(Song song, String targetPlaylist) {
        if (song == null || targetPlaylist == null) return;
        
        // Load existing songs in the target playlist
        List<Song> targetPlaylistSongs = PlaylistStore.loadPlaylistSongs(requireContext(), targetPlaylist);
        
        // Check if song already exists in the target playlist
        for (Song existingSong : targetPlaylistSongs) {
            if (existingSong.getUriString() != null && existingSong.getUriString().equals(song.getUriString())) {
                android.widget.Toast.makeText(requireContext(), 
                    "Song already in playlist", 
                    android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Add song to target playlist
        targetPlaylistSongs.add(song);
        PlaylistStore.savePlaylistSongs(requireContext(), targetPlaylist, targetPlaylistSongs);
        
        // Notify all listeners that data has changed
        RefreshManager.notifyDataChanged();
        
        android.widget.Toast.makeText(requireContext(), 
            "Added to \"" + targetPlaylist + "\"", 
            android.widget.Toast.LENGTH_SHORT).show();
    }
    
    // Show confirmation dialog before removing song from playlist
    private void showRemoveFromPlaylistConfirmation(Song song) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Remove from Playlist")
            .setMessage("Remove \"" + song.getTitle() + "\" from this playlist?")
            .setPositiveButton("Remove", (dialogInterface, i) -> {
                removeSongFromPlaylist(song);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    // Remove song from this playlist
    private void removeSongFromPlaylist(Song song) {
        if (song == null) return;
        
        // Collect URIs to remove
        java.util.Set<String> urisToRemove = new java.util.HashSet<>();
        if (song.getUriString() != null) {
            urisToRemove.add(song.getUriString());
        }
        
        // Stop playback if the current song is being removed
        PlaybackManager playbackManager = PlaybackManager.get();
        if (playbackManager.isPlaying() && song.getUriString() != null) {
            android.net.Uri currentUri = playbackManager.getCurrentUri();
            if (currentUri != null && song.getUriString().equals(currentUri.toString())) {
                playbackManager.release();
            }
        }
        
        // Remove from playlist
        playlistSongs.remove(song);
        PlaylistStore.savePlaylistSongs(requireContext(), playlistName, playlistSongs);
        songAdapter.updateData(playlistSongs);
        updateEmptyState();
        
        // Check if all songs have been removed
        if (playlistSongs.isEmpty()) {
            playbackManager.release();
        }
        
        // Notify all listeners that data has changed
        RefreshManager.notifyDataChanged();
        
        android.widget.Toast.makeText(requireContext(), 
            "\"" + song.getTitle() + "\" removed from playlist", 
            android.widget.Toast.LENGTH_SHORT).show();
    }
}