package com.example.zalo_pe_se184586.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.FriendRequest;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "zalo_app.db";
    private static final int DATABASE_VERSION = 2;

    // Table names
    private static final String TABLE_CONTACTS = "contacts";
    private static final String TABLE_FRIENDS = "friends";
    private static final String TABLE_GROUPS = "groups";
    private static final String TABLE_GROUP_MEMBERS = "group_members";
    private static final String TABLE_MESSAGES = "messages";
    private static final String TABLE_FRIEND_REQUESTS = "friend_requests";

    // Contacts columns
    private static final String COL_CONTACT_ID = "id";
    private static final String COL_CONTACT_NAME = "name";
    private static final String COL_CONTACT_PHONE = "phone_number";
    private static final String COL_CONTACT_AVATAR_URL = "avatar_url";

    // Friend requests columns
    private static final String COL_FR_ID = "id";
    private static final String COL_FR_FROM_PHONE = "from_phone_number";
    private static final String COL_FR_FROM_NAME = "from_name";
    private static final String COL_FR_FROM_CONTACT_ID = "from_contact_id";
    private static final String COL_FR_TIMESTAMP = "timestamp";
    private static final String COL_FR_STATUS = "status";

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

    // Current user ID - giả sử current user là contact đầu tiên (ID = "1" = Alice Nguyen)
    // Trong thực tế, có thể lưu trong SharedPreferences hoặc từ authentication
    private static final String CURRENT_USER_ID = "1"; // Alice Nguyen

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
                COL_CONTACT_PHONE + " TEXT, " +
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

        // Create friend_requests table
        String createFriendRequestsTable = "CREATE TABLE " + TABLE_FRIEND_REQUESTS + " (" +
                COL_FR_ID + " TEXT PRIMARY KEY, " +
                COL_FR_FROM_PHONE + " TEXT NOT NULL, " +
                COL_FR_FROM_NAME + " TEXT NOT NULL, " +
                COL_FR_FROM_CONTACT_ID + " TEXT, " +
                COL_FR_TIMESTAMP + " INTEGER NOT NULL, " +
                COL_FR_STATUS + " TEXT NOT NULL DEFAULT 'pending')";
        db.execSQL(createFriendRequestsTable);

        // Insert sample data
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add phone_number column to contacts table
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COL_CONTACT_PHONE + " TEXT");
            
            // Create friend_requests table
            String createFriendRequestsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_FRIEND_REQUESTS + " (" +
                    COL_FR_ID + " TEXT PRIMARY KEY, " +
                    COL_FR_FROM_PHONE + " TEXT NOT NULL, " +
                    COL_FR_FROM_NAME + " TEXT NOT NULL, " +
                    COL_FR_FROM_CONTACT_ID + " TEXT, " +
                    COL_FR_TIMESTAMP + " INTEGER NOT NULL, " +
                    COL_FR_STATUS + " TEXT NOT NULL DEFAULT 'pending')";
            db.execSQL(createFriendRequestsTable);
        }
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Insert sample contacts
        String[] contactNames = {
            "Alice Nguyen", "Bao Tran", "Chi Le", "Duy Pham", "Emily Vo",
            "Gia Bui", "Hien Ho", "Khoa Dao", "Linh Phan", "Minh Dang",
            "Nam Vo", "Oanh Tran", "Phong Le", "Quynh Pham", "Son Ho"
        };

        String[] phoneNumbers = {
            "0912345678", "0912345679", "0912345680", "0912345681", "0912345682",
            "0912345683", "0912345684", "0912345685", "0912345686", "0912345687",
            "0912345688", "0912345689", "0912345690", "0912345691", "0912345692"
        };

        for (int i = 0; i < contactNames.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COL_CONTACT_ID, String.valueOf(i + 1));
            cv.put(COL_CONTACT_NAME, contactNames[i]);
            cv.put(COL_CONTACT_PHONE, phoneNumbers[i]);
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
                String phone = null;
                try {
                    int phoneIndex = cursor.getColumnIndex(COL_CONTACT_PHONE);
                    if (phoneIndex >= 0) {
                        phone = cursor.getString(phoneIndex);
                    }
                } catch (Exception e) {
                    // Column might not exist in old database
                }
                String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_AVATAR_URL));
                contacts.add(new Contact(id, name, phone, avatarUrl));
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
                String phone = null;
                try {
                    int phoneIndex = cursor.getColumnIndex(COL_CONTACT_PHONE);
                    if (phoneIndex >= 0) {
                        phone = cursor.getString(phoneIndex);
                    }
                } catch (Exception e) {
                    // Column might not exist in old database
                }
                String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_AVATAR_URL));
                friends.add(new Contact(id, name, phone, avatarUrl));
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
        if (contact.getPhoneNumber() != null) {
            cv.put(COL_CONTACT_PHONE, contact.getPhoneNumber());
        }
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
                String phone = null;
                try {
                    int phoneIndex = cursor.getColumnIndex(COL_CONTACT_PHONE);
                    if (phoneIndex >= 0) {
                        phone = cursor.getString(phoneIndex);
                    }
                } catch (Exception e) {
                    // Column might not exist in old database
                }
                String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_AVATAR_URL));
                members.add(new Contact(id, name, phone, avatarUrl));
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

    /**
     * Find or create a one-to-one group with a contact.
     * Group sẽ có 2 members: current user và contact đích
     * Nhưng khi hiển thị, chỉ hiển thị contact đích (giống Zalo)
     */
    public Group findOrCreateOneToOneGroup(Contact contact) {
        SQLiteDatabase db = getReadableDatabase();
        String currentUserId = getCurrentUserId();
        
        // Không tạo group với chính mình
        if (contact.getId().equals(currentUserId)) {
            return null;
        }
        
        // Get all groups and check if there's already a 1-1 group with this contact
        // (a group with exactly 2 members: current user + contact)
        List<Group> allGroups = getAllGroups();
        for (Group group : allGroups) {
            List<Contact> members = group.getMembers();
            if (members.size() == 2) {
                boolean hasCurrentUser = false;
                boolean hasContact = false;
                for (Contact member : members) {
                    if (member.getId().equals(currentUserId)) {
                        hasCurrentUser = true;
                    }
                    if (member.getId().equals(contact.getId())) {
                        hasContact = true;
                    }
                }
                // Nếu group có cả current user và contact này, đây là group 1-1 cần tìm
                if (hasCurrentUser && hasContact) {
                    return group;
                }
            }
        }
        
        // Not found, create new 1-1 group
        Contact currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        // Tạo group với 2 members: current user và contact đích
        List<Contact> groupMembers = new ArrayList<>();
        groupMembers.add(currentUser);
        groupMembers.add(contact);
        
        String groupName = contact.getName();
        return createGroup(groupName, groupMembers);
    }



    /**
     * Add a member to an existing group
     */
    public void addMemberToGroup(String groupId, Contact contact) {
        SQLiteDatabase db = getWritableDatabase();
        
        // Check if member already exists in group
        String checkQuery = "SELECT COUNT(*) FROM " + TABLE_GROUP_MEMBERS +
                " WHERE " + COL_GM_GROUP_ID + " = ? AND " + COL_GM_CONTACT_ID + " = ?";
        Cursor cursor = db.rawQuery(checkQuery, new String[]{groupId, contact.getId()});
        
        boolean exists = false;
        if (cursor != null && cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
            cursor.close();
        }
        
        if (!exists) {
            ContentValues cv = new ContentValues();
            cv.put(COL_GM_GROUP_ID, groupId);
            cv.put(COL_GM_CONTACT_ID, contact.getId());
            db.insert(TABLE_GROUP_MEMBERS, null, cv);
        }
    }

    /**
     * Delete a group and all related data (messages, members)
     */
    public void deleteGroup(String groupId) {
        SQLiteDatabase db = getWritableDatabase();
        
        // Delete messages
        db.delete(TABLE_MESSAGES, COL_MSG_GROUP_ID + " = ?", new String[]{groupId});
        
        // Delete group members
        db.delete(TABLE_GROUP_MEMBERS, COL_GM_GROUP_ID + " = ?", new String[]{groupId});
        
        // Delete group
        db.delete(TABLE_GROUPS, COL_GROUP_ID + " = ?", new String[]{groupId});
    }

    // Friend Request operations
    public void addFriendRequest(String fromPhoneNumber, String fromName, String fromContactId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FR_ID, UUID.randomUUID().toString());
        cv.put(COL_FR_FROM_PHONE, fromPhoneNumber);
        cv.put(COL_FR_FROM_NAME, fromName);
        if (fromContactId != null) {
            cv.put(COL_FR_FROM_CONTACT_ID, fromContactId);
        }
        cv.put(COL_FR_TIMESTAMP, System.currentTimeMillis());
        cv.put(COL_FR_STATUS, "pending");
        db.insert(TABLE_FRIEND_REQUESTS, null, cv);
    }

    public List<FriendRequest> getPendingFriendRequests() {
        List<FriendRequest> requests = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FRIEND_REQUESTS +
                " WHERE " + COL_FR_STATUS + " = 'pending'" +
                " ORDER BY " + COL_FR_TIMESTAMP + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_FR_ID));
                String fromPhone = cursor.getString(cursor.getColumnIndexOrThrow(COL_FR_FROM_PHONE));
                String fromName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FR_FROM_NAME));
                String fromContactId = null;
                int contactIdIndex = cursor.getColumnIndex(COL_FR_FROM_CONTACT_ID);
                if (contactIdIndex >= 0 && !cursor.isNull(contactIdIndex)) {
                    fromContactId = cursor.getString(contactIdIndex);
                }
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FR_TIMESTAMP));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_FR_STATUS));
                requests.add(new FriendRequest(id, fromPhone, fromName, fromContactId, timestamp, status));
            }
            cursor.close();
        }
        return requests;
    }

    public void updateFriendRequestStatus(String requestId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FR_STATUS, status);
        db.update(TABLE_FRIEND_REQUESTS, cv, COL_FR_ID + " = ?", new String[]{requestId});
    }

    public Contact findContactByPhoneNumber(String phoneNumber) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, null, COL_CONTACT_PHONE + " = ?",
                new String[]{phoneNumber}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_NAME));
            String phone = null;
            try {
                int phoneIndex = cursor.getColumnIndex(COL_CONTACT_PHONE);
                if (phoneIndex >= 0) {
                    phone = cursor.getString(phoneIndex);
                }
            } catch (Exception e) {
                // Column might not exist in old database
            }
            String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_AVATAR_URL));
            cursor.close();
            return new Contact(id, name, phone, avatarUrl);
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        return CURRENT_USER_ID;
    }

    /**
     * Get current user Contact
     */
    public Contact getCurrentUser() {
        return findContactById(CURRENT_USER_ID);
    }

    /**
     * Find contact by ID
     */
    public Contact findContactById(String contactId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, null, COL_CONTACT_ID + " = ?",
                new String[]{contactId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_NAME));
            String phone = null;
            try {
                int phoneIndex = cursor.getColumnIndex(COL_CONTACT_PHONE);
                if (phoneIndex >= 0) {
                    phone = cursor.getString(phoneIndex);
                }
            } catch (Exception e) {
                // Column might not exist in old database
            }
            String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_AVATAR_URL));
            cursor.close();
            return new Contact(id, name, phone, avatarUrl);
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    /**
     * Get other members of a group, excluding the current user.
     */
    public List<Contact> getOtherMembers(Group group) {
        List<Contact> otherMembers = new ArrayList<>();
        Contact currentUser = getCurrentUser();
        for (Contact member : group.getMembers()) {
            if (!member.getId().equals(currentUser.getId())) {
                otherMembers.add(member);
            }
        }
        return otherMembers;
    }
}
