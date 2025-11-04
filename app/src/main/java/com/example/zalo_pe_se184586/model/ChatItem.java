package com.example.zalo_pe_se184586.model;

public class ChatItem {
    private String name;
    private String lastMessage;
    private String time;
    private int avatarResId;

    public ChatItem(String name, String lastMessage, String time, int avatarResId) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        this.avatarResId = avatarResId;
    }

    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public int getAvatarResId() { return avatarResId; }
}
