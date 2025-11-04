package com.example.zalo_pe_se184586.model;

public class DiscoverItem {
    public static final String TYPE_NEWS = "news";
    public static final String TYPE_TRENDING = "trending";

    private final String id;
    private final String type; // "news" or "trending"
    private final String title;
    private final String imageUrl;
    private final String content;
    private final String author;
    private final long timestamp;

    public DiscoverItem(String id, String type, String title, String imageUrl, String content, String author, long timestamp) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.imageUrl = imageUrl;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoverItem that = (DiscoverItem) o;
        return timestamp == that.timestamp &&
                java.util.Objects.equals(id, that.id) &&
                java.util.Objects.equals(type, that.type) &&
                java.util.Objects.equals(title, that.title) &&
                java.util.Objects.equals(imageUrl, that.imageUrl) &&
                java.util.Objects.equals(content, that.content) &&
                java.util.Objects.equals(author, that.author);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
