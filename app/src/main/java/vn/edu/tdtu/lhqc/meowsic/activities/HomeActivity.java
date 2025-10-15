package vn.edu.tdtu.lhqc.meowsic.activities;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_home;
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_library;
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_profile;

public class HomeActivity extends AppCompatActivity {
    private LinearLayout navHomeItem, navLibraryItem, navProfileItem;
    private int selectedNavItem = R.id.nav_home_item;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize navigation items
        initNavigationItems();

        // load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new fragment_home())
                    .commit();
            setSelectedNavItem(R.id.nav_home_item);
        }

        // Set click listeners for navigation items
        navHomeItem.setOnClickListener(v -> onNavigationItemClick(R.id.nav_home_item, new fragment_home()));
        navLibraryItem.setOnClickListener(v -> onNavigationItemClick(R.id.nav_library_item, new fragment_library()));
        navProfileItem.setOnClickListener(v -> onNavigationItemClick(R.id.nav_profile_item, new fragment_profile()));
    }
    
    private void initNavigationItems() {
        navHomeItem = findViewById(R.id.nav_home_item);
        navLibraryItem = findViewById(R.id.nav_library_item);
        navProfileItem = findViewById(R.id.nav_profile_item);
        
        // Set up navigation items with proper icons and text
        setupNavItem(navHomeItem, R.drawable.ic_home, "Home");
        setupNavItem(navLibraryItem, R.drawable.ic_library, "Library");
        setupNavItem(navProfileItem, R.drawable.ic_profile, "Profile");
    }
    
    private void setupNavItem(LinearLayout navItem, int iconRes, String text) {
        ImageView icon = navItem.findViewById(R.id.nav_icon);
        TextView textView = navItem.findViewById(R.id.nav_text);
        
        icon.setImageResource(iconRes);
        textView.setText(text);
    }
    
    private void onNavigationItemClick(int itemId, Fragment fragment) {
        setSelectedNavItem(itemId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    
    private void setSelectedNavItem(int itemId) {
        // Reset all items to unselected state
        setNavItemState(navHomeItem, false);
        setNavItemState(navLibraryItem, false);
        setNavItemState(navProfileItem, false);
        
        // Set selected item
        selectedNavItem = itemId;
        if (itemId == R.id.nav_home_item) {
            setNavItemState(navHomeItem, true);
        } else if (itemId == R.id.nav_library_item) {
            setNavItemState(navLibraryItem, true);
        } else if (itemId == R.id.nav_profile_item) {
            setNavItemState(navProfileItem, true);
        }
    }
    
    private void setNavItemState(LinearLayout navItem, boolean isSelected) {
        ImageView icon = navItem.findViewById(R.id.nav_icon);
        TextView textView = navItem.findViewById(R.id.nav_text);
        
        if (isSelected) {
            icon.setColorFilter(getResources().getColor(R.color.primary_pink, null));
            textView.setTextColor(getResources().getColor(R.color.primary_pink, null));
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            icon.setColorFilter(getResources().getColor(R.color.primary_grey, null));
            textView.setTextColor(getResources().getColor(R.color.primary_grey, null));
            textView.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    public void onBackPressedDispatcher() {
        // Check if there are fragments in the back stack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // Pop the back stack instead of finishing the activity
            getSupportFragmentManager().popBackStack();
        } else {
            // No fragments in back stack, finish the activity
            super.getOnBackPressedDispatcher();
        }
    }
}