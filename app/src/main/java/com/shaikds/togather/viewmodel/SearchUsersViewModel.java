package com.shaikds.togather.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.shaikds.togather.model.Post;
import com.shaikds.togather.model.User;
import com.shaikds.togather.repository.UsersRepo;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersViewModel extends AndroidViewModel implements UsersRepo.OnUsersInterface {
    private static String TAG = SearchUsersViewModel.class.getSimpleName();
    MutableLiveData<List<User>> usersLiveData;
    MutableLiveData<Boolean> isUsersLoadedLiveData;
    ArrayList<User> groupUsers = new ArrayList<>();
    User user;


    UsersRepo usersRepo = new UsersRepo(this);

    public SearchUsersViewModel(@NonNull Application application) {
        super(application);
        usersLiveData = new MutableLiveData<>();
        isUsersLoadedLiveData= new MutableLiveData<>();
        isUsersLoadedLiveData.postValue(false);
        usersRepo.getAllUsers();

    }

    public User getSpecificUser(String uid) {
        try {
            for (User user : usersLiveData.getValue()) {
                if (user.getUid().equals(uid)) {
                    Log.d(TAG, "getSpecificUser: " + user.getName());
                    return user;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public void changeUserImg(User user) {
        usersRepo.changeProfileImg(user);
    }

    public MutableLiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public void setGroupUsers(Post post) {
        if (groupUsers.size()>0){
            groupUsers.clear();
        }
        groupUsers = new ArrayList<>();
        for (String userUid : post.getUsers()) {
            groupUsers.add(getSpecificUser(userUid));

        }
    }

    public List<User> getGroupUsersDetail() {
        return groupUsers;
    }

    public void myUser(User user) {
        this.user = user;
    }

    public User getMyUser() {
        if (user == null) {
            Log.d(TAG, "getMyUser: user is null new User ");
            return getSpecificUser(usersRepo.firebaseUser.getUid());
        } else {
            return user;
        }
    }

    public void updateUser(User user) {
        if (user != null) {
            usersRepo.updateUser(user);
        }
    }

    @Override
    public void setUsersList(List<User> newUsersList) {
        usersLiveData.postValue(newUsersList);
        Log.d(TAG, "setUsersList: new users list from view model");
    }

    public MutableLiveData<Boolean> getIsUsersLoadedLiveData() {
        return isUsersLoadedLiveData;
    }

    @Override
    public void setIsUsersLoaded(boolean isUsersLoaded) {
        isUsersLoadedLiveData.postValue(isUsersLoaded);
    }
}
