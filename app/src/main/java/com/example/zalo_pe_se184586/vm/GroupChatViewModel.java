package com.example.zalo_pe_se184586.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.zalo_pe_se184586.data.ContactRepository;
import com.example.zalo_pe_se184586.data.GroupRepository;
import com.example.zalo_pe_se184586.data.MessageRepository;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.model.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupChatViewModel extends ViewModel {
    static final String KEY_GROUP_ID = "group_id";
    private static final String KEY_DRAFT = "draft";

    private final SavedStateHandle savedStateHandle;
    private final GroupRepository groupRepository = GroupRepository.getInstance();
    private final MessageRepository messageRepository = MessageRepository.getInstance();
    private final ContactRepository contactRepository = ContactRepository.getInstance();
    private final MediatorLiveData<Group> groupLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Message>> messagesLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Contact>> memberContactsLiveData = new MediatorLiveData<>();

    private LiveData<Group> groupSource;
    private LiveData<List<Message>> messageSource;

    public GroupChatViewModel(@NonNull SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;
        String groupId = savedStateHandle.get(KEY_GROUP_ID);
        if (groupId != null) {
            attachGroup(groupId);
        }
    }

    public LiveData<Group> getGroup() {
        return groupLiveData;
    }

    public LiveData<List<Message>> getMessages() {
        return messagesLiveData;
    }

    public LiveData<List<Contact>> getMemberContacts() {
        return memberContactsLiveData;
    }

    public boolean hasGroup() {
        return savedStateHandle.get(KEY_GROUP_ID) != null;
    }

    public void setInitialGroupId(String groupId) {
        if (groupId == null || hasGroup()) {
            return;
        }
        savedStateHandle.set(KEY_GROUP_ID, groupId);
        attachGroup(groupId);
    }

    private void attachGroup(String groupId) {
        if (groupSource != null) {
            groupLiveData.removeSource(groupSource);
        }
        groupSource = groupRepository.getGroupLiveData(groupId);
        groupLiveData.addSource(groupSource, group -> {
            groupLiveData.setValue(group);
            updateMembers(group);
        });

        if (messageSource != null) {
            messagesLiveData.removeSource(messageSource);
        }
        messageSource = messageRepository.getMessagesLiveData(groupId);
        messagesLiveData.addSource(messageSource, messagesLiveData::setValue);
    }

    private void updateMembers(Group group) {
        if (group == null) {
            memberContactsLiveData.setValue(Collections.emptyList());
            return;
        }
        List<Contact> contacts = new ArrayList<>();
        for (String memberId : group.getMemberIds()) {
            Contact contact = contactRepository.getContactById(memberId);
            if (contact != null) {
                contacts.add(contact);
            }
        }
        memberContactsLiveData.setValue(contacts);
    }

    public void sendMessage(String content) {
        String groupId = savedStateHandle.get(KEY_GROUP_ID);
        if (groupId == null) {
            return;
        }
        messageRepository.sendMessage(groupId, getCurrentUserId(), content);
    }

    public void updateDraft(String draft) {
        savedStateHandle.set(KEY_DRAFT, draft == null ? "" : draft);
    }

    public String getDraft() {
        String draft = savedStateHandle.get(KEY_DRAFT);
        return draft == null ? "" : draft;
    }

    public String resolveSenderName(String senderId) {
        if (senderId == null) {
            return "";
        }
        if (senderId.equals(getCurrentUserId())) {
            return "You";
        }
        Contact contact = contactRepository.getContactById(senderId);
        return contact != null ? contact.getName() : "Unknown";
    }

    public String getCurrentUserId() {
        return contactRepository.getCurrentUser().getId();
    }
}
