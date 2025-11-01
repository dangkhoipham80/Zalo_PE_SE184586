package com.example.zalo_pe_se184586.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Message implements Parcelable {
    public enum Status {
        SENT,
        SEEN
    }

    private final String id;
    private final String groupId;
    private final String senderId;
    private final String content;
    private final long timestamp;
    private final Status status;

    public Message(String id, String groupId, String senderId, String content, long timestamp, Status status) {
        this.id = id;
        this.groupId = groupId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
    }

    protected Message(Parcel in) {
        id = in.readString();
        groupId = in.readString();
        senderId = in.readString();
        content = in.readString();
        timestamp = in.readLong();
        status = Status.valueOf(in.readString());
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(groupId);
        dest.writeString(senderId);
        dest.writeString(content);
        dest.writeLong(timestamp);
        dest.writeString(status.name());
    }
}
