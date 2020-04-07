package com.my.socialstress.utils;

import java.util.Date;
import java.util.List;

public class User {
    private String image, name, email, password, user_id, social, friendList, ties;

    public User() {}

    public User(String user_id, String image, String name, String social, String email, String password, String friendList, String ties) {
        this.user_id = user_id;
        this.image = image;
        this.name = name;
        this.social = social;
        this.email = email;
        this.password = password;
        this.friendList = friendList;
        this.ties = ties;
    }

    public String getUser_id()
    {
        return  user_id;
    }

    public  void  setUser_id(String user_id)
    {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSocial() {
        return social;
    }

    public void setSocial(String social) {
        this.social = social;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFriendList() {
        return friendList;
    }

    public void setFriendList(String friendList) {
        this.friendList = friendList;
    }

    public String getTies() {
        return ties;
    }

    public void setTies(String ties) {
        this.ties = ties;
    }

}
