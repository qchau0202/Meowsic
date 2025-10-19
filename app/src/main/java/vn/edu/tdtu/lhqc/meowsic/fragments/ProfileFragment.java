package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.managers.PlaylistStore;
import vn.edu.tdtu.lhqc.meowsic.managers.RefreshManager;
import vn.edu.tdtu.lhqc.meowsic.managers.SongStore;
import vn.edu.tdtu.lhqc.meowsic.models.Song;

public class ProfileFragment extends Fragment implements RefreshManager.RefreshListener {

    // UI Components
    private ShapeableImageView profileImage;
    private ImageView editImageOverlay;
    private TextView profileName;
    private TextView profileTitle;
    private EditText editFullname;
    private EditText editEmail;
    private MaterialButton editProfileButton;
    private LinearLayout aboutMeowsic;
    private TextView playlistsCount, songsCount, artistsCount;

    // Profile data
    private boolean isEditMode = false;
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_PROFILE_NAME = "name";
    private static final String KEY_PROFILE_EMAIL = "email";
    
    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public ProfileFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        loadAndSaveImage(imageUri);
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initializeViews(view);
        setupClickListeners();
        loadSavedProfile();
        updateStatistics();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Update statistics when user returns to profile
        updateStatistics();
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
        // Called when data changes occur - refresh the statistics
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(this::updateStatistics);
        }
    }

    private void initializeViews(View view) {
        // Profile image and info
        profileImage = view.findViewById(R.id.profile_image);
        editImageOverlay = view.findViewById(R.id.edit_image_overlay);
        profileName = view.findViewById(R.id.profile_name);
        profileTitle = view.findViewById(R.id.profile_title);
        // Input fields
        editFullname = view.findViewById(R.id.edit_fullname);
        editEmail = view.findViewById(R.id.edit_email);
        // Button
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        // Menu options
        aboutMeowsic = view.findViewById(R.id.about_meowsic);
        // Statistics
        playlistsCount = view.findViewById(R.id.playlists_count);
        songsCount = view.findViewById(R.id.songs_count);
        artistsCount = view.findViewById(R.id.artists_count);
        // Set initial state
        setEditMode(false);
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            if (isEditMode) {
                // Save changes and exit edit mode
                saveProfileChanges();
                setEditMode(false);
            } else {
                // Enter edit mode
                setEditMode(true);
            }
        });
        
        // Profile image click (always available, not dependent on edit mode)
        profileImage.setOnClickListener(v -> openImagePicker());
        
        // Edit overlay click (always available)
        if (editImageOverlay != null) {
            editImageOverlay.setOnClickListener(v -> openImagePicker());
        }

        // Menu options
        aboutMeowsic.setOnClickListener(v -> showToast("About Meowsic"));
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        
        // Enable/disable input fields and update text colors
        if (editMode) {
            editFullname.setEnabled(true);
            editEmail.setEnabled(true);
            editFullname.setTextColor(getContext().getColor(R.color.primary_black));
            editEmail.setTextColor(getContext().getColor(R.color.primary_black));

            editProfileButton.setText("Save Changes");
            editProfileButton.setBackgroundTintList(getContext().getColorStateList(R.color.primary_pink));
        } else {
            editFullname.setEnabled(false);
            editEmail.setEnabled(false);
            editFullname.setTextColor(getContext().getColor(R.color.primary_grey));
            editEmail.setTextColor(getContext().getColor(R.color.primary_grey));

            editProfileButton.setText("Edit Profile");
            editProfileButton.setBackgroundTintList(getContext().getColorStateList(R.color.primary_pink));
        }
        
        // Note: Profile image edit overlay is always visible, not tied to edit mode
    }

    private void saveProfileChanges() {
        // Update profile name
        String newName = editFullname.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        
        // Validation
        if (newName.isEmpty()) {
            showToast("Name cannot be empty");
            return;
        }
        
        if (newEmail.isEmpty()) {
            showToast("Email cannot be empty");
            return;
        }
        
        if (!isValidEmail(newEmail)) {
            showToast("Please enter a valid email address");
            return;
        }
        
        if (!newName.isEmpty()) {
            profileName.setText(newName);
        }
        
        // Save to SharedPreferences (user_prefs for consistency)
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_PROFILE_NAME, newName);
        editor.putString(KEY_PROFILE_EMAIL, newEmail);
        editor.apply();
        
        showToast("Profile updated successfully!");
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    private void loadAndSaveImage(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            
            // Resize bitmap to save storage
            bitmap = resizeBitmap(bitmap, 400, 400);
            
            // Convert to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            
            // Save to SharedPreferences
            SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_PROFILE_IMAGE, encodedImage).apply();
            
            // Update UI
            profileImage.setImageBitmap(bitmap);
            
        } catch (Exception e) {
            showToast("Failed to load image");
        }
    }
    
    private void loadSavedProfile() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Load profile name
        String savedName = prefs.getString(KEY_PROFILE_NAME, "");
        if (!savedName.isEmpty()) {
            editFullname.setText(savedName);
            profileName.setText(savedName);
        }
        
        // Load email
        String savedEmail = prefs.getString(KEY_PROFILE_EMAIL, "");
        if (!savedEmail.isEmpty()) {
            editEmail.setText(savedEmail);
        }
        
        // Load profile image
        String encodedImage = prefs.getString(KEY_PROFILE_IMAGE, null);
        if (encodedImage != null) {
            try {
                byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                profileImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                // Use default image if decoding fails
            }
        }
    }
    
    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;
        
        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        
        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }
        
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }
    
    private void updateStatistics() {
        if (getContext() == null) return;
        
        // Load all songs from library
        java.util.List<Song> allSongs = SongStore.load(getContext());
        
        // Count songs (items with URI)
        int songCount = 0;
        java.util.Set<String> uniqueArtists = new java.util.HashSet<>();
        
        for (Song song : allSongs) {
            if (song.getUriString() != null) {
                songCount++;
                // Add artist to set for unique count
                if (song.getArtist() != null && !song.getArtist().isEmpty()) {
                    uniqueArtists.add(song.getArtist());
                }
            }
        }
        
        // Count playlists from PlaylistStore
        java.util.List<String> playlists = PlaylistStore.getAllPlaylistNames(getContext());
        int playlistCount = playlists.size();
        
        // Update UI
        if (songsCount != null) songsCount.setText(String.valueOf(songCount));
        if (playlistsCount != null) playlistsCount.setText(String.valueOf(playlistCount));
        if (artistsCount != null) artistsCount.setText(String.valueOf(uniqueArtists.size()));
    }
}