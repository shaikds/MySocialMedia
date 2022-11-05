package com.shaikds.togather.model;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    String name, prof, company, bio, email, web, url, uid, phone, password;
    boolean isApproved;
    ArrayList<String> groupLikes;


    public User(String name, String prof, String company, String bio, String email, String web, String url, String uid, String phone, ArrayList<String> groupLikes, String password) {
        this.name = name;
        this.prof = prof;
        this.company = company;
        this.bio = bio;
        this.email = email;
        this.web = web;
        this.url = url;
        this.uid = uid;
        this.phone = phone;
        this.groupLikes = groupLikes;
        this.password = password;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProf() {
        return prof;
    }

    public void setProf(String prof) {
        this.prof = prof;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }


    public ArrayList<String> getGroupLikes() {
        if (groupLikes == null) {
            groupLikes = new ArrayList<>();
        }
        return groupLikes;
    }

    public void setGroupLikes(ArrayList<String> groupLikes) {

        this.groupLikes = groupLikes;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
