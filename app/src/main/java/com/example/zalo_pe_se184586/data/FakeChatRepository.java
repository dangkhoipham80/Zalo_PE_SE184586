package com.example.zalo_pe_se184586.data;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.model.ChatItem;
import com.example.zalo_pe_se184586.model.Message;

import java.util.ArrayList;
import java.util.List;

public class FakeChatRepository {

    public static List<ChatItem> getSampleChats() {
        List<ChatItem> chats = new ArrayList<>();

        chats.add(new ChatItem("Alice Nguyen", "Hey! How are you?", "10:24 AM", R.drawable.ic_launcher_foreground));
        chats.add(new ChatItem("Bao Tran", "Let's go out later!", "09:58 AM", R.drawable.ic_launcher_foreground));
        chats.add(new ChatItem("Chi Le", "Did you finish the project?", "Yesterday", R.drawable.ic_launcher_foreground));
        chats.add(new ChatItem("Duy Pham", "👍", "Yesterday", R.drawable.ic_launcher_foreground));
        chats.add(new ChatItem("Group: FPT Friends", "Khoa: see you soon!", "2 days ago", R.drawable.ic_launcher_foreground));
        chats.add(new ChatItem("Emily Vo", "❤️", "3 days ago", R.drawable.ic_launcher_foreground));

        return chats;
    }

    public static List<Message> getSampleMessages() {
        List<Message> messages = new ArrayList<>();

        messages.add(new Message("1", "Alice", "Hello, how are you?", "10:20 AM", System.currentTimeMillis()));
        messages.add(new Message("2", "Bao", "I'm good! You?", "10:21 AM", System.currentTimeMillis()));
        messages.add(new Message("1", "Alice", "Doing great, thanks!", "10:22 AM", System.currentTimeMillis()));
        messages.add(new Message("2", "Bao", "Wanna grab coffee later?", "10:23 AM", System.currentTimeMillis()));

        return messages;
    }
}
