package com.example.zalo_pe_se184586.adapter;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.databinding.ItemMessageBinding;
import com.example.zalo_pe_se184586.model.Message;

import java.util.Date;

public class MessageAdapter extends ListAdapter<Message, MessageAdapter.MessageViewHolder> {

    public interface SenderNameResolver {
        String resolveName(String senderId);
    }

    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getContent().equals(newItem.getContent())
                    && oldItem.getTimestamp() == newItem.getTimestamp()
                    && oldItem.getStatus() == newItem.getStatus()
                    && oldItem.getSenderId().equals(newItem.getSenderId());
        }
    };

    private final SenderNameResolver senderNameResolver;

    public MessageAdapter(SenderNameResolver senderNameResolver) {
        super(DIFF_CALLBACK);
        this.senderNameResolver = senderNameResolver;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMessageBinding binding = ItemMessageBinding.inflate(inflater, parent, false);
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemMessageBinding binding;

        MessageViewHolder(ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            binding.senderText.setText(senderNameResolver.resolveName(message.getSenderId()));
            binding.messageText.setText(message.getContent());
            CharSequence time = DateFormat.getTimeFormat(binding.getRoot().getContext())
                    .format(new Date(message.getTimestamp()));
            binding.timeText.setText(time);
            int statusTextRes = message.getStatus() == Message.Status.SEEN
                    ? R.string.message_status_seen
                    : R.string.message_status_sent;
            binding.statusText.setText(binding.getRoot().getContext().getString(statusTextRes));
        }
    }
}
