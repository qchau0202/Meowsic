package vn.edu.tdtu.lhqc.meowsic.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import vn.edu.tdtu.lhqc.meowsic.R;

public class fragment_profile extends Fragment {

    // UI Components
    private ShapeableImageView profileImage;
    private TextView profileName;
    private TextView profileTitle;
    private EditText editFullname;
    private EditText editEmail;
    private MaterialButton editProfileButton;
    private LinearLayout privacySettings;
    private LinearLayout downloadSettings;
    private LinearLayout aboutMeowsic;

    // Profile data
    private boolean isEditMode = false;

    public fragment_profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initializeViews(view);
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        // Profile image and info
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileTitle = view.findViewById(R.id.profile_title);
        // Input fields
        editFullname = view.findViewById(R.id.edit_fullname);
        editEmail = view.findViewById(R.id.edit_email);
        // Button
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        // Menu options
        privacySettings = view.findViewById(R.id.privacy_settings);
        downloadSettings = view.findViewById(R.id.download_settings);
        aboutMeowsic = view.findViewById(R.id.about_meowsic);
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

        // Menu options click listeners
        privacySettings.setOnClickListener(v -> showToast("Privacy Settings"));
        downloadSettings.setOnClickListener(v -> showToast("Download Settings"));
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
    }

    private void saveProfileChanges() {
        // Update profile name
        String newName = editFullname.getText().toString().trim();
        if (!newName.isEmpty()) {
            profileName.setText(newName);
        }
        
        showToast("Profile updated successfully!");
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}