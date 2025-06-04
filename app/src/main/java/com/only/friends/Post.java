package com.only.friends;

import com.google.firebase.database.Exclude;

public class Post {

    @Exclude
    private String id;    
    private String userId;
    private String userName;
    private String content;
    private String caption;
    private long timestamp;

    public Post() {
    }

    public Post(String userId, String userName, String content, String caption, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.caption = caption;
        this.timestamp = timestamp;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCaption() {
        return caption;
    }

}
