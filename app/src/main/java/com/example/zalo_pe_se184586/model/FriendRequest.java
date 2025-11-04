package com.example.zalo_pe_se184586.model;

public class FriendRequest {
    private final String id;
    private final String fromPhoneNumber;
    private final String fromName;
    private final String fromContactId; // null nếu người gửi chưa có trong contacts
    private final long timestamp;
    private final String status; // "pending", "accepted", "rejected"

    public FriendRequest(String id, String fromPhoneNumber, String fromName, String fromContactId, long timestamp, String status) {
        this.id = id;
        this.fromPhoneNumber = fromPhoneNumber;
        this.fromName = fromName;
        this.fromContactId = fromContactId;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getFromPhoneNumber() {
        return fromPhoneNumber;
    }

    public String getFromName() {
        return fromName;
    }

    public String getFromContactId() {
        return fromContactId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }
}
