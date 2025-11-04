package com.example.zalo_pe_se184586.ui.contacts;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zalo_pe_se184586.databinding.ItemContactBinding;
import com.example.zalo_pe_se184586.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragmentAdapter extends RecyclerView.Adapter<ContactsFragmentAdapter.ContactViewHolder> {

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    private final List<Contact> contacts = new ArrayList<>();
    private final OnContactClickListener listener;

    public ContactsFragmentAdapter(OnContactClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Contact> newContacts) {
        contacts.clear();
        if (newContacts != null) {
            contacts.addAll(newContacts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemContactBinding binding = ItemContactBinding.inflate(inflater, parent, false);
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        private final ItemContactBinding binding;

        ContactViewHolder(ItemContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Contact contact) {
            String name = contact.getName();
            binding.nameText.setText(name);
            if (name != null && !name.isEmpty()) {
                binding.avatarText.setText(name.substring(0, 1).toUpperCase());
            } else {
                binding.avatarText.setText("?");
            }

            binding.getRoot().setOnClickListener(v -> listener.onContactClick(contact));
        }
    }
}