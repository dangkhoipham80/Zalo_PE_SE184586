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
import androidx.recyclerview.widget.RecyclerView;

import com.example.zalo_pe_se184586.databinding.FragmentContactsBinding;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.ui.main.MainActivity;
import com.example.zalo_pe_se184586.ui.select.SelectContactsActivity;
import com.example.zalo_pe_se184586.ui.chat.GroupChatActivity;
import com.example.zalo_pe_se184586.data.GroupRepository;
import com.example.zalo_pe_se184586.ui.main.ChatListAdapter;
import com.example.zalo_pe_se184586.data.ContactRepository;
import com.example.zalo_pe_se184586.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.example.zalo_pe_se184586.model.Message;

import java.util.List;
import java.util.Collections;
import java.util.Collections;

/**
 * Fragment displaying contacts with tabs: All, Friends, Groups
 */
public class ContactsFragment extends Fragment implements MainActivity.SearchableFragment {

    private FragmentContactsBinding binding;
    private ContactsViewModel viewModel;
    private ContactsFragmentAdapter contactAdapter;
    private ChatListAdapter groupAdapter;
    private GroupRepository groupRepository;
    private RecyclerView.Adapter<?> currentAdapter;

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

        // Setup adapters
        contactAdapter = new ContactsFragmentAdapter(contact -> showContactOptions(contact));
        groupAdapter = new ChatListAdapter(group -> openGroupChat(group));
        groupAdapter.setOnChatLongClickListener(this::showGroupOptions);
        groupAdapter.setShowSettingsButton(true); // Show settings button for groups
        
        // Set initial adapter (All tab)
        currentAdapter = contactAdapter;
        binding.contactsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.contactsRecycler.setAdapter(contactAdapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewModel.setFilter(position);
                
                // Switch adapter based on tab
                if (position == 2) { // Groups tab
                    currentAdapter = groupAdapter;
                    binding.contactsRecycler.setAdapter(groupAdapter);
                    // Refresh repository and load groups (only groups with >2 members)
                    groupRepository.refreshGroups();
                    List<Group> allGroups = groupRepository.getAllGroups();
                    // Filter: only show groups with more than 2 members (exclude 1-1 chats)
                    List<Group> filteredGroups = new java.util.ArrayList<>();
                    for (Group group : allGroups) {
                        if (group.getMembers().size() > 2) {
                            filteredGroups.add(group);
                        }
                    }
                    // Sort by latest message timestamp (descending)
                    sortGroupsByLatestMessage(filteredGroups);
                    groupAdapter.submitList(filteredGroups);
                    binding.emptyState.setVisibility(
                            filteredGroups.isEmpty() ? View.VISIBLE : View.GONE
                    );
                    
                    // Show Add button, hide actionsBar, hide FAB
                    binding.btnAddGroup.setVisibility(View.VISIBLE);
                    binding.actionsBar.setVisibility(View.GONE);
                    hideFAB();
                } else { // All or Friends tab
                    if (currentAdapter != contactAdapter) {
                        currentAdapter = contactAdapter;
                        binding.contactsRecycler.setAdapter(contactAdapter);
                    }
                    // Hide Add button, show actionsBar, show FAB
                    binding.btnAddGroup.setVisibility(View.GONE);
                    binding.actionsBar.setVisibility(View.VISIBLE);
                    showFAB();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Observe contacts (for All and Friends tabs)
        viewModel.getFilteredContacts().observe(getViewLifecycleOwner(), contacts -> {
            if (currentAdapter == contactAdapter) {
                contactAdapter.submitList(contacts);
                binding.emptyState.setVisibility(
                        contacts == null || contacts.isEmpty() ? View.VISIBLE : View.GONE
                );
            }
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
        
        // Add Group button (shown in Groups tab)
        binding.btnAddGroup.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SelectContactsActivity.class);
            startActivity(intent);
        });
    }

