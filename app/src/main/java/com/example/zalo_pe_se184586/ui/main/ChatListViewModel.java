package com.example.zalo_pe_se184586.ui.main;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zalo_pe_se184586.data.ContactRepository;
import com.example.zalo_pe_se184586.data.GroupRepository;
import com.example.zalo_pe_se184586.model.Contact;
import com.example.zalo_pe_se184586.model.Group;
import com.example.zalo_pe_se184586.model.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ViewModel cho danh sách chat (danh sách Group).
 * Cung cấp getGroups() và search(String) đúng như ChatListFragment đang gọi.
 */
public class ChatListViewModel extends AndroidViewModel {

    private final GroupRepository groupRepo;
    private final ContactRepository contactRepo;
    // Danh sách gốc và danh sách hiển thị (lọc theo search)
    private final List<Group> original = new ArrayList<>();
    private final MutableLiveData<List<Group>> groupsLiveData = new MutableLiveData<>(Collections.emptyList());

    public ChatListViewModel(Application application) {
        super(application);
        groupRepo = GroupRepository.getInstance(application);
        contactRepo = ContactRepository.getInstance(application);
        loadInitial();
    }

    private void loadInitial() {
        // Lấy các group hiện có trong repo
        List<Group> all = groupRepo.getAllGroups();

        // Nếu trống, seed 1–2 group mẫu từ ContactRepository để có dữ liệu hiển thị
        if (all == null || all.isEmpty()) {
            List<Contact> contacts = contactRepo.getContacts();
            List<Contact> seed1 = new ArrayList<>();
            List<Contact> seed2 = new ArrayList<>();

            if (!contacts.isEmpty()) seed1.add(contacts.get(0)); // Alice Nguyen
            if (contacts.size() > 1) seed1.add(contacts.get(1)); // Bao Tran
            if (contacts.size() > 2) seed2.add(contacts.get(2)); // Chi Le
            if (contacts.size() > 3) seed2.add(contacts.get(3)); // Duy Pham

            Group g1 = groupRepo.createGroup("Alice & Bao", seed1);
            Group g2 = groupRepo.createGroup("Chi & Duy", seed2);

            // Thêm vài message mẫu để adapter có lastMessage hợp lý
            groupRepo.addMessage(g1.getId(), new Message("1", g1.getId(), "Alice Nguyen", "Hello, how are you?", System.currentTimeMillis()));
            groupRepo.addMessage(g1.getId(), new Message("2", g1.getId(), "Bao Tran", "I'm good! You?", System.currentTimeMillis()));
            groupRepo.addMessage(g2.getId(), new Message("3", g2.getId(), "Chi Le", "Đi cf không?", System.currentTimeMillis()));
            groupRepo.addMessage(g2.getId(), new Message("4", g2.getId(), "Duy Pham", "Ok nhé!", System.currentTimeMillis()));

            all = groupRepo.getAllGroups();
        }

        original.clear();
        if (all != null) original.addAll(all);
        // Sort by latest message timestamp (descending)
        sortGroupsByLatestMessage(original);
        groupsLiveData.setValue(new ArrayList<>(original));
    }

    /** LiveData để Fragment observe */
    public LiveData<List<Group>> getGroups() {
        return groupsLiveData;
    }

    /** Lọc theo tên group (hoặc theo tên thành viên nếu bạn muốn mở rộng) */
    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            List<Group> sorted = new ArrayList<>(original);
            sortGroupsByLatestMessage(sorted);
            groupsLiveData.setValue(sorted);
            return;
        }
        String q = query.trim().toLowerCase();
        List<Group> filtered = new ArrayList<>();
        for (Group g : original) {
            boolean matchName = g.getName() != null && g.getName().toLowerCase().contains(q);
            if (matchName) {
                filtered.add(g);
                continue;
            }
            // Mở rộng: khớp theo tên thành viên
            if (g.getMembers() != null) {
                for (Contact c : g.getMembers()) {
                    if (c.getName() != null && c.getName().toLowerCase().contains(q)) {
                        filtered.add(g);
                        break;
                    }
                }
            }
        }
        // Sort filtered results by latest message timestamp (descending)
        sortGroupsByLatestMessage(filtered);
        groupsLiveData.setValue(filtered);
    }

    /** Gọi khi cần reload lại từ repository (sau khi xóa group, tạo mới, etc.) */
    public void refresh() {
        // Refresh repository first to reload from database
        groupRepo.refreshGroups();
        List<Group> all = groupRepo.getAllGroups();
        original.clear();
        if (all != null) original.addAll(all);
        // Sort by latest message timestamp (descending)
        sortGroupsByLatestMessage(original);
        groupsLiveData.setValue(new ArrayList<>(original));
    }

    /**
     * Sắp xếp groups theo thời gian tin nhắn mới nhất (descending - mới nhất ở đầu)
     * Groups không có tin nhắn sẽ được đặt ở cuối
     */
    private void sortGroupsByLatestMessage(List<Group> groups) {
        Collections.sort(groups, (g1, g2) -> {
            Message lastMsg1 = g1.getLastMessage();
            Message lastMsg2 = g2.getLastMessage();
            
            // Nếu cả hai đều có tin nhắn, sort theo timestamp (descending)
            if (lastMsg1 != null && lastMsg2 != null) {
                return Long.compare(lastMsg2.getTimestamp(), lastMsg1.getTimestamp());
            }
            // Group có tin nhắn đứng trước group không có tin nhắn
            if (lastMsg1 != null) return -1;
            if (lastMsg2 != null) return 1;
            // Cả hai đều không có tin nhắn, giữ nguyên thứ tự
            return 0;
        });
    }
}
