package com.example.zalo_pe_se184586.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.databinding.FragmentContactsBinding;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.ui.contacts.ContactsFragmentAdapter;
import com.example.zalo_pe_se184586.ui.select.SelectContactsActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

public class ContactsFragment extends Fragment implements MainActivity.SearchableFragment {

    private FragmentContactsBinding binding;
    private ContactsFragmentViewModel viewModel;
    private ContactsFragmentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ContactsFragmentViewModel.class);

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Friends"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Groups"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.setFilter(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        adapter = new ContactsFragmentAdapter(contact -> showContactOptions(contact));
        binding.contactsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.contactsRecycler.setAdapter(adapter);

        viewModel.getFilteredContacts().observe(getViewLifecycleOwner(), contacts -> {
            adapter.submitList(contacts);
            binding.emptyState.setVisibility(
                    contacts == null || contacts.isEmpty() ? View.VISIBLE : View.GONE
            );
        });

        binding.btnNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SelectContactsActivity.class);
            startActivity(intent);
        });

        binding.btnNewGroup.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SelectContactsActivity.class);
            startActivity(intent);
        });
    }

    private void showContactOptions(Contact contact) {
        String[] options = {"Send Message", "View Profile", "Add to Group"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(contact.getName())
                .setItems(options, (dialog, which) -> {
                    // TODO: Implement actions
                })
                .show();
    }

    @Override
    public void onSearchQuery(String query) {
        viewModel.search(query);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}