package com.example.zalo_pe_se184586.ui.main;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zalo_pe_se184586.databinding.ItemChatPreviewBinding;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.model.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(Group group);
    }

    private final List<Group> groups = new ArrayList<>();
    private final OnChatClickListener listener;

    public ChatListAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Group> newGroups) {
        groups.clear();
        if (newGroups != null) {
            groups.addAll(newGroups);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemChatPreviewBinding binding = ItemChatPreviewBinding.inflate(inflater, parent, false);
        return new ChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        private final ItemChatPreviewBinding binding;

        ChatViewHolder(ItemChatPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Group group) {
            String name = group.getName();
            binding.groupNameText.setText(name);

            if (name != null && !name.isEmpty()) {
                binding.avatarText.setText(name.substring(0, 1).toUpperCase());
            } else {
                binding.avatarText.setText("?");
            }

            binding.memberCountText.setText(group.getMembers().size() + " members");

            Message lastMessage = group.getLastMessage();
            if (lastMessage != null) {
                String preview = lastMessage.getSenderName() + ": " + lastMessage.getContent();
                binding.lastMessageText.setText(preview);
                binding.timeText.setText(formatTime(lastMessage.getTimestamp()));
            } else {
                binding.lastMessageText.setText("No messages yet");
                binding.timeText.setText("");
            }

            binding.getRoot().setOnClickListener(v -> listener.onChatClick(group));
        }

        private String formatTime(long timestamp) {
            Calendar msgCal = Calendar.getInstance();
            msgCal.setTimeInMillis(timestamp);

            Calendar now = Calendar.getInstance();

            if (isSameDay(msgCal, now)) {
                return DateFormat.getTimeFormat(binding.getRoot().getContext())
                        .format(new Date(timestamp));
            } else if (isYesterday(msgCal, now)) {
                return "Yesterday";
            } else {
                return DateFormat.getDateFormat(binding.getRoot().getContext())
                        .format(new Date(timestamp));
            }
        }

        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }

        private boolean isYesterday(Calendar cal1, Calendar cal2) {
            Calendar yesterday = (Calendar) cal2.clone();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            return isSameDay(cal1, yesterday);
        }
    }
}