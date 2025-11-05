package com.example.zalo_pe_se184586.data;

import android.content.Context;

import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.FriendRequest;

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

    public List<Contact> getNonFriendContacts() {
        // Get all contacts that are not friends
        List<Contact> allContacts = database.getAllContacts();
        List<Contact> friends = database.getFriends();
        List<String> friendIds = new java.util.ArrayList<>();
        for (Contact friend : friends) {
            friendIds.add(friend.getId());
        }
        
        List<Contact> nonFriends = new java.util.ArrayList<>();
        for (Contact contact : allContacts) {
            if (!friendIds.contains(contact.getId())) {
                nonFriends.add(contact);
            }
        }
        return Collections.unmodifiableList(nonFriends);
    }

    public List<FriendRequest> getPendingFriendRequests() {
        return database.getPendingFriendRequests();
    }

    public void addFriendRequest(String fromPhoneNumber, String fromName, String fromContactId) {
        database.addFriendRequest(fromPhoneNumber, fromName, fromContactId);
    }

    public void acceptFriendRequest(String requestId) {
        database.updateFriendRequestStatus(requestId, "accepted");
        // If the request has a contactId, add as friend
        FriendRequest request = null;
        for (FriendRequest fr : database.getPendingFriendRequests()) {
            if (fr.getId().equals(requestId)) {
                request = fr;
                break;
            }
        }
        // Note: We need to reload from DB to get the request after it's been updated
        // For simplicity, we'll handle this in the caller
    }

    public void rejectFriendRequest(String requestId) {
        database.updateFriendRequestStatus(requestId, "rejected");
    }

    public Contact findContactByPhoneNumber(String phoneNumber) {
        return database.findContactByPhoneNumber(phoneNumber);
    }

    /**
     * Update contact's avatar URL
     */
    public void updateContactAvatar(String contactId, String avatarUrl) {
        database.updateContactAvatar(contactId, avatarUrl);
    }
}
