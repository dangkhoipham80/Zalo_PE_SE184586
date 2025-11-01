package com.example.zalo_pe_se184586.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zalo_pe_se184586.databinding.ItemContactBinding;
import com.example.zalo_pe_se184586.model.Contact;

public class ContactAdapter extends ListAdapter<Contact, ContactAdapter.ContactViewHolder> {

    public ContactAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Contact> DIFF_CALLBACK = new DiffUtil.ItemCallback<Contact>() {
        @Override
        public boolean areItemsTheSame(@NonNull Contact oldItem, @NonNull Contact newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Contact oldItem, @NonNull Contact newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && equalsNullable(oldItem.getPhone(), newItem.getPhone());
        }

        private boolean equalsNullable(String first, String second) {
            if (first == null) {
                return second == null;
            }
            return first.equals(second);
        }
    };

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemContactBinding binding = ItemContactBinding.inflate(inflater, parent, false);
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {

        private final ItemContactBinding binding;

        ContactViewHolder(ItemContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Contact contact) {
            binding.nameText.setText(contact.getName());
            binding.phoneText.setText(contact.getPhone());
            String name = contact.getName();
            if (name != null && !name.isEmpty()) {
                binding.avatarText.setText(name.substring(0, 1).toUpperCase());
            } else {
                binding.avatarText.setText("?");
            }
        }
    }
}
