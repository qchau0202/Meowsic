package vn.edu.tdtu.lhqc.meowsic.activities;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import vn.edu.tdtu.lhqc.meowsic.R;
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_home;
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_library;
import vn.edu.tdtu.lhqc.meowsic.fragments.fragment_profile;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new fragment_home())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selected = null;
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selected = new fragment_home();
                } else if (itemId == R.id.nav_library) {
                    selected = new fragment_library();
                } else if (itemId == R.id.nav_profile) {
                    selected = new fragment_profile();
                }

                if (selected != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selected)
                            .commit();
                    return true;
                }
                return false;
            }
        });

    }
}