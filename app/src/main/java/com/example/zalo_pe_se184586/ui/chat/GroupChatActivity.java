package com.example.zalo_pe_se184586.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.databinding.ActivityGroupChatBinding;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.model.Message;

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

        adapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.messagesRecycler.setLayoutManager(layoutManager);
        binding.messagesRecycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(GroupChatViewModel.class);

        Group group = getIntent().getParcelableExtra(EXTRA_GROUP);
        if (group != null) {
            viewModel.setInitialGroup(group);
        } else if (!viewModel.hasGroup()) {
            finish();
            return;
        }

        viewModel.getGroup().observe(this, updatedGroup -> {
            if (updatedGroup == null) {
                return;
            }
            binding.toolbar.setTitle(updatedGroup.getName());
            binding.toolbar.setSubtitle(getString(R.string.members_count_format, updatedGroup.getMembers().size()));
            binding.membersText.setText(joinMemberNames(updatedGroup.getMembers()));
            List<Message> messages = updatedGroup.getMessages();
            adapter.submitList(messages);
            if (!messages.isEmpty()) {
                binding.messagesRecycler.scrollToPosition(messages.size() - 1);
            }
        });

        binding.sendButton.setOnClickListener(v -> submitMessage());
        binding.messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitMessage();
                return true;
            }
            return false;
        });
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
