package com.example.zalo_pe_se184586.ui.chat;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.zalo_pe_se184586.data.GroupRepository;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.model.Message;

import java.util.UUID;

public class GroupChatViewModel extends ViewModel {

    static final String KEY_GROUP_ID = "group_id";

    private final SavedStateHandle savedStateHandle;
    private final GroupRepository repository = GroupRepository.getInstance();
    private final MediatorLiveData<Group> groupLiveData = new MediatorLiveData<>();
    private LiveData<Group> sourceLiveData;

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

    public boolean hasGroup() {
        return savedStateHandle.get(KEY_GROUP_ID) != null;
    }

    public void setInitialGroup(Group group) {
        if (group == null) {
            return;
        }
        Group current = groupLiveData.getValue();
        if (current == null) {
            savedStateHandle.set(KEY_GROUP_ID, group.getId());
            attachGroup(group.getId());
            groupLiveData.setValue(group);
        }
    }

    private void attachGroup(String groupId) {
        if (sourceLiveData != null) {
            groupLiveData.removeSource(sourceLiveData);
        }
        sourceLiveData = repository.getGroupLiveData(groupId);
        groupLiveData.addSource(sourceLiveData, groupLiveData::setValue);
    }

    public void sendMessage(String content) {
        String groupId = savedStateHandle.get(KEY_GROUP_ID);
        if (groupId == null || content == null) {
            return;
        }
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        Message message = new Message(UUID.randomUUID().toString(), groupId, "You", trimmed, System.currentTimeMillis());
        repository.addMessage(groupId, message);
    }
}
