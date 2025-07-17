package com.s23010269.skill4u;

public class UserModel {
    private String uid;
    private String name;

    public UserModel() {}

    public UserModel(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }
}
