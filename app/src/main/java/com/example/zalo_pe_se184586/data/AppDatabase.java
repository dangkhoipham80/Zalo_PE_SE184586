package com.example.zalo_pe_se184586.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "zalo_app.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_CONTACTS = "contacts";
    private static final String TABLE_FRIENDS = "friends";
    private static final String TABLE_GROUPS = "groups";
    private static final String TABLE_GROUP_MEMBERS = "group_members";
    private static final String TABLE_MESSAGES = "messages";

    // Contacts columns
    private static final String COL_CONTACT_ID = "id";
    private static final String COL_CONTACT_NAME = "name";
    private static final String COL_CONTACT_AVATAR_URL = "avatar_url";

    // Friends columns (foreign key to contacts)
    private static final String COL_FRIEND_CONTACT_ID = "contact_id";

    // Groups columns
    private static final String COL_GROUP_ID = "id";
    private static final String COL_GROUP_NAME = "name";

    // Group members columns
    private static final String COL_GM_GROUP_ID = "group_id";
    private static final String COL_GM_CONTACT_ID = "contact_id";

    // Messages columns
    private static final String COL_MSG_ID = "id";
    private static final String COL_MSG_GROUP_ID = "group_id";
    private static final String COL_MSG_SENDER_NAME = "sender_name";
    private static final String COL_MSG_CONTENT = "content";
    private static final String COL_MSG_TIMESTAMP = "timestamp";

    private static AppDatabase instance;
    private final Context context;

    private AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabase(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create contacts table
        String createContactsTable = "CREATE TABLE " + TABLE_CONTACTS + " (" +
                COL_CONTACT_ID + " TEXT PRIMARY KEY, " +
                COL_CONTACT_NAME + " TEXT NOT NULL, " +
                COL_CONTACT_AVATAR_URL + " TEXT)";
        db.execSQL(createContactsTable);

        // Create friends table
        String createFriendsTable = "CREATE TABLE " + TABLE_FRIENDS + " (" +
                COL_FRIEND_CONTACT_ID + " TEXT PRIMARY KEY, " +
                "FOREIGN KEY(" + COL_FRIEND_CONTACT_ID + ") REFERENCES " + TABLE_CONTACTS + "(" + COL_CONTACT_ID + "))";
        db.execSQL(createFriendsTable);

        // Create groups table
        String createGroupsTable = "CREATE TABLE " + TABLE_GROUPS + " (" +
                COL_GROUP_ID + " TEXT PRIMARY KEY, " +
                COL_GROUP_NAME + " TEXT NOT NULL)";
        db.execSQL(createGroupsTable);

        // Create group_members table
        String createGroupMembersTable = "CREATE TABLE " + TABLE_GROUP_MEMBERS + " (" +
                COL_GM_GROUP_ID + " TEXT NOT NULL, " +
                COL_GM_CONTACT_ID + " TEXT NOT NULL, " +
                "PRIMARY KEY(" + COL_GM_GROUP_ID + ", " + COL_GM_CONTACT_ID + "), " +
                "FOREIGN KEY(" + COL_GM_GROUP_ID + ") REFERENCES " + TABLE_GROUPS + "(" + COL_GROUP_ID + "), " +
                "FOREIGN KEY(" + COL_GM_CONTACT_ID + ") REFERENCES " + TABLE_CONTACTS + "(" + COL_CONTACT_ID + "))";
        db.execSQL(createGroupMembersTable);

        // Create messages table
        String createMessagesTable = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COL_MSG_ID + " TEXT PRIMARY KEY, " +
                COL_MSG_GROUP_ID + " TEXT NOT NULL, " +
                COL_MSG_SENDER_NAME + " TEXT NOT NULL, " +
                COL_MSG_CONTENT + " TEXT NOT NULL, " +
                COL_MSG_TIMESTAMP + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_MSG_GROUP_ID + ") REFERENCES " + TABLE_GROUPS + "(" + COL_GROUP_ID + "))";
        db.execSQL(createMessagesTable);

        // Insert sample data
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Insert sample contacts
        String[] contactNames = {
            "Alice Nguyen", "Bao Tran", "Chi Le", "Duy Pham", "Emily Vo",
            "Gia Bui", "Hien Ho", "Khoa Dao", "Linh Phan", "Minh Dang",
            "Nam Vo", "Oanh Tran", "Phong Le", "Quynh Pham", "Son Ho"
        };

        for (int i = 0; i < contactNames.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COL_CONTACT_ID, String.valueOf(i + 1));
            cv.put(COL_CONTACT_NAME, contactNames[i]);
            cv.put(COL_CONTACT_AVATAR_URL, "");
            db.insert(TABLE_CONTACTS, null, cv);
        }

        // Insert sample friends (first 8 contacts are friends)
        for (int i = 1; i <= 8; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COL_FRIEND_CONTACT_ID, String.valueOf(i));
            db.insert(TABLE_FRIENDS, null, cv);
        }

        // Insert sample groups
        ContentValues group1 = new ContentValues();
        group1.put(COL_GROUP_ID, "group_1");
        group1.put(COL_GROUP_NAME, "FPT Friends");
        db.insert(TABLE_GROUPS, null, group1);

        ContentValues group2 = new ContentValues();
        group2.put(COL_GROUP_ID, "group_2");
        group2.put(COL_GROUP_NAME, "Study Group");
        db.insert(TABLE_GROUPS, null, group2);

        ContentValues group3 = new ContentValues();
        group3.put(COL_GROUP_ID, "group_3");
        group3.put(COL_GROUP_NAME, "Coffee Lovers");
        db.insert(TABLE_GROUPS, null, group3);

        // Insert group members
        // Group 1: FPT Friends - members: 1, 2, 3, 4, 5
        for (int i = 1; i <= 5; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COL_GM_GROUP_ID, "group_1");
            cv.put(COL_GM_CONTACT_ID, String.valueOf(i));
            db.insert(TABLE_GROUP_MEMBERS, null, cv);
        }

        // Group 2: Study Group - members: 3, 4, 6, 7
        int[] group2Members = {3, 4, 6, 7};
        for (int memberId : group2Members) {
            ContentValues cv = new ContentValues();
            cv.put(COL_GM_GROUP_ID, "group_2");
            cv.put(COL_GM_CONTACT_ID, String.valueOf(memberId));
            db.insert(TABLE_GROUP_MEMBERS, null, cv);
        }

        // Group 3: Coffee Lovers - members: 1, 8, 9, 10
        int[] group3Members = {1, 8, 9, 10};
        for (int memberId : group3Members) {
            ContentValues cv = new ContentValues();
            cv.put(COL_GM_GROUP_ID, "group_3");
            cv.put(COL_GM_CONTACT_ID, String.valueOf(memberId));
            db.insert(TABLE_GROUP_MEMBERS, null, cv);
        }

        // Insert sample messages
        long now = System.currentTimeMillis();
        
        // Messages for group_1
        insertMessage(db, "msg_1", "group_1", "Alice Nguyen", "Hey everyone! How are you?", now - 3600000);
        insertMessage(db, "msg_2", "group_1", "Bao Tran", "I'm good! Thanks!", now - 3300000);
        insertMessage(db, "msg_3", "group_1", "Chi Le", "Let's meet up soon!", now - 3000000);
        
        // Messages for group_2
        insertMessage(db, "msg_4", "group_2", "Chi Le", "Đi cf không?", now - 7200000);
        insertMessage(db, "msg_5", "group_2", "Duy Pham", "Ok nhé!", now - 6900000);
        insertMessage(db, "msg_6", "group_2", "Gia Bui", "Count me in!", now - 6600000);
        
        // Messages for group_3
        insertMessage(db, "msg_7", "group_3", "Alice Nguyen", "Best coffee place?", now - 86400000);
        insertMessage(db, "msg_8", "group_3", "Khoa Dao", "The Coffee House!", now - 83100000);
    }

    private void insertMessage(SQLiteDatabase db, String id, String groupId, String sender, String content, long timestamp) {
        ContentValues cv = new ContentValues();
        cv.put(COL_MSG_ID, id);
        cv.put(COL_MSG_GROUP_ID, groupId);
        cv.put(COL_MSG_SENDER_NAME, sender);
        cv.put(COL_MSG_CONTENT, content);
        cv.put(COL_MSG_TIMESTAMP, timestamp);
        db.insert(TABLE_MESSAGES, null, cv);
    }

    // Contact operations
    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, null, null, null, null, null, COL_CONTACT_NAME + " ASC");
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_NAME));
                String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_AVATAR_URL));
                contacts.add(new Contact(id, name, avatarUrl));
            }
            cursor.close();
        }
        return contacts;
    }

    public List<Contact> getFriends() {
        List<Contact> friends = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT c.* FROM " + TABLE_CONTACTS + " c " +
                "INNER JOIN " + TABLE_FRIENDS + " f ON c." + COL_CONTACT_ID + " = f." + COL_FRIEND_CONTACT_ID +
                " ORDER BY c." + COL_CONTACT_NAME + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_NAME));
                String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_AVATAR_URL));
                friends.add(new Contact(id, name, avatarUrl));
            }
            cursor.close();
        }
        return friends;
    }

    public void addContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CONTACT_ID, contact.getId());
        cv.put(COL_CONTACT_NAME, contact.getName());
        cv.put(COL_CONTACT_AVATAR_URL, contact.getAvatarUrl());
        db.insertWithOnConflict(TABLE_CONTACTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void addFriend(String contactId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FRIEND_CONTACT_ID, contactId);
        db.insertWithOnConflict(TABLE_FRIENDS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Group operations
    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROUPS, null, null, null, null, null, COL_GROUP_NAME + " ASC");
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String groupId = cursor.getString(cursor.getColumnIndexOrThrow(COL_GROUP_ID));
                String groupName = cursor.getString(cursor.getColumnIndexOrThrow(COL_GROUP_NAME));
                
                // Get members
                List<Contact> members = getGroupMembers(db, groupId);
                
                // Get messages
                List<Message> messages = getGroupMessages(db, groupId);
                
                groups.add(new Group(groupId, groupName, members, messages));
            }
            cursor.close();
        }
        return groups;
    }

    private List<Contact> getGroupMembers(SQLiteDatabase db, String groupId) {
        List<Contact> members = new ArrayList<>();
        String query = "SELECT c.* FROM " + TABLE_CONTACTS + " c " +
                "INNER JOIN " + TABLE_GROUP_MEMBERS + " gm ON c." + COL_CONTACT_ID + " = gm." + COL_GM_CONTACT_ID +
                " WHERE gm." + COL_GM_GROUP_ID + " = ?" +
                " ORDER BY c." + COL_CONTACT_NAME + " ASC";
        Cursor cursor = db.rawQuery(query, new String[]{groupId});
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_NAME));
                String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_AVATAR_URL));
                members.add(new Contact(id, name, avatarUrl));
            }
            cursor.close();
        }
        return members;
    }

    private List<Message> getGroupMessages(SQLiteDatabase db, String groupId) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_MESSAGES +
                " WHERE " + COL_MSG_GROUP_ID + " = ?" +
                " ORDER BY " + COL_MSG_TIMESTAMP + " ASC";
        Cursor cursor = db.rawQuery(query, new String[]{groupId});
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_ID));
                String sender = cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_SENDER_NAME));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_CONTENT));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_MSG_TIMESTAMP));
                messages.add(new Message(id, groupId, sender, content, timestamp));
            }
            cursor.close();
        }
        return messages;
    }

    public Group createGroup(String name, List<Contact> members) {
        SQLiteDatabase db = getWritableDatabase();
        String groupId = UUID.randomUUID().toString();
        
        // Insert group
        ContentValues cv = new ContentValues();
        cv.put(COL_GROUP_ID, groupId);
        cv.put(COL_GROUP_NAME, name);
        db.insert(TABLE_GROUPS, null, cv);
        
        // Insert members
        for (Contact member : members) {
            ContentValues memberCv = new ContentValues();
            memberCv.put(COL_GM_GROUP_ID, groupId);
            memberCv.put(COL_GM_CONTACT_ID, member.getId());
            db.insert(TABLE_GROUP_MEMBERS, null, memberCv);
        }
        
        return new Group(groupId, name, members, new ArrayList<>());
    }

    public void addMessage(String groupId, Message message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MSG_ID, message.getId());
        cv.put(COL_MSG_GROUP_ID, groupId);
        cv.put(COL_MSG_SENDER_NAME, message.getSenderName());
        cv.put(COL_MSG_CONTENT, message.getContent());
        cv.put(COL_MSG_TIMESTAMP, message.getTimestamp());
        db.insert(TABLE_MESSAGES, null, cv);
    }

    public Group getGroup(String groupId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROUPS, null, COL_GROUP_ID + " = ?", 
                new String[]{groupId}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            String groupName = cursor.getString(cursor.getColumnIndexOrThrow(COL_GROUP_NAME));
            List<Contact> members = getGroupMembers(db, groupId);
            List<Message> messages = getGroupMessages(db, groupId);
            cursor.close();
            return new Group(groupId, groupName, members, messages);
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
}
