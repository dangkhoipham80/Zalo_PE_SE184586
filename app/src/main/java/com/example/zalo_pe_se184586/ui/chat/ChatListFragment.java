package com.example.zalo_pe_se184586.ui.chat;

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

import com.example.zalo_pe_se184586.databinding.FragmentChatListBinding;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.ui.chat.GroupChatActivity;
import com.example.zalo_pe_se184586.ui.main.ChatListAdapter;
// ⚠️ import đúng ViewModel ở package ui.main
import com.example.zalo_pe_se184586.ui.main.ChatListViewModel;
import com.example.zalo_pe_se184586.ui.main.MainActivity;

public class ChatListFragment extends Fragment implements MainActivity.SearchableFragment {

    private FragmentChatListBinding binding;
    private ChatListViewModel viewModel;
    private ChatListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Có thể dùng new ViewModelProvider(this) cũng được;
        // dùng requireActivity() nếu muốn share VM giữa nhiều fragment.
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);

        adapter = new ChatListAdapter(this::openGroupChat);
        binding.chatRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.chatRecycler.setAdapter(adapter);

        // Observe danh sách nhóm (chats)
        viewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
            adapter.submitList(groups);
            binding.emptyState.setVisibility(
                    groups == null || groups.isEmpty() ? View.VISIBLE : View.GONE
            );
        });
    }

    private void openGroupChat(Group group) {
        Intent intent = new Intent(requireContext(), GroupChatActivity.class);
        intent.putExtra(GroupChatActivity.EXTRA_GROUP, group);
        startActivity(intent);
    }

    @Override
    public void onSearchQuery(String query) {
        // forward xuống ViewModel
        viewModel.search(query != null ? query : "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
