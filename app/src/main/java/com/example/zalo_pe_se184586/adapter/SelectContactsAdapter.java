package com.example.zalo_pe_se184586.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zalo_pe_se184586.databinding.ItemSelectContactBinding;
import com.example.zalo_pe_se184586.model.Contact;

import java.util.HashSet;
import java.util.Set;

public class SelectContactsAdapter extends ListAdapter<Contact, SelectContactsAdapter.ContactViewHolder> {

    public interface OnContactToggleListener {
        void onContactToggled(Contact contact);
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

    private final OnContactToggleListener toggleListener;
    private final Set<String> selectedIds = new HashSet<>();

    public SelectContactsAdapter(OnContactToggleListener toggleListener) {
        super(DIFF_CALLBACK);
        this.toggleListener = toggleListener;
    }

    public void setSelectedIds(Set<String> ids) {
        selectedIds.clear();
        if (ids != null) {
            selectedIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSelectContactBinding binding = ItemSelectContactBinding.inflate(inflater, parent, false);
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private final ItemSelectContactBinding binding;

        ContactViewHolder(ItemSelectContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Contact contact) {
            binding.nameText.setText(contact.getName());
            binding.phoneText.setText(contact.getPhone());
            String name = contact.getName();
            binding.avatarText.setText(name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "?");
            binding.checkbox.setOnCheckedChangeListener(null);
            boolean selected = selectedIds.contains(contact.getId());
            binding.checkbox.setChecked(selected);
            View.OnClickListener toggle = v -> toggleListener.onContactToggled(contact);
            binding.getRoot().setOnClickListener(toggle);
            binding.checkbox.setOnClickListener(toggle);
        }
    }
}
