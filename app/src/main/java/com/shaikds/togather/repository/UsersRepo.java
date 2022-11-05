package com.shaikds.togather.repository;

import android.util.Log;

import com.shaikds.togather.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UsersRepo {
    List<User> usersList = new ArrayList<>();
    OnUsersInterface onUsersInterface;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    public FirebaseUser firebaseUser;

    final CollectionReference cloudAllUsers = firebaseFirestore.collection("user");

    public UsersRepo(OnUsersInterface onUsersInterface) {
        this.onUsersInterface = onUsersInterface;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public List<User> getAllUsers() {
        cloudAllUsers.addSnapshotListener((value, error) -> {
            usersList.clear();
            if (value != null) {
                for (DocumentSnapshot snapshot : value.getDocuments()) {
                    /*
                conditions to fulfill search:
                *1) User not current user
                *2) The user name or email entered in search view.(case insensitive)
                 */
                    User user = snapshot.toObject(User.class);
                    usersList.add(user);
                    onUsersInterface.setUsersList(usersList);
                    if (value.getDocuments().get(value.getDocuments().size()-1).equals(snapshot)){
                        // if it's last snap shot so notify its ended.
                        onUsersInterface.setIsUsersLoaded(true);
                    }
                }
            }
        });
        return usersList;
    }

    //update specific group in DB's.
    public void updateUser(User user) {
        //update fire store values that can be changed --> after group has created.
        //can be changed values:
        //end time,users in group+count , uid of manager,description,title,,email of manager,min/max people, location, url,group price.
        cloudAllUsers.document(firebaseUser.getUid()).update("bio", user.getBio(),
                "company", user.getCompany(), "email", user.getEmail()
                , "name", user.getName(), "profession", user.getProf(), "url", user.getUrl(),
                "web", user.getWeb(), "groupLikes", user.getGroupLikes()).addOnCompleteListener(task -> {
            Log.d("UsersRepo", "onComplete: Completed Updating User.");
        });
    }

    public void changeProfileImg(User user) {
        cloudAllUsers.document(firebaseUser.getUid()).update("url", user.getUrl());
    }

    public interface OnUsersInterface {
        void setUsersList(List<User> newUsersList);

        void setIsUsersLoaded(boolean isUsersLoaded);
    }
}


