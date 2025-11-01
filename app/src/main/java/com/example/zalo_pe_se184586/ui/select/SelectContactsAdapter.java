package com.example.zalo_pe_se184586.ui.select;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zalo_pe_se184586.databinding.ItemSelectContactBinding;
import com.example.zalo_pe_se184586.model.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectContactsAdapter extends RecyclerView.Adapter<SelectContactsAdapter.ContactViewHolder> {

    public interface OnContactToggleListener {
        void onContactToggled(Contact contact);
    }

    private final List<Contact> items = new ArrayList<>();
    private final Set<String> selectedIds = new HashSet<>();
    private final OnContactToggleListener toggleListener;

    public SelectContactsAdapter(OnContactToggleListener toggleListener) {
        this.toggleListener = toggleListener;
    }

    public void submitList(List<Contact> contacts) {
        items.clear();
        if (contacts != null) {
            items.addAll(contacts);
        }
        notifyDataSetChanged();
    }

    public void updateSelection(Set<String> ids) {
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
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        private final ItemSelectContactBinding binding;

        ContactViewHolder(ItemSelectContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Contact contact) {
            binding.nameText.setText(contact.getName());
            String name = contact.getName();
            if (name != null && !name.isEmpty()) {
                binding.avatarText.setText(name.substring(0, 1));
            } else {
                binding.avatarText.setText("?");
            }
            binding.checkbox.setOnCheckedChangeListener(null);
            binding.checkbox.setChecked(selectedIds.contains(contact.getId()));
            View.OnClickListener listener = v -> toggleListener.onContactToggled(contact);
            binding.getRoot().setOnClickListener(listener);
            binding.checkbox.setOnClickListener(listener);
        }
    }
}
