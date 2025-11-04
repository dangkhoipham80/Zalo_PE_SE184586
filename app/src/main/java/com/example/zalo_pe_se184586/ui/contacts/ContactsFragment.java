package com.example.zalo_pe_se184586.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zalo_pe_se184586.databinding.FragmentContactsBinding;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.ui.main.MainActivity;
import com.example.zalo_pe_se184586.ui.select.SelectContactsActivity;
import com.example.zalo_pe_se184586.ui.chat.GroupChatActivity;
import com.example.zalo_pe_se184586.data.GroupRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

/**
 * Fragment displaying contacts with tabs: All, Friends, Groups
 */
public class ContactsFragment extends Fragment implements MainActivity.SearchableFragment {

    private FragmentContactsBinding binding;
    private ContactsViewModel viewModel;
    private ContactsFragmentAdapter adapter;
    private GroupRepository groupRepository;

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

        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        groupRepository = GroupRepository.getInstance(requireContext());

        // Setup tabs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Friends"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Groups"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Filter contacts based on tab
                viewModel.setFilter(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Setup RecyclerView
        adapter = new ContactsFragmentAdapter(contact -> showContactOptions(contact));
        binding.contactsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.contactsRecycler.setAdapter(adapter);

        // Observe contacts
        viewModel.getFilteredContacts().observe(getViewLifecycleOwner(), contacts -> {
            adapter.submitList(contacts);
            binding.emptyState.setVisibility(
                    contacts == null || contacts.isEmpty() ? View.VISIBLE : View.GONE
            );
        });

        // Quick actions
        binding.btnNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SelectContactsActivity.class);
            intent.putExtra("mode", "single"); // Single contact chat
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
                    switch (which) {
                        case 0: // Send message
                            sendMessageToContact(contact);
                            break;
                        case 1: // View profile
                            showContactProfile(contact);
                            break;
                        case 2: // Add to group
                            showGroupSelector(contact);
                            break;
                    }
                })
                .show();
    }

    private void sendMessageToContact(Contact contact) {
        // Find or create a 1-1 group with this contact
        Group group = groupRepository.findOrCreateOneToOneGroup(contact);
        
        // Open chat activity
        Intent intent = new Intent(requireContext(), GroupChatActivity.class);
        intent.putExtra(GroupChatActivity.EXTRA_GROUP, group);
        startActivity(intent);
    }

    private void showContactProfile(Contact contact) {
        StringBuilder profileInfo = new StringBuilder();
        profileInfo.append("Name: ").append(contact.getName()).append("\n");
        profileInfo.append("ID: ").append(contact.getId()).append("\n");
        if (contact.getAvatarUrl() != null && !contact.getAvatarUrl().isEmpty()) {
            profileInfo.append("Avatar: ").append(contact.getAvatarUrl()).append("\n");
        } else {
            profileInfo.append("Avatar: Not set\n");
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Contact Profile")
                .setMessage(profileInfo.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showGroupSelector(Contact contact) {
        List<Group> allGroups = groupRepository.getAllGroups();
        
        if (allGroups.isEmpty()) {
            Toast.makeText(requireContext(), "No groups available. Create a group first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build array of group names
        String[] groupNames = new String[allGroups.size()];
        for (int i = 0; i < allGroups.size(); i++) {
            groupNames[i] = allGroups.get(i).getName();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select a group to add " + contact.getName())
                .setItems(groupNames, (dialog, which) -> {
                    Group selectedGroup = allGroups.get(which);
                    // Check if contact is already in the group
                    boolean alreadyInGroup = false;
                    for (Contact member : selectedGroup.getMembers()) {
                        if (member.getId().equals(contact.getId())) {
                            alreadyInGroup = true;
                            break;
                        }
                    }
                    
                    if (alreadyInGroup) {
                        Toast.makeText(requireContext(), contact.getName() + " is already in this group.", Toast.LENGTH_SHORT).show();
                    } else {
                        groupRepository.addMemberToGroup(selectedGroup.getId(), contact);
                        Toast.makeText(requireContext(), "Added " + contact.getName() + " to " + selectedGroup.getName(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
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