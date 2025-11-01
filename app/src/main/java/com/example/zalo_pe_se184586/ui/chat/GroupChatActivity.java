package com.example.zalo_pe_se184586.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.adapter.MessageAdapter;
import com.example.zalo_pe_se184586.databinding.ActivityGroupChatBinding;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Message;
import com.example.zalo_pe_se184586.vm.GroupChatViewModel;

import java.util.ArrayList;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "extra_group_id";

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

        viewModel = new ViewModelProvider(this).get(GroupChatViewModel.class);
        adapter = new MessageAdapter(viewModel::resolveSenderName);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.messagesRecycler.setLayoutManager(layoutManager);
        binding.messagesRecycler.setAdapter(adapter);

        String groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        if (groupId != null) {
            viewModel.setInitialGroupId(groupId);
        } else if (!viewModel.hasGroup()) {
            finish();
            return;
        }

        binding.messageInput.setText(viewModel.getDraft());
        if (binding.messageInput.getText() != null) {
            binding.messageInput.setSelection(binding.messageInput.getText().length());
        }
        binding.messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateDraft(s != null ? s.toString() : "");
            }
        });

        viewModel.getGroup().observe(this, group -> {
            if (group == null) {
                return;
            }
            binding.toolbar.setTitle(group.getName());
            binding.toolbar.setSubtitle(getString(R.string.members_count_format, group.getMemberIds().size()));
        });

        viewModel.getMemberContacts().observe(this, contacts -> binding.membersText.setText(joinMemberNames(contacts)));

        viewModel.getMessages().observe(this, messages -> {
            if (messages == null) {
                adapter.submitList(null);
                return;
            }
            List<Message> copy = new ArrayList<>(messages);
            adapter.submitList(copy);
            if (!copy.isEmpty()) {
                binding.messagesRecycler.scrollToPosition(copy.size() - 1);
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
        viewModel.updateDraft("");
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
