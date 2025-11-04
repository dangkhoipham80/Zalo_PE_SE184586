package com.example.zalo_pe_se184586.model;

public class ContactItem {
    private String name;
    private String phone;
    private int avatarResId;

    public ContactItem(String name, String phone, int avatarResId) {
        this.name = name;
        this.phone = phone;
        this.avatarResId = avatarResId;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public int getAvatarResId() { return avatarResId; }
}
