package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.adapters.SongAdapter;
import vn.edu.tdtu.lhqc.meowsic.managers.FavoriteStore;
import vn.edu.tdtu.lhqc.meowsic.managers.PlaybackManager;
import vn.edu.tdtu.lhqc.meowsic.managers.RefreshManager;
import vn.edu.tdtu.lhqc.meowsic.models.Song;

public class FavoriteFragment extends Fragment implements RefreshManager.RefreshListener {
    
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private TextView emptyState;
    private List<Song> favoriteSongs = new ArrayList<>();

    public FavoriteFragment() {
        // Required empty public constructor
    }

    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        loadFavoriteSongs();
        
        return view;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        RefreshManager.addListener(this);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        RefreshManager.removeListener(this);
    }
    
    @Override
    public void onDataChanged() {
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(this::loadFavoriteSongs);
        }
    }
    
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewSongs);
        emptyState = view.findViewById(R.id.empty_state);
    }
    
    private void setupRecyclerView() {
        songAdapter = new SongAdapter(favoriteSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(songAdapter);
        
        songAdapter.setOnSongClickListener(song -> {
            if (song == null || getActivity() == null) return;
            if (song.getUriString() != null) {
                try {
                    android.net.Uri uri = android.net.Uri.parse(song.getUriString());
                    PlaybackManager.get().play(
                        requireContext(), uri, song.getTitle(), song.getArtist(), song.getAlbumArtBase64()
                    );
                    
                    NowPlayingFragment target = NowPlayingFragment.newInstance(
                        song.getTitle(), song.getArtist(), song.getUriString()
                    );
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, target)
                        .addToBackStack(null)
                        .commit();
                } catch (Exception ignored) {}
            }
        });
    }
    
    private void loadFavoriteSongs() {
        favoriteSongs.clear();
        favoriteSongs.addAll(FavoriteStore.load(requireContext()));
        
        if (songAdapter != null) {
            songAdapter.updateData(favoriteSongs);
        }
        
        updateEmptyState();
    }
    
    private void updateEmptyState() {
        if (emptyState == null || recyclerView == null) return;
        
        if (favoriteSongs.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}