package com.example.zalo_pe_se184586.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zalo_pe_se184586.databinding.FragmentDiscoverBinding;
import com.example.zalo_pe_se184586.model.DiscoverItem;
import com.example.zalo_pe_se184586.ui.discover.DiscoverAdapter;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    private FragmentDiscoverBinding binding;
    private DiscoverAdapter adapter;
    private List<DiscoverItem> allItems = new ArrayList<>();
    private String currentFilter = DiscoverItem.TYPE_NEWS; // "news" or "trending"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        adapter = new DiscoverAdapter();
        binding.discoverRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.discoverRecycler.setAdapter(adapter);
        binding.discoverRecycler.setVisibility(View.VISIBLE);

        // Load sample data
        loadSampleData();

        // Setup button listeners
        binding.btnDiscoverNews.setOnClickListener(v -> {
            if (!DiscoverItem.TYPE_NEWS.equals(currentFilter)) {
                currentFilter = DiscoverItem.TYPE_NEWS;
                filterItems();
                updateButtonStates(true);
            }
        });

        binding.btnDiscoverTrending.setOnClickListener(v -> {
            if (!DiscoverItem.TYPE_TRENDING.equals(currentFilter)) {
                currentFilter = DiscoverItem.TYPE_TRENDING;
                filterItems();
                updateButtonStates(false);
            }
        });
        
        // Set initial state - News is active
        updateButtonStates(true);

        binding.btnRefreshDiscover.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Refreshing...", Toast.LENGTH_SHORT).show();
            filterItems();
        });

        binding.btnExploreMore.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Exploring more content...", Toast.LENGTH_SHORT).show();
        });

        // Initial filter
        filterItems();
    }

    private void updateButtonStates(boolean newsActive) {
        if (newsActive) {
            // News is active
            binding.btnDiscoverNews.setAlpha(1.0f);
            binding.btnDiscoverNews.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.btnDiscoverNews.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
            binding.btnDiscoverTrending.setAlpha(0.6f);
            binding.btnDiscoverTrending.setTypeface(null, android.graphics.Typeface.NORMAL);
            binding.btnDiscoverTrending.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        } else {
            // Trending is active
            binding.btnDiscoverTrending.setAlpha(1.0f);
            binding.btnDiscoverTrending.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.btnDiscoverTrending.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
            binding.btnDiscoverNews.setAlpha(0.6f);
            binding.btnDiscoverNews.setTypeface(null, android.graphics.Typeface.NORMAL);
            binding.btnDiscoverNews.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        }
    }

    private void loadSampleData() {
        allItems.clear(); // Clear existing items to avoid duplicates
        long now = System.currentTimeMillis();
        
        // News items
        allItems.add(new DiscoverItem(
            "news_1",
            DiscoverItem.TYPE_NEWS,
            "Công nghệ AI đang thay đổi cuộc sống hàng ngày! 🤖",
            "https://example.com/ai.jpg",
            "Từ trợ lý ảo đến xe tự lái, AI đang trở thành một phần không thể thiếu. Hãy cùng khám phá những ứng dụng thú vị nhất!",
            "Tech Reporter",
            now - 3600000
        ));

        allItems.add(new DiscoverItem(
            "news_2",
            DiscoverItem.TYPE_NEWS,
            "Bữa sáng Việt Nam được bình chọn ngon nhất thế giới 🍜",
            "https://example.com/pho.jpg",
            "Phở, bánh mì, bún chả... ẩm thực Việt Nam đang chinh phục thế giới! Bạn đã thử hết chưa?",
            "Food Blogger",
            now - 7200000
        ));

        allItems.add(new DiscoverItem(
            "news_3",
            DiscoverItem.TYPE_NEWS,
            "10 tips học code hiệu quả cho người mới bắt đầu 💻",
            "https://example.com/code.jpg",
            "Học lập trình không khó nếu bạn biết cách! Chia sẻ từ các developer hàng đầu về kinh nghiệm học code.",
            "Code Master",
            now - 10800000
        ));

        // Trending items
        allItems.add(new DiscoverItem(
            "trending_1",
            DiscoverItem.TYPE_TRENDING,
            "Challenge mới: 7 ngày không dùng điện thoại 📱❌",
            "https://example.com/challenge.jpg",
            "Thử thách khó nhất nhưng bổ ích nhất! Ai dám thử không? Comment số ngày bạn nghĩ mình có thể làm được!",
            "Trend Setter",
            now - 1800000
        ));

        allItems.add(new DiscoverItem(
            "trending_2",
            DiscoverItem.TYPE_TRENDING,
            "Meme hôm nay: Khi deadline đến gần 😂",
            "https://example.com/meme.jpg",
            "Ai cũng từng trải qua cảm giác này! Share nếu bạn đồng cảm! Tag ngay bạn bè đang làm việc đến khuya!",
            "Meme King",
            now - 3600000
        ));

        allItems.add(new DiscoverItem(
            "trending_3",
            DiscoverItem.TYPE_TRENDING,
            "Xu hướng: Tự làm đồ handmade tại nhà 🎨",
            "https://example.com/handmade.jpg",
            "Ở nhà nhiều quá, tại sao không thử làm đồ handmade? Vừa vui vừa có quà tặng bạn bè!",
            "DIY Queen",
            now - 5400000
        ));

        allItems.add(new DiscoverItem(
            "trending_4",
            DiscoverItem.TYPE_TRENDING,
            "Hot: Bài hát mới của Sơn Tùng M-TP đã ra mắt! 🎵",
            "https://example.com/music.jpg",
            "Bản hit mới nhất đang làm mưa làm gió các bảng xếp hạng! Nghe ngay và cho biết bạn nghĩ gì!",
            "Music Lover",
            now - 900000
        ));

        allItems.add(new DiscoverItem(
            "news_4",
            DiscoverItem.TYPE_NEWS,
            "Kỳ nghỉ lễ đang đến gần! Nơi nào đẹp nhất để đi? ✈️",
            "https://example.com/travel.jpg",
            "Mùa du lịch sắp đến rồi! Cùng khám phá những điểm đến tuyệt vời trong nước và quốc tế.",
            "Travel Expert",
            now - 14400000
        ));

        allItems.add(new DiscoverItem(
            "trending_5",
            DiscoverItem.TYPE_TRENDING,
            "Viral: Chú mèo biết mở cửa tủ lạnh 🐱",
            "https://example.com/cat.jpg",
            "Video này đang được chia sẻ rầm rộ! Chú mèo thông minh này đã học cách tự lấy thức ăn. Xem ngay!",
            "Pet Lover",
            now - 2700000
        ));
    }

    private void filterItems() {
        List<DiscoverItem> filtered = new ArrayList<>();
        for (DiscoverItem item : allItems) {
            if (currentFilter != null && currentFilter.equals(item.getType())) {
                filtered.add(item);
            }
        }
        
        // Debug: Log số lượng items
        android.util.Log.d("DiscoverFragment", "Filter: " + currentFilter + ", Items found: " + filtered.size() + ", Total items: " + allItems.size());
        
        // Submit list to adapter
        adapter.submitList(new ArrayList<>(filtered)); // Create new list to trigger diff
        
        // Update visibility
        if (filtered.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.discoverRecycler.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.discoverRecycler.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}