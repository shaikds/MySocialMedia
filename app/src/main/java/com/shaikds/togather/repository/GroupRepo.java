package com.shaikds.togather.repository;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.shaikds.togather.model.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GroupRepo {
    private static final String TAG = "GroupRepo";
    List<Post> postList = new ArrayList<>();
    List<Post> filteredPostList = new ArrayList<>();
    List<Post> postsMap = new ArrayList<>();
    boolean resultFirestore;
    boolean resultRealtime;

    Bundle bundleResult = new Bundle();

    OnPostsInterface postsInterface;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    CollectionReference referenceAllGroups = firebaseFirestore.collection("allgroups");
    final CollectionReference referenceArchive = firebaseFirestore.collection("archived_groups");

    public GroupRepo(OnPostsInterface postsInterface) {
        this.postsInterface = postsInterface;
    }

    //get all posts
    public List<Post> getAllPosts() {
        referenceAllGroups.addSnapshotListener((value, error) -> {
            postList.clear();
            postsMap.clear();
            for (DocumentSnapshot post : value.getDocuments()) {
                Post postModel = post.toObject(Post.class);
                postModel.setGroupId(post.getId()); // put all group id's in every post. only from here. this value is null in db.
                postsMap.add(postModel);
                postList.add(postModel);
                postsInterface.setPostList(postsMap);
                //postsInterface.setFilteredLiveData(postsMap);
                Log.d(TAG, "onEvent: DB UPDATED --> LETS GET NEW LIST.");
            }
        });

        return postsMap;
    }


    //filter posts only on list that downloaded already. we have to use get all posts before we use this method.
    public List<Post> getFilteredPosts(String categoryName) {
        filteredPostList.clear();
        if (categoryName.equals("הכל")) { // if it's all categories, no need to filter.
            filteredPostList.addAll(postList);
            postsInterface.setFilteredPostList(filteredPostList);
            return filteredPostList;
        }
        for (Post post : postList) {
            if (post.getCategory().equals(categoryName)) { // same category only.
                filteredPostList.add(post);
            }
        }
        postsInterface.setFilteredPostList(filteredPostList);
        return filteredPostList;
    }

    public boolean addGroupToDB(Post post) {
        referenceAllGroups.add(post).addOnSuccessListener(documentReference -> {
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bundleResult.putBoolean("add", false);

                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                bundleResult.putBoolean("add", true);
                Log.d(TAG, "onCompleteAddGroup: Add result is true.");
            }
        });
        return resultRealtime && resultFirestore;
    }


    //update specific group in DB's.
    public void updateGroupInDB(Post post) {
        //update fire store values that can be changed --> after group has created.
        //can be changed values:
        //end time,users in group+count , uid of manager,description,title,,email of manager,min/max people, location, url,group price.
        referenceAllGroups.document(post.getGroupId()).update("endTime", post.getEndTime(),
                "users", post.getUsers(), "usersCount", post.getUsersCount(), "uid"
                , post.getUid(), "description", post.getDescription(), "title", post.getTitle(), "email", post.getEmail(),
                "minPeople", post.getMinPeople(), "maxPeople", post.getMaxPeople(), "location"
                , post.getLocation(), "groupPrice", post.getGroupPrice(), "originalPrice", post.getOriginalPrice(), "url", post.getUrl()
                , "groupStarted", post.isGroupStarted(), "postImageUri", post.getPostImageUri(), "shipmentMethod", post.getShipmentMethod()).addOnCompleteListener(task -> {
            Log.d(TAG, "onComplete: updated list from repo.");
            if (task.isSuccessful()) {
                bundleResult.putBoolean("update", true);
            } else {
                bundleResult.putBoolean("update", false);
            }
        });
    }

    public void groupToPower(Post post) {
        referenceAllGroups.document(post.getGroupId()).update("endTime", null,
                "users", post.getUsers(), "usersCount", post.getUsersCount(), "uid"
                , null, "description", post.getDescription(), "title", post.getTitle(), "email", null,
                "minPeople", null, "maxPeople", null, "location"
                , null, "groupPrice", null, "originalPrice", null
                , "url", null, "groupManager", null, "groupStarted", false, "shipmentMethod", "גם וגם").addOnCompleteListener(task -> {
            Log.d(TAG, "onComplete: updated list from repo.");
            bundleResult.putBoolean("update", true);
        });
    }

    public void deleteGroupInDB(Post post) {

        //remove from firestore db.
        referenceAllGroups.document(post.getGroupId()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        bundleResult.putBoolean("delete", true);
                        Log.d(TAG, "onCompleteDeleteGroup: Delete result is True.");
                    }
                });
    }

    // copies the group to archive and deletes it from all groups.
    public void archiveGroup(Post post) {
        referenceArchive.document(post.getGroupId()).set(post).addOnCompleteListener(task -> {
            deleteGroupInDB(post); // delete group from all groups directory in DB. .
            Log.d(TAG, "onComplete: archived groups");
        });
    }


    //interface
    public interface OnPostsInterface {
        void setPostList(List<Post> postList);

        void setFilteredPostList(List<Post> filteredPostList);

    }
}
