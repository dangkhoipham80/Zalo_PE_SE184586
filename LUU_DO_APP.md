# 📖 LUỒNG ĐI CỦA FRIEND, GROUP VÀ MESSAGING TRONG APP

## 🏗️ KIẾN TRÚC TỔNG QUAN

```
UI Layer (Fragment/Activity)
    ↓
ViewModel (quản lý state, logic)
    ↓
Repository (caching, business logic)
    ↓
Database (SQLite - AppDatabase)
```

---

## 👥 1. FRIEND SYSTEM (Hệ thống bạn bè)

### 1.1 Database Schema

**Bảng `contacts`:**

- `id` (PRIMARY KEY)
- `name`
- `phone_number`
- `avatar_url`

**Bảng `friends`:**

- `contact_id` (PRIMARY KEY, FOREIGN KEY → contacts.id)
- Quan hệ: Một contact có thể là friend (1-1 relationship)

**Bảng `friend_requests`:**

- `id` (PRIMARY KEY)
- `from_phone_number` (số điện thoại người gửi)
- `from_name` (tên người gửi)
- `from_contact_id` (NULL nếu người gửi chưa có trong contacts)
- `timestamp`
- `status` ("pending", "accepted", "rejected")

### 1.2 Luồng Thêm Friend

#### A. Gửi Friend Request (chưa có trong code UI, nhưng có trong DB)

```
User nhập số điện thoại
    ↓
ContactRepository.addFriendRequest(phoneNumber, name, contactId)
    ↓
AppDatabase.addFriendRequest()
    ↓
INSERT INTO friend_requests
    (id, from_phone_number, from_name, from_contact_id, timestamp, status)
VALUES (UUID, phoneNumber, name, contactId, now, "pending")
```

#### B. Accept Friend Request

```
User chọn Accept trong danh sách friend requests
    ↓
ContactRepository.acceptFriendRequest(requestId)
    ↓
AppDatabase.updateFriendRequestStatus(requestId, "accepted")
    ↓
UPDATE friend_requests SET status = "accepted" WHERE id = requestId
    ↓
(Nếu có from_contact_id):
    AppDatabase.addFriend(contactId)
    ↓
    INSERT INTO friends (contact_id) VALUES (contactId)
```

#### C. Reject Friend Request

```
User chọn Reject
    ↓
ContactRepository.rejectFriendRequest(requestId)
    ↓
AppDatabase.updateFriendRequestStatus(requestId, "rejected")
    ↓
UPDATE friend_requests SET status = "rejected" WHERE id = requestId
```

### 1.3 Lấy Danh Sách Friends

```
ContactsFragment/ContactsViewModel
    ↓
ContactRepository.getFriends()
    ↓
AppDatabase.getFriends()
    ↓
SELECT c.* FROM contacts c
INNER JOIN friends f ON c.id = f.contact_id
ORDER BY c.name ASC
    ↓
Trả về List<Contact>
```

### 1.4 Phân Biệt Friend vs Non-Friend

```
ContactRepository.getNonFriendContacts()
    ↓
1. Lấy tất cả contacts: getAllContacts()
2. Lấy tất cả friends: getFriends()
3. Filter: contacts NOT IN friends
    ↓
Trả về List<Contact> (non-friends)
```

---

## 👨‍👩‍👧‍👦 2. GROUP SYSTEM (Hệ thống nhóm)

### 2.1 Database Schema

**Bảng `groups`:**

- `id` (PRIMARY KEY, UUID)
- `name`

**Bảng `group_members`:**

- `group_id` (FOREIGN KEY → groups.id)
- `contact_id` (FOREIGN KEY → contacts.id)
- PRIMARY KEY (group_id, contact_id) → Một user chỉ có thể tham gia 1 lần

**Lưu ý:** Một group có nhiều members (many-to-many relationship)

### 2.2 Luồng Tạo Group

#### A. Tạo Group Mới (Từ SelectContactsActivity)

```
User chọn contacts (≥2 người) + nhập tên group
    ↓
SelectContactsActivity.createGroup(groupName)
    ↓
GroupRepository.createGroup(name, selectedContacts)
    ↓
AppDatabase.createGroup(name, members)
    ↓
1. Generate UUID cho groupId
2. INSERT INTO groups (id, name) VALUES (groupId, name)
3. FOR EACH member:
   - Kiểm tra avatar, nếu null → tạo default avatar
   - INSERT INTO group_members (group_id, contact_id) VALUES (groupId, member.id)
4. Return Group object
    ↓
GroupRepository: Tạo MutableLiveData<Group> và cache vào HashMap
    ↓
Mở GroupChatActivity với group mới
```

#### B. Tạo 1-1 Chat (Từ ContactsFragment)

