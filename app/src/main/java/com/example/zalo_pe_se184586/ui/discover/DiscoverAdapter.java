package com.example.zalo_pe_se184586.ui.discover;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zalo_pe_se184586.R;
import com.example.zalo_pe_se184586.model.DiscoverItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiscoverAdapter extends ListAdapter<DiscoverItem, DiscoverAdapter.DiscoverViewHolder> {

    public DiscoverAdapter() {
        super(new DiffUtil.ItemCallback<DiscoverItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull DiscoverItem oldItem, @NonNull DiscoverItem newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull DiscoverItem oldItem, @NonNull DiscoverItem newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public DiscoverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discover, parent, false);
        return new DiscoverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoverViewHolder holder, int position) {
        DiscoverItem item = getItem(position);
        holder.bind(item);
    }

    static class DiscoverViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView titleView;
        private final TextView contentView;
        private final TextView authorView;
        private final TextView timeView;
        private final TextView typeBadge;

        DiscoverViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            titleView = itemView.findViewById(R.id.item_title);
            contentView = itemView.findViewById(R.id.item_content);
            authorView = itemView.findViewById(R.id.item_author);
            timeView = itemView.findViewById(R.id.item_time);
            typeBadge = itemView.findViewById(R.id.item_type_badge);
        }

        void bind(DiscoverItem item) {
            titleView.setText(item.getTitle());
            contentView.setText(item.getContent());
            authorView.setText(item.getAuthor());

            // Format time
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            timeView.setText(sdf.format(new Date(item.getTimestamp())));

            // Set type badge
            if (DiscoverItem.TYPE_TRENDING.equals(item.getType())) {
                typeBadge.setText("🔥 Trending");
                typeBadge.setVisibility(View.VISIBLE);
            } else {
                typeBadge.setText("📰 News");
                typeBadge.setVisibility(View.VISIBLE);
            }

            // For now, use placeholder drawable (in real app, use Glide/Picasso to load imageUrl)
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                // Placeholder image - in production, load from URL
                imageView.setImageResource(R.drawable.bg_avatar_circle);
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}
