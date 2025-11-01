package com.example.zalo_pe_se184586.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.zalo_pe_se184586.data.ContactRepository;
import com.example.zalo_pe_se184586.model.Contact;

import java.util.List;

public class ContactsViewModel extends ViewModel {
    private final MutableLiveData<List<Contact>> contactsLiveData = new MutableLiveData<>();
    private final ContactRepository contactRepository = ContactRepository.getInstance();

    public ContactsViewModel() {
        loadContacts();
    }

    public LiveData<List<Contact>> getContacts() {
        return contactsLiveData;
    }

    public void refresh() {
        loadContacts();
    }

    private void loadContacts() {
        contactsLiveData.setValue(contactRepository.getAllContacts());
    }
}