```
User click "Send Message" trên một contact
    ↓
ContactsFragment.sendMessageToContact(contact)
    ↓
GroupRepository.findOrCreateOneToOneGroup(contact)
    ↓
AppDatabase.findOrCreateOneToOneGroup(contact)
    ↓
1. Kiểm tra: contact.id != currentUserId
2. Tìm trong tất cả groups:
   - Nếu có group với EXACTLY 2 members
   - Và có cả currentUser + contact
   → Return group đó
3. Nếu không tìm thấy:
   - Tạo group mới với 2 members: [currentUser, contact]
   - Tên group = contact.getName()
   → Return group mới
    ↓
Mở GroupChatActivity với group này
```

**Logic đặc biệt:**

- Chat 1-1 thực chất là một group có 2 members
- Nhưng khi hiển thị, chỉ show tên của người kia (giống Zalo)

### 2.3 Luồng Thêm Member Vào Group

#### A. Thêm từ ContactsFragment

```
User chọn contact → "Add to Group"
    ↓
ContactsFragment.showGroupSelector(contact)
    ↓
Hiển thị dialog danh sách groups
    ↓
User chọn group
    ↓
GroupRepository.addMemberToGroup(groupId, contact)
    ↓
AppDatabase.addMemberToGroup(groupId, contact)
    ↓
1. Kiểm tra: SELECT COUNT(*) FROM group_members
   WHERE group_id = ? AND contact_id = ?
2. Nếu count = 0 (chưa có):
   INSERT INTO group_members (group_id, contact_id) VALUES (groupId, contactId)
3. Reload group từ DB và update LiveData
```

#### B. Thêm từ GroupChatActivity (Menu)

```
User click menu "Add Member" trong group chat
    ↓
GroupChatActivity.showAddMemberDialog()
    ↓
1. Lấy tất cả contacts
2. Filter: contacts NOT IN group.members
3. Hiển thị dialog chọn contact
    ↓
User chọn contact
    ↓
GroupRepository.addMemberToGroup(groupId, contact)
    ↓
(Tương tự như trên)
    ↓
Refresh UI: viewModel.setInitialGroup(updatedGroup)
```

### 2.4 Luồng Xóa Member Khỏi Group

```
User click menu "Remove Member" trong group chat
    ↓
GroupChatActivity.showRemoveMemberDialog()
    ↓
1. Lấy danh sách members (trừ current user)
2. Hiển thị dialog chọn member
    ↓
User chọn member
    ↓
GroupChatActivity.removeMemberFromGroup(groupId, member)
    ↓
AppDatabase.removeMemberFromGroup(groupId, contactId)
    ↓
DELETE FROM group_members
WHERE group_id = ? AND contact_id = ?
    ↓
Refresh UI
```

### 2.5 Luồng Xóa Group

```
User long-press group trong ChatListFragment
    ↓
ChatListFragment.showDeleteGroupDialog(group)
    ↓
User confirm delete
    ↓
GroupRepository.deleteGroup(groupId)
    ↓
AppDatabase.deleteGroup(groupId)
    ↓
1. DELETE FROM messages WHERE group_id = ?
2. DELETE FROM group_members WHERE group_id = ?
3. DELETE FROM groups WHERE id = ?
    ↓
GroupRepository: Xóa khỏi HashMap cache
    ↓
ChatListViewModel.refresh() → Reload danh sách groups
```

### 2.6 Hiển Thị Groups

#### A. Trong ChatListFragment (Messages tab)

```
ChatListFragment.onViewCreated()
    ↓
ChatListViewModel.getGroups().observe()
    ↓
ChatListViewModel.loadInitial()
    ↓
GroupRepository.getAllGroups()
    ↓
1. Kiểm tra HashMap cache
2. Nếu rỗng → Load từ AppDatabase.getAllGroups()
   - SELECT * FROM groups ORDER BY name
   - FOR EACH group:
     * getGroupMembers(groupId) → JOIN với contacts
     * getGroupMessages(groupId) → SELECT * FROM messages WHERE group_id = ?
   - Tạo Group object với members + messages
3. Cache vào HashMap
    ↓
Sort groups theo latest message timestamp (mới nhất lên đầu)
    ↓
ChatListAdapter.submitList(groups)
```

#### B. Trong ContactsFragment (Groups tab)

```
ContactsFragment: Tab "Groups" selected
    ↓
GroupRepository.getAllGroups()
    ↓
Filter: Chỉ lấy groups có members.size() > 2
(Ẩn các chat 1-1)
    ↓
Sort theo latest message timestamp
    ↓
ChatListAdapter.submitList(filteredGroups)
```

---

