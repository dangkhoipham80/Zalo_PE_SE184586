package com.example.zalo_pe_se184586.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.databinding.ActivityMainBinding;

// ✅ Import các Fragment đúng package
import com.example.zalo_pe_se184586.ui.chat.ChatListFragment;
import com.example.zalo_pe_se184586.ui.contacts.ContactsFragment;
import com.example.zalo_pe_se184586.ui.discover.DiscoverFragment;
import com.example.zalo_pe_se184586.ui.profile.ProfileFragment;

import com.example.zalo_pe_se184586.ui.select.SelectContactsActivity;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_messages);
        }

        // floating action button → mở SelectContactsActivity
        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectContactsActivity.class);
            startActivity(intent);
        });
    }

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_messages) {
                    selectedFragment = new ChatListFragment();
                    binding.toolbar.setTitle(R.string.nav_messages);
                    binding.fab.show();
                } else if (itemId == R.id.nav_contacts) {
                    selectedFragment = new ContactsFragment();
                    binding.toolbar.setTitle(R.string.nav_contacts);
                    binding.fab.show();
                } else if (itemId == R.id.nav_discover) {
                    selectedFragment = new DiscoverFragment();
                    binding.toolbar.setTitle(R.string.nav_discover);
                    binding.fab.hide();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    binding.toolbar.setTitle(R.string.nav_profile);
                    binding.fab.hide();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Fragment fragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (fragment instanceof SearchableFragment) {
                    ((SearchableFragment) fragment).onSearchQuery(newText);
                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_create_group) {
            Intent intent = new Intent(this, SelectContactsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    // Interface cho fragment có hỗ trợ search
    public interface SearchableFragment {
        void onSearchQuery(String query);
    }

    public void hideFAB() {
        binding.fab.hide();
    }

    public void showFAB() {
        binding.fab.show();
    }
}
