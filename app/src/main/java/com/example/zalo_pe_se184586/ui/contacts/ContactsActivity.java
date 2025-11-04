package com.example.zalo_pe_se184586.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.data.ContactRepository;
import com.example.zalo_pe_se184586.databinding.ActivityContactsBinding;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.ui.select.SelectContactsActivity;

import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private ActivityContactsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        List<Contact> contacts = ContactRepository.getInstance(this).getContacts();
        ContactsAdapter adapter = new ContactsAdapter(contacts);
        binding.contactsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.contactsRecycler.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create_group) {
            Intent intent = new Intent(this, SelectContactsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
