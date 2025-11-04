package com.example.zalo_pe_se184586.data;

import android.content.Context;

import com.example.zalo_pe_se184586.model.Contact;

import java.util.Collections;
import java.util.List;

public class ContactRepository {
    private static volatile ContactRepository instance;
    private AppDatabase database;
    private Context context;

    private ContactRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(this.context);
    }

    public static ContactRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (ContactRepository.class) {
                if (instance == null) {
                    instance = new ContactRepository(context);
                }
            }
        }
        return instance;
    }

    public List<Contact> getContacts() {
        List<Contact> contacts = database.getAllContacts();
        return Collections.unmodifiableList(contacts);
    }

    public List<Contact> getFriends() {
        List<Contact> friends = database.getFriends();
        return Collections.unmodifiableList(friends);
    }

    public void addContact(Contact contact) {
        database.addContact(contact);
    }

    public void addFriend(String contactId) {
        database.addFriend(contactId);
    }
}
