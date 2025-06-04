package com.only.friends;

import com.google.firebase.database.Exclude;

public class Post {

    @Exclude
    private String id;    private String userId;
    private String userName;
    private String userEmail;
    private String content; // filename of uploaded image in Firebase Storage
    private String caption; // user's caption/description for the post
    private long timestamp;

    public Post() {
    }    public Post(String userId, String userName, String userEmail, String content, String caption, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.content = content;
        this.caption = caption;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
