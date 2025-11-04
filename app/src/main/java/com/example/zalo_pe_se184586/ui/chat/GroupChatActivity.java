package com.example.zalo_pe_se184586.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.data.AppDatabase;
import com.example.zalo_pe_se184586.data.ContactRepository;
import com.example.zalo_pe_se184586.data.GroupRepository;
import com.example.zalo_pe_se184586.databinding.ActivityGroupChatBinding;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.model.Message;
import com.example.zalo_pe_se184586.ui.select.SelectContactsActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import android.view.View;

public class GroupChatActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP = "extra_group";

    private ActivityGroupChatBinding binding;
    private GroupChatViewModel viewModel;
    private MessageAdapter adapter;
    private GroupRepository groupRepository;
    private ContactRepository contactRepository;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup RecyclerView
        adapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.messagesRecycler.setLayoutManager(layoutManager);
        binding.messagesRecycler.setAdapter(adapter);

        // Initialize repositories
        groupRepository = GroupRepository.getInstance(getApplication());
        contactRepository = ContactRepository.getInstance(getApplication());
        appDatabase = AppDatabase.getInstance(getApplication());

        // Initialize ViewModel with proper factory for SavedStateHandle support
        GroupChatViewModelFactory factory = new GroupChatViewModelFactory(this, getApplication());
        viewModel = new ViewModelProvider(this, factory).get(GroupChatViewModel.class);

        // Handle initial group data from intent (only on first creation)
        if (savedInstanceState == null) {
            Group group = getIntent().getParcelableExtra(EXTRA_GROUP);
            if (group != null) {
                viewModel.setInitialGroup(group);
            } else if (!viewModel.hasGroup()) {
                // No group data available, cannot proceed
                finish();
                return;
            }
        }

        // Observe group changes (handles rotation and real-time updates)
        viewModel.getGroup().observe(this, this::updateUI);

        // Setup click listeners
        binding.sendButton.setOnClickListener(v -> submitMessage());
        binding.messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitMessage();
                return true;
            }
            return false;
        });

        // Members text is now hidden, no click listener needed
    }

    private void updateUI(Group group) {
        if (group == null) {
            return;
        }

        // Update toolbar - chỉ hiển thị tên group
        binding.toolbar.setTitle(group.getName());
        
        // Ẩn subtitle và members text
        binding.toolbar.setSubtitle(null);
        binding.membersLabel.setVisibility(View.GONE);
        binding.membersText.setVisibility(View.GONE);

        // Update messages
        List<Message> messages = group.getMessages();
        adapter.submitList(messages);

        // Scroll to latest message
        if (!messages.isEmpty()) {
            binding.messagesRecycler.scrollToPosition(messages.size() - 1);
        }
        
        // Invalidate menu to update visibility of items based on group owner
        invalidateOptionsMenu();
    }

    /**
     * Lấy danh sách members để hiển thị
     * - Với chat 1-1 (2 members): chỉ hiển thị người kia (giống Zalo)
     * - Với group chat (>2 members): hiển thị tất cả (trừ current user)
     */
    private List<Contact> getDisplayMembers(Group group) {
        List<Contact> allMembers = group.getMembers();
        String currentUserId = AppDatabase.getInstance(this).getCurrentUserId();
        
        // Nếu là chat 1-1 (chính xác 2 members), chỉ hiển thị người kia
        if (allMembers.size() == 2) {
            // Tìm member khác với current user
            for (Contact member : allMembers) {
                if (!member.getId().equals(currentUserId)) {
                    return java.util.Collections.singletonList(member);
                }
            }
        }
        
        // Với group chat, hiển thị tất cả trừ current user
        List<Contact> displayMembers = new java.util.ArrayList<>();
        for (Contact member : allMembers) {
            if (!member.getId().equals(currentUserId)) {
                displayMembers.add(member);
            }
        }
        return displayMembers;
    }

    private String joinMemberNames(List<Contact> members) {
        if (members == null || members.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            builder.append(members.get(i).getName());
            if (i < members.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private void submitMessage() {
        CharSequence text = binding.messageInput.getText();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        viewModel.sendMessage(text.toString());
        binding.messageInput.setText("");
    }

    private void showGroupInfo() {
        Group group = viewModel.getGroup().getValue();
        if (group == null) {
            return;
        }

        List<Contact> displayMembers = getDisplayMembers(group);
        StringBuilder info = new StringBuilder();
        info.append("Group Name: ").append(group.getName()).append("\n\n");
        info.append("Members (").append(displayMembers.size()).append("):\n");
        for (Contact member : displayMembers) {
            info.append("• ").append(member.getName()).append("\n");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.group_info_title)
                .setMessage(info.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_chat, menu);
        
        // Chỉ hiển thị "Delete Group" nếu current user là chủ group
        Group group = viewModel.getGroup().getValue();
        if (group != null) {
            MenuItem deleteItem = menu.findItem(R.id.action_delete_group);
            if (deleteItem != null) {
                boolean isOwner = isGroupOwner(group);
                deleteItem.setVisible(isOwner);
            }
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_view_members) {
            showMembersList();
            return true;
        } else if (itemId == R.id.action_add_member) {
            showAddMemberDialog();
            return true;
        } else if (itemId == R.id.action_remove_member) {
            showRemoveMemberDialog();
            return true;
        } else if (itemId == R.id.action_change_nickname) {
            showChangeNicknameDialog();
            return true;
        } else if (itemId == R.id.action_promote_member) {
            showPromoteMemberDialog();
            return true;
        } else if (itemId == R.id.action_delete_group) {
            showDeleteGroupDialog();
            return true;
        } else if (itemId == R.id.action_group_settings) {
            showGroupSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMembersList() {
        Group group = viewModel.getGroup().getValue();
        if (group == null) {
            return;
        }

        List<Contact> displayMembers = getDisplayMembers(group);
        StringBuilder info = new StringBuilder();
        info.append("Group: ").append(group.getName()).append("\n\n");
        info.append("Members (").append(displayMembers.size()).append("):\n\n");
        for (Contact member : displayMembers) {
            info.append("• ").append(member.getName());
            if (member.getPhoneNumber() != null && !member.getPhoneNumber().isEmpty()) {
                info.append(" (").append(member.getPhoneNumber()).append(")");
            }
            info.append("\n");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.action_view_members)
                .setMessage(info.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showAddMemberDialog() {
        Group group = viewModel.getGroup().getValue();
        if (group == null) {
            return;
        }

        // Get all contacts
        List<Contact> allContacts = contactRepository.getContacts();
        
        // Filter out contacts already in group
        List<Contact> availableContacts = new ArrayList<>();
        List<String> memberIds = new ArrayList<>();
        for (Contact member : group.getMembers()) {
            memberIds.add(member.getId());
        }
        
        for (Contact contact : allContacts) {
            if (!memberIds.contains(contact.getId())) {
                availableContacts.add(contact);
            }
        }

        if (availableContacts.isEmpty()) {
            Toast.makeText(this, R.string.no_contacts_available, Toast.LENGTH_SHORT).show();
            return;
        }

        // Build array of contact names
        String[] contactNames = new String[availableContacts.size()];
        for (int i = 0; i < availableContacts.size(); i++) {
            contactNames[i] = availableContacts.get(i).getName();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_member_to_add)
                .setItems(contactNames, (dialog, which) -> {
                    Contact selectedContact = availableContacts.get(which);
                    // Check again if already in group (just in case)
                    boolean alreadyInGroup = false;
                    for (Contact member : group.getMembers()) {
                        if (member.getId().equals(selectedContact.getId())) {
                            alreadyInGroup = true;
                            break;
                        }
                    }
                    
                    if (alreadyInGroup) {
                        Toast.makeText(this, getString(R.string.member_already_in_group, selectedContact.getName()), Toast.LENGTH_SHORT).show();
                    } else {
                        groupRepository.addMemberToGroup(group.getId(), selectedContact);
                        // Refresh group to update UI
                        Group updatedGroup = appDatabase.getGroup(group.getId());
                        if (updatedGroup != null) {
                            viewModel.setInitialGroup(updatedGroup);
                        }
                        Toast.makeText(this, getString(R.string.member_added_success, selectedContact.getName()), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showRemoveMemberDialog() {
        Group group = viewModel.getGroup().getValue();
        if (group == null) {
            return;
        }

        List<Contact> displayMembers = getDisplayMembers(group);
        
        // Cannot remove if only 1 member left (current user + 1 other)
        if (displayMembers.size() <= 1) {
            Toast.makeText(this, R.string.cannot_remove_last_member, Toast.LENGTH_SHORT).show();
            return;
        }

        // Build array of member names
        String[] memberNames = new String[displayMembers.size()];
        for (int i = 0; i < displayMembers.size(); i++) {
            memberNames[i] = displayMembers.get(i).getName();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_member_to_remove)
                .setItems(memberNames, (dialog, which) -> {
                    Contact selectedMember = displayMembers.get(which);
                    // Remove member from group
                    removeMemberFromGroup(group.getId(), selectedMember);
                    Toast.makeText(this, getString(R.string.member_removed_success, selectedMember.getName()), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void removeMemberFromGroup(String groupId, Contact member) {
        // Get current group
        Group group = groupRepository.getGroupLiveData(groupId).getValue();
        if (group == null) {
            return;
        }

        // Remove from database
        appDatabase.removeMemberFromGroup(groupId, member.getId());
        
        // Reload group from database
        Group updatedGroup = appDatabase.getGroup(groupId);
        if (updatedGroup != null) {
            // Update ViewModel
            viewModel.setInitialGroup(updatedGroup);
        }
    }

    private void showGroupSettings() {
        Group group = viewModel.getGroup().getValue();
        if (group == null) {
            return;
        }

        List<Contact> displayMembers = getDisplayMembers(group);
        StringBuilder settings = new StringBuilder();
        settings.append("Group Name: ").append(group.getName()).append("\n\n");
        settings.append("Total Members: ").append(displayMembers.size()).append("\n");
        settings.append("Total Messages: ").append(group.getMessages() != null ? group.getMessages().size() : 0).append("\n\n");
        settings.append("Settings options:\n");
        settings.append("• Change group name (coming soon)\n");
        settings.append("• Change group avatar (coming soon)\n");
        settings.append("• Notification settings (coming soon)");

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.group_settings_title)
                .setMessage(settings.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * Kiểm tra xem current user có phải chủ group không
     * Chủ group là member đầu tiên trong danh sách
     */
    private boolean isGroupOwner(Group group) {
        String currentUserId = appDatabase.getCurrentUserId();
        List<Contact> members = group.getMembers();
        if (members != null && !members.isEmpty()) {
            // Member đầu tiên là chủ group
            Contact owner = members.get(0);
            return owner.getId().equals(currentUserId);
        }
        return false;
    }

    private void showChangeNicknameDialog() {
        Group group = viewModel.getGroup().getValue();
        if (group == null) {
            return;
        }

        List<Contact> displayMembers = getDisplayMembers(group);
        if (displayMembers.isEmpty()) {
            Toast.makeText(this, "No members available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build array of member names
        String[] memberNames = new String[displayMembers.size()];
        for (int i = 0; i < displayMembers.size(); i++) {
            memberNames[i] = displayMembers.get(i).getName();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Nickname")
                .setItems(memberNames, (dialog, which) -> {
                    Contact selectedMember = displayMembers.get(which);
                    // Show input dialog for new nickname
                    showNicknameInputDialog(selectedMember);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showNicknameInputDialog(Contact member) {
        android.view.View inputView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        com.google.android.material.textfield.TextInputLayout inputLayout = new com.google.android.material.textfield.TextInputLayout(this);
        inputLayout.setHint("Enter nickname for " + member.getName());
        inputLayout.setPadding(32, 0, 32, 0);
        com.google.android.material.textfield.TextInputEditText editText = new com.google.android.material.textfield.TextInputEditText(inputLayout.getContext());
        editText.setLayoutParams(new com.google.android.material.textfield.TextInputLayout.LayoutParams(
                com.google.android.material.textfield.TextInputLayout.LayoutParams.MATCH_PARENT,
                com.google.android.material.textfield.TextInputLayout.LayoutParams.WRAP_CONTENT));
        inputLayout.addView(editText);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Nickname")
                .setView(inputLayout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String nickname = editText.getText() != null ? editText.getText().toString().trim() : "";
                    if (!nickname.isEmpty()) {
                        // TODO: Implement nickname change in database
                        Toast.makeText(this, "Nickname changed to: " + nickname, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showPromoteMemberDialog() {
        Group group = viewModel.getGroup().getValue();
        if (group == null) {
            return;
        }

        // Chỉ chủ group mới được promote members
        if (!isGroupOwner(group)) {
            Toast.makeText(this, "Only group owner can promote members", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Contact> displayMembers = getDisplayMembers(group);
        if (displayMembers.isEmpty()) {
            Toast.makeText(this, "No members available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build array of member names
        String[] memberNames = new String[displayMembers.size()];
        for (int i = 0; i < displayMembers.size(); i++) {
            memberNames[i] = displayMembers.get(i).getName();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Promote to Admin")
                .setItems(memberNames, (dialog, which) -> {
                    Contact selectedMember = displayMembers.get(which);
                    // TODO: Implement promote to admin in database
                    Toast.makeText(this, selectedMember.getName() + " promoted to admin", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showDeleteGroupDialog() {
        Group group = viewModel.getGroup().getValue();
        if (group == null) {
            return;
        }

        // Chỉ chủ group mới được xóa group
        if (!isGroupOwner(group)) {
            Toast.makeText(this, "Only group owner can delete the group", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_group_title)
                .setMessage(getString(R.string.delete_group_message, group.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    groupRepository.deleteGroup(group.getId());
                    Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}