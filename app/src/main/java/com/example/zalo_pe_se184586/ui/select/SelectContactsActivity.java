package com.example.zalo_pe_se184586.ui.select;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.data.GroupRepository;
import com.example.zalo_pe_se184586.databinding.ActivitySelectContactsBinding;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.ui.chat.GroupChatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class SelectContactsActivity extends AppCompatActivity implements SelectContactsAdapter.OnContactToggleListener {

    private ActivitySelectContactsBinding binding;
    private SelectContactsViewModel viewModel;
    private SelectContactsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(SelectContactsViewModel.class);
        adapter = new SelectContactsAdapter(this);
        binding.contactsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.contactsRecycler.setAdapter(adapter);

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        viewModel.getFilteredContacts().observe(this, contacts -> {
            adapter.submitList(contacts);
            binding.emptyState.setVisibility(contacts == null || contacts.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getSelectedContactIds().observe(this, ids -> {
            adapter.updateSelection(ids);
            int count = ids != null ? ids.size() : 0;
            binding.selectionCountText.setText(getString(R.string.selected_count_format, count));
        });

        viewModel.isCreateEnabled().observe(this, enabled -> binding.createGroupButton.setEnabled(Boolean.TRUE.equals(enabled)));

        binding.createGroupButton.setOnClickListener(v -> showGroupNameDialog());
    }

    private void showGroupNameDialog() {
        final TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setHint(getString(R.string.prompt_group_name));
        int padding = getResources().getDimensionPixelSize(R.dimen.dialog_content_padding);
        inputLayout.setPadding(padding, padding, padding, 0);
        final TextInputEditText editText = new TextInputEditText(inputLayout.getContext());
        editText.setLayoutParams(new TextInputLayout.LayoutParams(
                TextInputLayout.LayoutParams.MATCH_PARENT,
                TextInputLayout.LayoutParams.WRAP_CONTENT));
        inputLayout.addView(editText);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title_create_group)
                .setView(inputLayout)
                .setPositiveButton(R.string.action_create_group, null)
                .setNegativeButton(android.R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            CharSequence text = editText.getText();
            String groupName = text != null ? text.toString().trim() : "";
            if (groupName.isEmpty()) {
                inputLayout.setError(getString(R.string.error_group_name_required));
                return;
            }
            inputLayout.setError(null);
            createGroup(groupName);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void createGroup(String groupName) {
        List<Contact> selectedContacts = viewModel.getSelectedContacts();
        if (selectedContacts.size() < 2) {
            Toast.makeText(this, R.string.error_min_members, Toast.LENGTH_SHORT).show();
            return;
        }
        Group group = GroupRepository.getInstance(this).createGroup(groupName, selectedContacts);
        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra(GroupChatActivity.EXTRA_GROUP, group);
        startActivity(intent);
        finish();
    }

    @Override
    public void onContactToggled(Contact contact) {
        viewModel.toggleContact(contact);
    }
}