## 💬 3. MESSAGING SYSTEM (Hệ thống tin nhắn)

### 3.1 Database Schema

**Bảng `messages`:**

- `id` (PRIMARY KEY, UUID)
- `group_id` (FOREIGN KEY → groups.id)
- `sender_name` (String - tên người gửi)
- `content` (String - nội dung tin nhắn)
- `timestamp` (Long - thời gian gửi)

**Lưu ý:**

- Tất cả tin nhắn đều thuộc về một group
- Chat 1-1 cũng là một group (có 2 members)
- Sử dụng `sender_name` thay vì `sender_id` (đơn giản hóa)

### 3.2 Luồng Gửi Tin Nhắn

```
User nhập tin nhắn trong GroupChatActivity
    ↓
User click Send button
    ↓
GroupChatActivity.submitMessage()
    ↓
viewModel.sendMessage(content)
    ↓
GroupChatViewModel.sendMessage(content)
    ↓
1. Lấy groupId từ SavedStateHandle
2. Tạo Message object:
   - id = UUID.randomUUID()
   - groupId
   - senderName = "You" (hardcoded - trong thực tế nên dùng currentUser.name)
   - content
   - timestamp = System.currentTimeMillis()
    ↓
GroupRepository.addMessage(groupId, message)
    ↓
1. AppDatabase.addMessage(groupId, message)
   INSERT INTO messages (id, group_id, sender_name, content, timestamp)
   VALUES (msgId, groupId, senderName, content, timestamp)
2. Update LiveData:
   - Lấy Group từ HashMap cache
   - group.addMessage(message) → Thêm vào ArrayList trong memory
   - liveData.setValue(group) → Notify observers
    ↓
GroupChatActivity.observe(viewModel.getGroup())
    ↓
updateUI(group) được gọi
    ↓
MessageAdapter.submitList(group.getMessages())
    ↓
RecyclerView hiển thị tin nhắn mới
```

### 3.3 Luồng Load Tin Nhắn Khi Mở Chat

```
GroupChatActivity.onCreate()
    ↓
Intent.getParcelableExtra(EXTRA_GROUP) → Lấy Group object
    ↓
viewModel.setInitialGroup(group)
    ↓
GroupChatViewModel.setInitialGroup(group)
    ↓
1. Lưu groupId vào SavedStateHandle
2. attachGroup(groupId)
   - GroupRepository.getGroupLiveData(groupId)
   - Tạo MutableLiveData nếu chưa có
   - Subscribe để nhận updates
    ↓
viewModel.getGroup().observe(this, this::updateUI)
    ↓
updateUI(group)
    ↓
1. binding.toolbar.setTitle(group.getName())
2. adapter.submitList(group.getMessages())
3. Scroll to bottom (latest message)
```

### 3.4 Luồng Load Messages Từ Database

```
AppDatabase.getAllGroups()
    ↓
FOR EACH group:
    getGroupMessages(db, groupId)
    ↓
SELECT * FROM messages
WHERE group_id = ?
ORDER BY timestamp ASC
    ↓
Tạo List<Message>
    ↓
Tạo Group object với messages này
```

**Lưu ý:** Messages được sort theo timestamp ASC (cũ → mới) để hiển thị đúng thứ tự

### 3.5 Hiển Thị Preview Tin Nhắn (Last Message)

```
ChatListAdapter.bind(Group group)
    ↓
Message lastMessage = group.getLastMessage()
    ↓
Group.getLastMessage()
    ↓
if (messages.isEmpty()) return null
else return messages.get(messages.size() - 1)
    ↓
Hiển thị: "SenderName: content"
Ví dụ: "Alice Nguyen: Hey everyone! How are you?"
```

---

## 🔄 4. DATA FLOW TỔNG QUAN

### 4.1 Repository Pattern

```
Repository (GroupRepository, ContactRepository)
├── Cache: HashMap<String, MutableLiveData<Group>>
├── Load từ Database khi cần
└── Update LiveData khi có thay đổi
```

**Ưu điểm:**

- Cache trong memory → Nhanh
- LiveData → Reactive updates
- Single source of truth (Database)

### 4.2 ViewModel Pattern

```
ViewModel (GroupChatViewModel, ChatListViewModel)
├── Giữ state qua SavedStateHandle
├── Expose LiveData cho UI observe
└── Xử lý business logic
```

**SavedStateHandle:** Giữ groupId qua configuration changes và process death

### 4.3 LiveData Flow

```
Database (SQLite)
    ↓
Repository (caches + exposes LiveData)
    ↓
ViewModel (exposes LiveData to UI)
    ↓
Fragment/Activity (observe LiveData)
    ↓
UI updates automatically
```

---

