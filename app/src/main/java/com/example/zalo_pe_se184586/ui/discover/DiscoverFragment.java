package com.example.zalo_pe_se184586.ui.discover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zalo_pe_se184586.R;

public class DiscoverFragment extends Fragment {

    public DiscoverFragment() {
        // Bắt buộc phải có constructor rỗng
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Liên kết layout fragment_discover.xml
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }
}
