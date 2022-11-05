package com.shaikds.togather.model;

import java.util.ArrayList;

public class Post {
    private String title;
    private String description;
    private String email;
    private String minPeople;
    private String maxPeople;
    private String groupPrice;
    private String originalPrice;
    private String location;
    private String category;
    private int usersCount;
    private String shipmentMethod;
    String groupManager;
    String groupId;
    String uid;
    String startTime;
    String endTime;
    String url;
    String postImageUri;
    String type;
    boolean groupStarted;





    ArrayList<String> users = new ArrayList<>();

    public String getGroupManager() {
        return groupManager;
    }

    public void setGroupManager(String groupManager) {
        this.groupManager = groupManager;
    }

    public Post() {
        this.usersCount = 1;
    }


    public void addUser(String userUid) {
        if (!(users.contains(userUid))) {
            this.setUsersCount(this.getUsersCount() + 1);
            users.add(userUid);
            this.setUsers(users);
        }

    }

    public void removeUser(String userId) {
        if (users.contains(userId)) {
            this.setUsersCount(this.getUsersCount() - 1);
            users.remove(userId);
            this.setUsers(users);
        }
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMinPeople() {
        return minPeople;
    }

    public void setMinPeople(String minPeople) {
        this.minPeople = minPeople;
    }

    public String getMaxPeople() {
        return maxPeople;
    }

    public void setMaxPeople(String maxPeople) {
        this.maxPeople = maxPeople;
    }

    public int getUsersCount() {
        return usersCount;
    }

    public void setUsersCount(int usersCount) {
        this.usersCount = usersCount;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getGroupPrice() {
        return groupPrice;
    }

    public void setGroupPrice(String groupPrice) {
        this.groupPrice = groupPrice;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPostImageUri() {
        return postImageUri;
    }

    public void setPostImageUri(String postUri) {
        this.postImageUri = postUri;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
        //this.addUser(uid);

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public boolean isGroupStarted() {
        return groupStarted;
    }

    public void setGroupStarted(boolean groupStarted) {
        this.groupStarted = groupStarted;
    }

    public boolean checkIfMember(String uid) {
        for (String user : this.users) {
            if (user.equals(uid)) {
                return true;
            }
        }
        return false;
    }

    public String getShipmentMethod() {
        return shipmentMethod;
    }

    public void setShipmentMethod(String shipmentMethod) {
        this.shipmentMethod = shipmentMethod;
    }


}
