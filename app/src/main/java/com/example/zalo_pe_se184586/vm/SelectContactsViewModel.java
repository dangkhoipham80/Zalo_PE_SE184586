package com.example.zalo_pe_se184586.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.zalo_pe_se184586.data.ContactRepository;
import com.example.zalo_pe_se184586.model.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectContactsViewModel extends ViewModel {
    private final ContactRepository contactRepository = ContactRepository.getInstance();
    private final MutableLiveData<List<Contact>> filteredContacts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Set<String>> selectedContactIds = new MutableLiveData<>(new HashSet<>());
    private final MediatorLiveData<Boolean> createEnabled = new MediatorLiveData<>();

    public SelectContactsViewModel() {
        filteredContacts.setValue(contactRepository.getAllContacts());
        createEnabled.addSource(selectedContactIds, ids -> createEnabled.setValue(ids != null && ids.size() >= 2));
    }

    public LiveData<List<Contact>> getFilteredContacts() {
        return filteredContacts;
    }

    public LiveData<Set<String>> getSelectedContactIds() {
        return selectedContactIds;
    }

    public LiveData<Boolean> isCreateEnabled() {
        return createEnabled;
    }

    public void toggleContact(Contact contact) {
        if (contact == null) {
            return;
        }
        Set<String> current = new HashSet<>(selectedContactIds.getValue());
        if (current.contains(contact.getId())) {
            current.remove(contact.getId());
        } else {
            current.add(contact.getId());
        }
        selectedContactIds.setValue(current);
    }

    public boolean isSelected(Contact contact) {
        Set<String> ids = selectedContactIds.getValue();
        return ids != null && contact != null && ids.contains(contact.getId());
    }

    public void setSearchQuery(String query) {
        filteredContacts.setValue(contactRepository.searchContacts(query));
    }

    public List<Contact> getSelectedContacts() {
        Set<String> ids = selectedContactIds.getValue();
        List<Contact> contacts = new ArrayList<>();
        if (ids == null) {
            return contacts;
        }
        for (String id : ids) {
            Contact contact = contactRepository.getContactById(id);
            if (contact != null) {
                contacts.add(contact);
            }
        }
        return contacts;
    }

    public List<String> getSelectedContactIdsSnapshot() {
        Set<String> ids = selectedContactIds.getValue();
        return ids == null ? new ArrayList<>() : new ArrayList<>(ids);
    }
}
