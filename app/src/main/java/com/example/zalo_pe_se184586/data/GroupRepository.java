package com.example.zalo_pe_se184586.data;

import android.content.Context;

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
    private AppDatabase database;
    private Context context;

    private GroupRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(this.context);
        loadGroupsFromDatabase();
    }

    public static GroupRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (GroupRepository.class) {
                if (instance == null) {
                    instance = new GroupRepository(context);
                }
            }
        }
        return instance;
    }

    private void loadGroupsFromDatabase() {
        List<Group> dbGroups = database.getAllGroups();
        for (Group group : dbGroups) {
            MutableLiveData<Group> liveData = new MutableLiveData<>(group);
            groups.put(group.getId(), liveData);
        }
    }

    public Group createGroup(String name, List<Contact> members) {
        Group group = database.createGroup(name, members);
        MutableLiveData<Group> liveData = new MutableLiveData<>(group);
        groups.put(group.getId(), liveData);
        return group;
    }

    public void addMessage(String groupId, Message message) {
        // Save to database
        database.addMessage(groupId, message);
        
        // Update LiveData
        MutableLiveData<Group> liveData = groups.get(groupId);
        if (liveData != null) {
            Group group = liveData.getValue();
            if (group != null) {
                group.addMessage(message);
                liveData.setValue(group);
            }
        } else {
            // Load from database if not in memory
            Group group = database.getGroup(groupId);
            if (group != null) {
                liveData = new MutableLiveData<>(group);
                groups.put(groupId, liveData);
            }
        }
    }

    public LiveData<Group> getGroupLiveData(String groupId) {
        MutableLiveData<Group> liveData = groups.get(groupId);
        if (liveData == null) {
            // Try to load from database
            Group group = database.getGroup(groupId);
            if (group != null) {
                liveData = new MutableLiveData<>(group);
                groups.put(groupId, liveData);
                return liveData;
            }
            return new MutableLiveData<>();
        }
        return liveData;
    }

    public List<Group> getAllGroups() {
        List<Group> groupList = new ArrayList<>();
        for (MutableLiveData<Group> liveData : groups.values()) {
            Group group = liveData.getValue();
            if (group != null) {
                groupList.add(group);
            }
        }
        // If no groups in memory, load from database
        if (groupList.isEmpty()) {
            groupList = database.getAllGroups();
            for (Group group : groupList) {
                MutableLiveData<Group> liveData = new MutableLiveData<>(group);
                groups.put(group.getId(), liveData);
            }
        }
        return groupList;
    }
}