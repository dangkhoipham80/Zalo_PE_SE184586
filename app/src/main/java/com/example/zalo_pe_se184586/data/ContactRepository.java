package com.example.zalo_pe_se184586.data;

import com.example.zalo_pe_se184586.model.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContactRepository {
    private static volatile ContactRepository instance;
    private final List<Contact> contacts = new ArrayList<>();
    private final Map<String, Contact> contactsById = new HashMap<>();
    private final Contact currentUser = new Contact("self", "You", 0, "000-000-0000");

    private ContactRepository() {
        seedContacts();
    }

    public static ContactRepository getInstance() {
        if (instance == null) {
            synchronized (ContactRepository.class) {
                if (instance == null) {
                    instance = new ContactRepository();
                }
            }
        }
        return instance;
    }

    public List<Contact> getAllContacts() {
        return new ArrayList<>(contacts);
    }

    public List<Contact> searchContacts(String query) {
        String safeQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (safeQuery.isEmpty()) {
            return getAllContacts();
        }
        List<Contact> filtered = new ArrayList<>();
        for (Contact contact : contacts) {
            String name = contact.getName();
            String phone = contact.getPhone();
            if ((name != null && name.toLowerCase(Locale.ROOT).contains(safeQuery))
                    || (phone != null && phone.replace(" ", "").contains(safeQuery))) {
                filtered.add(contact);
            }
        }
        return filtered;
    }

    public Contact getContactById(String id) {
        if (id == null) {
            return null;
        }
        if (currentUser.getId().equals(id)) {
            return currentUser;
        }
        return contactsById.get(id);
    }

    public Contact getCurrentUser() {
        return currentUser;
    }

    private void addContact(Contact contact) {
        contacts.add(contact);
        contactsById.put(contact.getId(), contact);
    }

    private void seedContacts() {
        addContact(new Contact("1", "Alice Nguyen", 0, "0901 111 001"));
        addContact(new Contact("2", "Bao Tran", 0, "0901 111 002"));
        addContact(new Contact("3", "Chi Le", 0, "0901 111 003"));
        addContact(new Contact("4", "Duy Pham", 0, "0901 111 004"));
        addContact(new Contact("5", "Emily Vo", 0, "0901 111 005"));
        addContact(new Contact("6", "Gia Bui", 0, "0901 111 006"));
        addContact(new Contact("7", "Hien Ho", 0, "0901 111 007"));
        addContact(new Contact("8", "Khoa Dao", 0, "0901 111 008"));
        addContact(new Contact("9", "Linh Phan", 0, "0901 111 009"));
        addContact(new Contact("10", "Minh Dang", 0, "0901 111 010"));
        addContact(new Contact("11", "Ngoc Do", 0, "0901 111 011"));
        addContact(new Contact("12", "Oanh Truong", 0, "0901 111 012"));
        addContact(new Contact("13", "Phuc Nguyen", 0, "0901 111 013"));
        addContact(new Contact("14", "Quang Bui", 0, "0901 111 014"));
        addContact(new Contact("15", "Rin Hoang", 0, "0901 111 015"));
        addContact(new Contact("16", "Son Le", 0, "0901 111 016"));
        addContact(new Contact("17", "Thao Vu", 0, "0901 111 017"));
        addContact(new Contact("18", "Uyên Tran", 0, "0901 111 018"));
        addContact(new Contact("19", "Vu Pham", 0, "0901 111 019"));
        addContact(new Contact("20", "Yen Dang", 0, "0901 111 020"));
    }
}
