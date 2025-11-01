package com.example.zalo_pe_se184586.data;

import com.example.zalo_pe_se184586.model.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactRepository {
    private static volatile ContactRepository instance;
    private final List<Contact> contacts;

    private ContactRepository() {
        contacts = new ArrayList<>();
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

    public List<Contact> getContacts() {
        return Collections.unmodifiableList(contacts);
    }

    private void seedContacts() {
        contacts.add(new Contact("1", "Alice Nguyen", ""));
        contacts.add(new Contact("2", "Bao Tran", ""));
        contacts.add(new Contact("3", "Chi Le", ""));
        contacts.add(new Contact("4", "Duy Pham", ""));
        contacts.add(new Contact("5", "Emily Vo", ""));
        contacts.add(new Contact("6", "Gia Bui", ""));
        contacts.add(new Contact("7", "Hien Ho", ""));
        contacts.add(new Contact("8", "Khoa Dao", ""));
        contacts.add(new Contact("9", "Linh Phan", ""));
        contacts.add(new Contact("10", "Minh Dang", ""));
    }
}
