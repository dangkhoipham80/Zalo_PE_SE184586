package com.example.zalo_pe_se184586.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zalo_pe_se184586.model.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupRepository {
    private static volatile GroupRepository instance;
    private final Map<String, MutableLiveData<Group>> groups = new HashMap<>();
    private final Map<String, Group> groupById = new HashMap<>();

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

    public Group createGroup(String name, List<String> memberIds) {
        String id = UUID.randomUUID().toString();
        Group group = new Group(id, name, memberIds, System.currentTimeMillis());
        MutableLiveData<Group> liveData = new MutableLiveData<>(group);
        groups.put(id, liveData);
        groupById.put(id, group);
        return group;
    }

    public LiveData<Group> getGroupLiveData(String groupId) {
        MutableLiveData<Group> liveData = groups.get(groupId);
        if (liveData == null) {
            Group group = groupById.get(groupId);
            liveData = group == null ? new MutableLiveData<>() : new MutableLiveData<>(group);
            groups.put(groupId, liveData);
        }
        return liveData;
    }

    public Group getGroup(String groupId) {
        return groupById.get(groupId);
    }

    public List<Group> getAllGroups() {
        return Collections.unmodifiableList(new ArrayList<>(groupById.values()));
    }
}
