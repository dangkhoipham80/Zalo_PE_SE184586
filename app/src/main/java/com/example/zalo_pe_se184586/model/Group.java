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
    private final ArrayList<String> memberIds;
    private final long createdAt;

    public Group(String id, String name, List<String> memberIds, long createdAt) {
        this.id = id;
        this.name = name;
        this.memberIds = new ArrayList<>(memberIds);
        this.createdAt = createdAt;
    }

    protected Group(Parcel in) {
        id = in.readString();
        name = in.readString();
        memberIds = in.createStringArrayList();
        createdAt = in.readLong();
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

    public List<String> getMemberIds() {
        return Collections.unmodifiableList(memberIds);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeStringList(memberIds);
        dest.writeLong(createdAt);
    }
}
