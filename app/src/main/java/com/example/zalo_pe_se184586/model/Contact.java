package com.example.zalo_pe_se184586.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class Contact implements Parcelable {
    private final String id;
    private final String name;
    @DrawableRes
    private final int avatarRes;
    private final String phone;

    public Contact(String id, String name, @DrawableRes int avatarRes, String phone) {
        this.id = id;
        this.name = name;
        this.avatarRes = avatarRes;
        this.phone = phone;
    }

    protected Contact(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatarRes = in.readInt();
        phone = in.readString();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAvatarRes() {
        return avatarRes;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(avatarRes);
        dest.writeString(phone);
    }
}
