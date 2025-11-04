package com.example.zalo_pe_se184586.ui.contacts;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zalo_pe_se184586.data.ContactRepository;
import com.example.zalo_pe_se184586.model.Contact;
import java.util.ArrayList;
import java.util.List;

public class ContactsViewModel extends AndroidViewModel {

    private final ContactRepository contactRepository;
    private final MutableLiveData<List<Contact>> contacts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Contact>> filteredContacts = new MutableLiveData<>(new ArrayList<>());

    private int currentFilter = 0; // 0 = All, 1 = Friends, 2 = Groups

    public ContactsViewModel(Application application) {
        super(application);
        contactRepository = ContactRepository.getInstance(application);
        loadContacts();
    }

    private void loadContacts() {
        List<Contact> allContacts = contactRepository.getContacts();
        contacts.setValue(allContacts);
        filterContacts();
    }

    public LiveData<List<Contact>> getFilteredContacts() {
        return filteredContacts;
    }

    public void setFilter(int filterType) {
        currentFilter = filterType;
        filterContacts();
    }

    public void search(String query) {
        List<Contact> list = contacts.getValue();
        if (list == null) return;
        List<Contact> result = new ArrayList<>();
        for (Contact c : list) {
            if (c.getName().toLowerCase().contains(query.toLowerCase())) {
                result.add(c);
            }
        }
        filteredContacts.setValue(result);
    }

    private void filterContacts() {
        List<Contact> list = contacts.getValue();
        if (list == null) return;
        
        List<Contact> filtered;
        switch (currentFilter) {
            case 1: // Friends - chỉ hiển thị friends
                filtered = contactRepository.getFriends();
                break;
            case 2: // Groups - không hiển thị contacts ở tab này, sẽ xử lý riêng trong Fragment
                filtered = new ArrayList<>(); // Empty list, Fragment sẽ xử lý Groups riêng
                break;
            default: // All - hiển thị tất cả contacts (friends + non-friends)
                filtered = new ArrayList<>(list);
                break;
        }
        filteredContacts.setValue(filtered);
    }

    public int getCurrentFilter() {
        return currentFilter;
    }
}
