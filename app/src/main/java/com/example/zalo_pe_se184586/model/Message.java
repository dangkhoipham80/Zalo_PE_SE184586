package com.example.zalo_pe_se184586.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Message implements Parcelable {
    private final String id;
    private final String groupId;
    private final String senderName;
    private final String content;
    private final long timestamp;

    public Message(String id, String groupId, String senderName, String content, long timestamp) {
        this.id = id;
        this.groupId = groupId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
    }

    protected Message(Parcel in) {
        id = in.readString();
        groupId = in.readString();
        senderName = in.readString();
        content = in.readString();
        timestamp = in.readLong();
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

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(groupId);
        dest.writeString(senderName);
        dest.writeString(content);
        dest.writeLong(timestamp);
    }
}
