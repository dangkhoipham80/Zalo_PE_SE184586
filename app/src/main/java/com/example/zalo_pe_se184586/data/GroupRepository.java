package com.example.zalo_pe_se184586.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupRepository {
    private static volatile GroupRepository instance;
    private final Map<String, MutableLiveData<Group>> groups = new HashMap<>();

    private GroupRepository() {
    }

    public static GroupRepository getInstance() {
        if (instance == null) {
            synchronized (GroupRepository.class) {
                if (instance == null) {
                    instance = new GroupRepository();
                }
            }
        }
        return instance;
    }

    public Group createGroup(String name, List<Contact> members) {
        String id = UUID.randomUUID().toString();
        Group group = new Group(id, name, members, new ArrayList<>());
        MutableLiveData<Group> liveData = new MutableLiveData<>(group);
        groups.put(id, liveData);
        return group;
    }

    public void addMessage(String groupId, Message message) {
        MutableLiveData<Group> liveData = groups.get(groupId);
        if (liveData == null) {
            return;
        }
        Group group = liveData.getValue();
        if (group == null) {
            return;
        }
        group.addMessage(message);
        liveData.setValue(group);
    }

    public LiveData<Group> getGroupLiveData(String groupId) {
        MutableLiveData<Group> liveData = groups.get(groupId);
        if (liveData == null) {
            return new MutableLiveData<>();
        }
        return liveData;
    }
}
