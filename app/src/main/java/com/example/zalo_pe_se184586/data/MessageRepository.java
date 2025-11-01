package com.example.zalo_pe_se184586.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zalo_pe_se184586.model.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MessageRepository {
    private static volatile MessageRepository instance;
    private final Map<String, MutableLiveData<List<Message>>> messagesByGroup = new HashMap<>();

    private MessageRepository() {
    }

    public static MessageRepository getInstance() {
        if (instance == null) {
            synchronized (MessageRepository.class) {
                if (instance == null) {
                    instance = new MessageRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<Message>> getMessagesLiveData(String groupId) {
        MutableLiveData<List<Message>> liveData = messagesByGroup.get(groupId);
        if (liveData == null) {
            liveData = new MutableLiveData<>(new ArrayList<>());
            messagesByGroup.put(groupId, liveData);
        }
        return liveData;
    }

    public void sendMessage(String groupId, String senderId, String content) {
        if (groupId == null || senderId == null || content == null) {
            return;
        }
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        MutableLiveData<List<Message>> liveData = (MutableLiveData<List<Message>>) getMessagesLiveData(groupId);
        List<Message> current = liveData.getValue();
        List<Message> updated = new ArrayList<>(current == null ? Collections.emptyList() : current);
        Message message = new Message(
                UUID.randomUUID().toString(),
                groupId,
                senderId,
                trimmed,
                System.currentTimeMillis(),
                Message.Status.SENT);
        updated.add(message);
        liveData.setValue(updated);
    }

    public void seedMessages(String groupId, List<Message> seedMessages) {
        MutableLiveData<List<Message>> liveData = (MutableLiveData<List<Message>>) getMessagesLiveData(groupId);
        List<Message> copy = new ArrayList<>(seedMessages == null ? Collections.emptyList() : seedMessages);
        liveData.setValue(copy);
    }
}
