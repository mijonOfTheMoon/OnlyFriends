package com.only.friends;

import com.google.firebase.database.Exclude;

public class User {

    @Exclude
    private String id;
    private String name;

    public User() {

    }

    public User(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
