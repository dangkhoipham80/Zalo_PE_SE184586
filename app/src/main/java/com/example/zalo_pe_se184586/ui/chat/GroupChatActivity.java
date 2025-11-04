package com.example.zalo_pe_se184586.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.databinding.ActivityGroupChatBinding;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.model.Message;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP = "extra_group";

    private ActivityGroupChatBinding binding;
    private GroupChatViewModel viewModel;
    private MessageAdapter adapter;

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

        // Make members clickable to show group info
        binding.membersText.setOnClickListener(v -> showGroupInfo());
    }

    private void updateUI(Group group) {
        if (group == null) {
            return;
        }

        // Update toolbar
        binding.toolbar.setTitle(group.getName());
        binding.toolbar.setSubtitle(getString(R.string.members_count_format, group.getMembers().size()));

        // Update members list
        binding.membersText.setText(joinMemberNames(group.getMembers()));

        // Update messages
        List<Message> messages = group.getMessages();
        adapter.submitList(messages);

        // Scroll to latest message
        if (!messages.isEmpty()) {
            binding.messagesRecycler.scrollToPosition(messages.size() - 1);
        }
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

        StringBuilder info = new StringBuilder();
        info.append("Group Name: ").append(group.getName()).append("\n\n");
        info.append("Members (").append(group.getMembers().size()).append("):\n");
        for (Contact member : group.getMembers()) {
            info.append("• ").append(member.getName()).append("\n");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.group_info_title)
                .setMessage(info.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}