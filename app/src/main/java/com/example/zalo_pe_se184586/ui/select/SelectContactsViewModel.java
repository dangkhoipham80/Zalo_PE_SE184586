package com.example.zalo_pe_se184586.ui.select;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zalo_pe_se184586.data.ContactRepository;
import com.example.zalo_pe_se184586.model.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SelectContactsViewModel extends AndroidViewModel {

    private final List<Contact> allContacts = new ArrayList<>();
    private final MutableLiveData<List<Contact>> filteredContacts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Set<String>> selectedContactIds = new MutableLiveData<>(new HashSet<>());
    private final MediatorLiveData<Boolean> createEnabled = new MediatorLiveData<>();

    public SelectContactsViewModel(Application application) {
        super(application);
        allContacts.addAll(ContactRepository.getInstance(application).getContacts());
        filteredContacts.setValue(new ArrayList<>(allContacts));
        createEnabled.addSource(selectedContactIds, ids -> createEnabled.setValue(ids.size() >= 2));
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
        Set<String> current = new HashSet<>(selectedContactIds.getValue());
        if (current.contains(contact.getId())) {
            current.remove(contact.getId());
        } else {
            current.add(contact.getId());
        }
        selectedContactIds.setValue(current);
    }

    public boolean isSelected(Contact contact) {
        Set<String> current = selectedContactIds.getValue();
        return current != null && current.contains(contact.getId());
    }

    public void setSearchQuery(String query) {
        String safeQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (safeQuery.isEmpty()) {
            filteredContacts.setValue(new ArrayList<>(allContacts));
            return;
        }
        List<Contact> result = new ArrayList<>();
        for (Contact contact : allContacts) {
            if (contact.getName().toLowerCase(Locale.ROOT).contains(safeQuery)) {
                result.add(contact);
            }
        }
        filteredContacts.setValue(result);
    }

    public List<Contact> getSelectedContacts() {
        Set<String> current = selectedContactIds.getValue();
        List<Contact> selected = new ArrayList<>();
        if (current == null) {
            return selected;
        }
        for (Contact contact : allContacts) {
            if (current.contains(contact.getId())) {
                selected.add(contact);
            }
        }
        return selected;
    }
}