## 📊 5. SƠ ĐỒ QUAN HỆ CỦA CÁC BẢNG

```
contacts (1) ────── (N) group_members (N) ────── (1) groups
    │                                                  │
    │                                                  │
    └─── (1) friends                                   │
                                                       │
                                                   (N) messages
```

**Giải thích:**

- 1 contact có thể tham gia nhiều groups (qua group_members)
- 1 group có nhiều members (qua group_members)
- 1 group có nhiều messages
- 1 contact có thể là friend (1-1 với bảng friends)

---

## 🎯 6. CÁC ĐIỂM QUAN TRỌNG

### 6.1 Current User

- Hardcoded: `CURRENT_USER_ID = "1"` (Alice Nguyen)
- Trong thực tế nên lưu trong SharedPreferences hoặc từ authentication

### 6.2 Chat 1-1 vs Group Chat

- **1-1 chat:** Group có EXACTLY 2 members (currentUser + contact)
- **Group chat:** Group có >2 members
- UI filter: Trong "Groups" tab, chỉ hiển thị groups có >2 members

### 6.3 Avatar Default

- Khi tạo contact/group mới, nếu không có avatar → Tự động generate từ ui-avatars.com
- URL format: `https://ui-avatars.com/api/?name={name}&background={color}`

### 6.4 Message Sender

- Hiện tại dùng `sender_name` = "You" (hardcoded)
- Nên thay bằng: `AppDatabase.getCurrentUser().getName()`

### 6.5 Sorting

- Groups được sort theo `lastMessage.timestamp` (DESC - mới nhất lên đầu)
- Messages được sort theo `timestamp` (ASC - cũ → mới)

---

## 📝 7. VÍ DỤ LUỒNG HOÀN CHỈNH

### Ví dụ: User A gửi tin nhắn cho User B

```
1. User A mở ContactsFragment
2. Click vào User B → "Send Message"
3. ContactsFragment.sendMessageToContact(contactB)
4. GroupRepository.findOrCreateOneToOneGroup(contactB)
   - Tìm group có [currentUser, contactB]
   - Không tìm thấy → Tạo mới
5. Mở GroupChatActivity với group này
6. User A nhập "Hello!" → Click Send
7. GroupChatViewModel.sendMessage("Hello!")
8. GroupRepository.addMessage(groupId, message)
   - INSERT vào database
   - Update LiveData
9. GroupChatActivity nhận update → Hiển thị tin nhắn
```

### Ví dụ: Tạo group chat mới

```
1. User click FAB hoặc "New Group"
2. Mở SelectContactsActivity
3. User chọn 3 contacts: A, B, C
4. User nhập tên: "Study Group"
5. Click "Create Group"
6. SelectContactsActivity.createGroup("Study Group")
7. GroupRepository.createGroup("Study Group", [A, B, C, currentUser])
8. AppDatabase.createGroup():
   - Tạo group mới với UUID
   - INSERT 4 records vào group_members
9. Mở GroupChatActivity với group mới
```

---

## 🔍 8. CÁC METHOD QUAN TRỌNG

### AppDatabase

- `getAllContacts()` - Lấy tất cả contacts
- `getFriends()` - Lấy danh sách friends
- `addFriend(contactId)` - Thêm friend
- `createGroup(name, members)` - Tạo group mới
- `findOrCreateOneToOneGroup(contact)` - Tìm/tạo chat 1-1
- `addMemberToGroup(groupId, contact)` - Thêm member
- `removeMemberFromGroup(groupId, contactId)` - Xóa member
- `deleteGroup(groupId)` - Xóa group
- `addMessage(groupId, message)` - Thêm tin nhắn
- `getGroupMessages(groupId)` - Lấy tin nhắn của group

### GroupRepository

- `createGroup(name, members)` - Tạo group + cache
- `findOrCreateOneToOneGroup(contact)` - Tìm/tạo 1-1 chat
- `addMessage(groupId, message)` - Thêm tin nhắn + update LiveData
- `getGroupLiveData(groupId)` - Lấy LiveData của group
- `getAllGroups()` - Lấy tất cả groups (từ cache hoặc DB)

### ContactRepository

- `getContacts()` - Lấy tất cả contacts
- `getFriends()` - Lấy friends
- `getNonFriendContacts()` - Lấy non-friends
- `addFriendRequest(...)` - Gửi friend request
- `acceptFriendRequest(requestId)` - Chấp nhận request
- `rejectFriendRequest(requestId)` - Từ chối request

---

**Kết luận:** App sử dụng kiến trúc MVVM với Repository pattern, lưu trữ persistent bằng SQLite, và sử dụng LiveData để reactive updates. Tất cả chat (1-1 và group) đều được quản lý như một Group trong database.
