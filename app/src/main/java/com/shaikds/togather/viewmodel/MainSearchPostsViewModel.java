package com.shaikds.togather.viewmodel;

import android.app.Application;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;

import com.shaikds.togather.R;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.model.Router;
import com.shaikds.togather.repository.GroupRepo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainSearchPostsViewModel extends AndroidViewModel implements GroupRepo.OnPostsInterface {

    private List<Post> originalPosts = new ArrayList<>();
    //Variables
    private final MutableLiveData<List<Post>> postsLiveData;
    private final MutableLiveData<List<Post>> filteredPostsLiveData;
    List<Post> filteredPosts = new ArrayList<>();
    GroupRepo groupRepo = new GroupRepo(this);
    private final String TAG = "MainSearchViewModel";
    private final MutableLiveData<Post> currentGroup = new MutableLiveData<>();
    private Router router;
    private int index;
    private int storeCount, groupCount;


    //Constructor
    public MainSearchPostsViewModel(@NonNull Application application) {
        super(application);
        postsLiveData = new MutableLiveData<>();
        filteredPostsLiveData = new MutableLiveData<>();
        archiveOutDatedPosts();
        groupRepo.getAllPosts();
    }

    //Setters

    public void setOriginalPosts(List<Post> originalPosts) {
        this.originalPosts = originalPosts;
    }

    //Getters
    public List<Post> getOriginalPosts() {
        return originalPosts;
    }


    //update All posts live data
    public MutableLiveData<List<Post>> getAllPosts() {
        return postsLiveData;
    }


    //Fragment Communication Through View Model.
    // choose a group between fragments
    public void select(Post post) {
        currentGroup.setValue(post);
    }

    //get the selected group in another fragment.
    public LiveData<Post> getSelectedGroup() {
        return currentGroup;
    }

    // remove groups & copy to archive_groups in db  --> if today 5/8 will work on 4/8>=x
    private void archiveOutDatedPosts() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        final Date todayDate = Calendar.getInstance().getTime();
        final String sDate = dateFormat.format(todayDate);
        if (postsLiveData.getValue() != null) {
            for (Post mPost : postsLiveData.getValue()) {
                if (mPost.getEndTime() != null) {
                    try {
                        if (todayDate.after(dateFormat.parse(mPost.getEndTime())) && !sDate.equals(mPost.getEndTime())) { // today date is after and not equal to end date of group .
                            /*if (mPost.getEndTime().equals(savedDate))*/  // if the date today equals to the end date -- > remove group from list and db.
                            groupRepo.archiveGroup(mPost);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
//            groupRepo.getAllPosts();

        }
    }

    //Filtered posts live data
    public MutableLiveData<List<Post>> getFilteredPosts(String categoryName) {
        filteredPosts.clear();
        groupRepo.getFilteredPosts(categoryName);
        //Same live data for both filtered and all Posts.
        return postsLiveData;
    }


    // ----_CRUD_------

    public void createGroup(Post post) {
        groupRepo.addGroupToDB(post);
    }

    //adding user to group in db and UPDATE the viewmodel.
    public void addUserToGroup(int position, String uid) {
        postsLiveData.getValue().get(position).addUser(uid);
        groupRepo.updateGroupInDB(postsLiveData.getValue().get(position));
    }

    //remove user from group.
    public void removeUserFromGroup(Post post, String uid) {
        final Post postCopy = post;
        if (post.getUsersCount() == 1) { // delete the group if no users .
            groupRepo.deleteGroupInDB(post);
            return;
        }
        int position = 0;
        for (Post mPost : postsLiveData.getValue()) {
            if (mPost.equals(post)) {  // break condition
                postCopy.removeUser(uid);
                if (mPost.getUid() == null) {
                    //POWER GROUP --> just delete him.
                    groupRepo.updateGroupInDB(postCopy);
                    return;
                }
                if (mPost.getUid().equals(uid)) { // if it's a manager -> make it a power group again, make all fields that changes -> null.
                    //updateCheckFields(post, uid);
                    updateCheckFields(postCopy, uid);
                    groupRepo.groupToPower(postCopy);
                }// if it's not a manager -> just break and update the group
                //postsLiveData.getValue().get(position).removeUser(uid); // w/ position.
                groupRepo.updateGroupInDB(postCopy);
                break; // got the position we need for live data
            }
            position++;
        }
    }

    //update post in list
    public void updatePostsList(Post post) {
        groupRepo.updateGroupInDB(post);
    }

    //delete specific group in db and postviewmodel.
    public void deleteGroupFromList(Post post) {
        if (!(post == null)) {
            groupRepo.deleteGroupInDB(post);
        }
    }

    // Nav Controller.
    public NavController setGetNavController(View view, NavController navController) {
        router = new Router(view, navController);
        return router.getNavController();
    }

    public NavController getNavController() {
        return router.getNavController();
    }

    //get all current user store groups(where he is manager)
    public List<Post> getGroupsByManager(List<Post> postsList, String uid) {
        List<Post> currentList = postsList;
        if (postsList != null) {
            for (Iterator<Post> iterator = currentList.iterator(); iterator.hasNext(); ) {
                Post post = iterator.next();
                if (post.getUid() == null) { // no uid in group --> not relevant.
                    iterator.remove();
                } else if (post.getUid().equals("")) {//same but diff
                    iterator.remove();

                } else if (!uid.equals(post.getUid())) {// all groups i am NOT manager.
                    iterator.remove();

                }
            }
            setStoreCount(currentList.size());
        }
        return currentList;
    }

    //get all current user groups he pariticipating.(where he's NOT a manager.)
    public List<Post> getGroupsByMember(List<Post> postsList, String uid) {
        List<Post> currentList = new ArrayList<>();
        if (postsList != null) {
            for (Post post : postsList) {
                try {
                    post.getUid(); // null exception check.
                    if (!(post.getUid().equals(uid))) {// this user is not manager --> next we check if he's a member.
                        if (post.checkIfMember(uid)) { // group member list contains current user uid--> add to list
                            currentList.add(post);
                        }
                    }

                } catch (NullPointerException e) { // power group
                    if (post.checkIfMember(uid)) { // group member list contains current user uid--> add to list
                        currentList.add(post);
                    }
                }
            }
            setGroupsCount(currentList.size());
        }
        return currentList;
    }


    private void setGroupsCount(int a) {
        this.storeCount = a;
    }

    private void setStoreCount(int a) {
        this.groupCount = a;

    }

    // all out dated posts --> archive it .
    public void archiveGroup(Post post) {
        groupRepo.archiveGroup(post);
    }

    // filter posts by location

    public ArrayList<Post> filterByLocation(ArrayList<String> locationsList) {
        filteredPosts.clear();
        if (postsLiveData.getValue() != null) {
            final List<Post> postsList = postsLiveData.getValue();
            for (Post post : postsList) {
                if (locationsList.contains(post.getLocation())) {
                    filteredPosts.add(post);
                }
            }
        }
        return (ArrayList<Post>) filteredPosts;
    }

    public List<Post> getPowerGroupsOnly(List<Post> postList) {
        List<Post> posts = new ArrayList<>();
        // can be not null if location is filtered first by search frag.
        filteredPosts = new ArrayList<>();
        filteredPosts.addAll(postList);
        for (Post post : filteredPosts) {
            if (post.getUid() != null) {
                posts.add(post);
            }
        }
        filteredPosts.removeAll(posts);
        return filteredPosts;
    }

    public List<Post> getGroupsNoPower(List<Post> postList) {
        List<Post> posts = new ArrayList<>();
        filteredPosts = new ArrayList<>();
        filteredPosts.addAll(postList);
        for (Post post : filteredPosts) {
            if (post.getUid() == (null)) {
                posts.add(post);
            }
        }
        filteredPosts.removeAll(posts);
        return filteredPosts;
    }

    private void updateCheckFields(Post post, String uid) {
        // all fields needs to be changed to become power group again.
        post.removeUser(uid);
        post.setMaxPeople(null);
        post.setMinPeople(null);
        post.setEndTime(null);
        post.setUid(null);
        post.setEmail(null);
        post.setOriginalPrice(null);
        post.setEmail(null);
        post.setUrl(null);
        post.setShipmentMethod(getApplication().getString(R.string.shipment_method_all));
    }

    public Post getGroupById(String groupId) {
        index = 0;
        if (postsLiveData.getValue() != null) {
            for (Post post : postsLiveData.getValue()) {
                index += 1;
                if (post.getGroupId() != null) {
                    if (post.getGroupId().equals(groupId)) {
                        // post id is same like string we got.
                        return post;
                    }
                }
            }
        }
        return null;
    }


    public int getIndex() {
        return index;
    }

    // Repo Interface Implement //
    // ----------------------- //
    //update live data
    @Override
    public void setPostList(List<Post> posts) {
        if (posts != null) {
            postsLiveData.postValue(posts);
        }
        Log.d(TAG, "setPostList: New List Arrived ");
    }

    @Override
    public void setFilteredPostList(List<Post> filteredPostList) {
        postsLiveData.postValue(filteredPostList);
        Log.d(TAG, "setFilteredPostList: New Filtered List Arrived");
    }


}
