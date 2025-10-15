package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import vn.edu.tdtu.lhqc.meowsic.R;

// Display now playing screen with song information and playback controls
public class fragment_now_playing extends Fragment {

    // UI Components
    private ImageView btnBack, btnMenu, btnPrevious, btnPlayPause, btnNext, btnExpand;
    private TextView songTitle, artistName;
    private SeekBar progressBar;

    // Song information
    private String currentSongTitle = "Song";
    private String currentArtistName = "Title";

    public fragment_now_playing() {
        // empty constructor
    }

    public static fragment_now_playing newInstance(String songTitle, String artistName) {
        fragment_now_playing fragment = new fragment_now_playing();
        Bundle args = new Bundle();
        args.putString("song_title", songTitle);
        args.putString("artist_name", artistName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSongTitle = getArguments().getString("song_title", "Song");
            currentArtistName = getArguments().getString("artist_name", "Artist");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_now_playing, container, false);
        
        initializeViews(view);
        setupClickListeners();
        updateSongInfo();
        
        return view;
    }

    private void initializeViews(View view) {
        // Header buttons
        btnBack = view.findViewById(R.id.btn_back);
        btnMenu = view.findViewById(R.id.btn_menu);
        
        // Song information
        songTitle = view.findViewById(R.id.song_title);
        artistName = view.findViewById(R.id.artist_name);

        // Progress
        progressBar = view.findViewById(R.id.progress_bar);
        
        // Playback controls
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btnNext = view.findViewById(R.id.btn_next);
        
        // Set up progress bar (placeholder values)
        progressBar.setMax(100);
        progressBar.setProgress(0);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Use fragment manager to handle back navigation properly
                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    getActivity().onBackPressed();
                }
            }
        });

        // Menu button
        btnMenu.setOnClickListener(v -> {
            // TODO: Implement menu functionality
        });

        // Previous button
        btnPrevious.setOnClickListener(v -> {
            // TODO: Implement previous song functionality
        });

        // Play/Pause button
        btnPlayPause.setOnClickListener(v -> {
            // TODO: Implement play/pause functionality
        });

        // Next button
        btnNext.setOnClickListener(v -> {
            // TODO: Implement next song functionality
        });

        // Expand button
        btnExpand.setOnClickListener(v -> {
            // TODO: Implement expand to show song list
        });

        // Progress bar
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO: Handle progress changes
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO: Pause playback while seeking
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO: Seek to new position in media player
            }
        });
    }

    private void updateSongInfo() {
        songTitle.setText(currentSongTitle);
        artistName.setText(currentArtistName);
    }
}