    private void hideFAB() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideFAB();
        }
    }

    private void showFAB() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showFAB();
        }
    }

    /**
     * Sắp xếp groups theo thời gian tin nhắn mới nhất (descending - mới nhất ở đầu)
     */
    private void sortGroupsByLatestMessage(List<Group> groups) {
        Collections.sort(groups, (g1, g2) -> {
            Message lastMsg1 = g1.getLastMessage();
            Message lastMsg2 = g2.getLastMessage();
            
            // Nếu cả hai đều có tin nhắn, sort theo timestamp (descending)
            if (lastMsg1 != null && lastMsg2 != null) {
                return Long.compare(lastMsg2.getTimestamp(), lastMsg1.getTimestamp());
            }
            // Group có tin nhắn đứng trước group không có tin nhắn
            if (lastMsg1 != null) return -1;
            if (lastMsg2 != null) return 1;
            // Cả hai đều không có tin nhắn, giữ nguyên thứ tự
            return 0;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh groups list when returning to this fragment
        if (currentAdapter == groupAdapter) {
            groupRepository.refreshGroups();
            List<Group> allGroups = groupRepository.getAllGroups();
            // Filter: only show groups with more than 2 members (exclude 1-1 chats)
            List<Group> filteredGroups = new java.util.ArrayList<>();
            for (Group group : allGroups) {
                if (group.getMembers().size() > 2) {
                    filteredGroups.add(group);
                }
            }
            // Sort by latest message timestamp (descending)
            sortGroupsByLatestMessage(filteredGroups);
            groupAdapter.submitList(filteredGroups);
            binding.emptyState.setVisibility(
                    filteredGroups.isEmpty() ? View.VISIBLE : View.GONE
            );
        }
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

    private void openGroupChat(Group group) {
        Intent intent = new Intent(requireContext(), GroupChatActivity.class);
        intent.putExtra(GroupChatActivity.EXTRA_GROUP, group);
        startActivity(intent);
    }

    private void showGroupOptions(Group group) {
        String[] options = {
            requireContext().getString(R.string.action_edit_name),
            requireContext().getString(R.string.action_add_member),
            requireContext().getString(R.string.delete)
        };
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(group.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit name
                            showEditGroupNameDialog(group);
                            break;
                        case 1: // Add member
                            showAddMemberToGroupDialog(group);
                            break;
                        case 2: // Delete
                            showDeleteGroupDialog(group);
                            break;
                    }
                })
                .show();
    }

    private void showEditGroupNameDialog(Group group) {
        com.google.android.material.textfield.TextInputLayout inputLayout = 
            new com.google.android.material.textfield.TextInputLayout(requireContext());
        inputLayout.setHint(getString(R.string.prompt_group_name));
        int padding = requireContext().getResources().getDimensionPixelSize(R.dimen.dialog_content_padding);
        inputLayout.setPadding(padding, padding, padding, 0);
        
        com.google.android.material.textfield.TextInputEditText editText = 
            new com.google.android.material.textfield.TextInputEditText(inputLayout.getContext());
        editText.setLayoutParams(new com.google.android.material.textfield.TextInputLayout.LayoutParams(
                com.google.android.material.textfield.TextInputLayout.LayoutParams.MATCH_PARENT,
                com.google.android.material.textfield.TextInputLayout.LayoutParams.WRAP_CONTENT));
        editText.setText(group.getName());
        inputLayout.addView(editText);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.action_edit_name)
                .setView(inputLayout)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            CharSequence text = editText.getText();
            String newName = text != null ? text.toString().trim() : "";
            if (newName.isEmpty()) {
                inputLayout.setError(getString(R.string.error_group_name_required));
                return;
            }
            inputLayout.setError(null);
            groupRepository.updateGroupName(group.getId(), newName);
            // Refresh groups list
            refreshGroupsList();
            Toast.makeText(requireContext(), getString(R.string.group_name_updated), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void showAddMemberToGroupDialog(Group group) {
        ContactRepository contactRepo = ContactRepository.getInstance(requireContext());
        List<Contact> allContacts = contactRepo.getContacts();
        
        // Filter: contacts NOT IN group
        List<Contact> availableContacts = new java.util.ArrayList<>();
        for (Contact contact : allContacts) {
            boolean isInGroup = false;
            for (Contact member : group.getMembers()) {
                if (member.getId().equals(contact.getId())) {
                    isInGroup = true;
                    break;
                }
            }
            if (!isInGroup) {
                availableContacts.add(contact);
            }
        }
        
        if (availableContacts.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_contacts_available, Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] contactNames = new String[availableContacts.size()];
        for (int i = 0; i < availableContacts.size(); i++) {
            contactNames[i] = availableContacts.get(i).getName();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.select_member_to_add)
                .setItems(contactNames, (dialog, which) -> {
                    Contact selectedContact = availableContacts.get(which);
                    groupRepository.addMemberToGroup(group.getId(), selectedContact);
                    refreshGroupsList();
                    Toast.makeText(requireContext(), 
                        getString(R.string.member_added_success, selectedContact.getName()), 
                        Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showDeleteGroupDialog(Group group) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_group_title)
                .setMessage(getString(R.string.delete_group_message, group.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    groupRepository.deleteGroup(group.getId());
                    refreshGroupsList();
                    Toast.makeText(requireContext(), getString(R.string.group_deleted), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void refreshGroupsList() {
        if (currentAdapter == groupAdapter) {
            groupRepository.refreshGroups();
            List<Group> allGroups = groupRepository.getAllGroups();
            List<Group> filteredGroups = new java.util.ArrayList<>();
            for (Group group : allGroups) {
                if (group.getMembers().size() > 2) {
                    filteredGroups.add(group);
                }
            }
            sortGroupsByLatestMessage(filteredGroups);
            groupAdapter.submitList(filteredGroups);
            binding.emptyState.setVisibility(
                    filteredGroups.isEmpty() ? View.VISIBLE : View.GONE
            );
        }
    }

    @Override
    public void onSearchQuery(String query) {
        if (currentAdapter == groupAdapter) {
            // Search in groups (only groups with >2 members)
            List<Group> allGroups = groupRepository.getAllGroups();
            // First filter: only groups with >2 members
            List<Group> groupsWithMoreThan2Members = new java.util.ArrayList<>();
            for (Group group : allGroups) {
                if (group.getMembers().size() > 2) {
                    groupsWithMoreThan2Members.add(group);
                }
            }
            
            if (query == null || query.trim().isEmpty()) {
                // Sort by latest message timestamp (descending)
                sortGroupsByLatestMessage(groupsWithMoreThan2Members);
                groupAdapter.submitList(groupsWithMoreThan2Members);
            } else {
                String q = query.toLowerCase().trim();
                List<Group> filtered = new java.util.ArrayList<>();
                for (Group group : groupsWithMoreThan2Members) {
                    if (group.getName() != null && group.getName().toLowerCase().contains(q)) {
                        filtered.add(group);
                    }
                }
                // Sort by latest message timestamp (descending)
                sortGroupsByLatestMessage(filtered);
                groupAdapter.submitList(filtered);
            }
        } else {
            // Search in contacts
            viewModel.search(query);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}