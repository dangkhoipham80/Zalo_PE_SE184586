package com.example.zalo_pe_se184586.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group implements Parcelable {
    private final String id;
    private final String name;
    private final ArrayList<Contact> members;
    private final ArrayList<Message> messages;

    public Group(String id, String name, List<Contact> members, List<Message> messages) {
        this.id = id;
        this.name = name;
        this.members = new ArrayList<>(members);
        this.messages = new ArrayList<>(messages);
    }

    protected Group(Parcel in) {
        id = in.readString();
        name = in.readString();
        members = in.createTypedArrayList(Contact.CREATOR);
        messages = in.createTypedArrayList(Message.CREATOR);
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Contact> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeTypedList(members);
        dest.writeTypedList(messages);
    }
}
