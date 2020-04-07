package com.my.socialstress.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class Question {
    private int id;
    private int answer;
    private int count;
    private String date;
    private String userid;

    public Question() {
    }

    public Question(int id, String userid, int answer, String date, int count) {
        this.id = id;
        this.userid = userid;
        this.answer = answer;
        this.date = date;
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCount(int count) {
        this.count = count;
    }
    public int getCount() {
        return count;
    }

}
