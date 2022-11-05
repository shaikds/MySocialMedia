package com.shaikds.togather.view.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.shaikds.togather.model.Common;
import com.shaikds.togather.R;
import com.shaikds.togather.adapters.SearchGroupsAdapter;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.model.User;
import com.shaikds.togather.adapters.UsersAdapter;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment implements SearchGroupsAdapter.IsGroupClicked {
    final static String TAG = "UsersFragment";
    private Common common;
    RadioGroup radioGroup;
    RadioButton radioButton;
    RecyclerView rvUsersList, rvGroupsList;
    UsersAdapter adapter;
    SearchGroupsAdapter groupsAdapter;
    List<User> userList;
    List<Post> postsList;
    Bundle bundle;
    SearchView sv;
    MainSearchPostsViewModel postsViewModel;
    SearchUsersViewModel usersViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_users, container, false);
        common = new Common(getContext());
        postsViewModel = new ViewModelProvider(getActivity()).get(MainSearchPostsViewModel.class);
        usersViewModel = new ViewModelProvider(getActivity()).get(SearchUsersViewModel.class);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        //init users list
        postsList = new ArrayList<>();
        userList = new ArrayList<>();
        //init the rest of things
        sv = getView().findViewById(R.id.search_view_users);



        //init the recycler view
        rvGroupsList = getView().findViewById(R.id.search_users_rv_groups);
        //set it's properties
        rvGroupsList.setHasFixedSize(true);
        rvGroupsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim())) {
                    //search text contains text,search it .
                        // show only groups.
                        searchGroups(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called whenever user press any single letter
                //if search query is not empty then search
                if (newText != null) {
                    if (!TextUtils.isEmpty(newText.trim())) {
                        //search text contains text,search it .
                        // show only groups.
                        searchGroups(newText);
                    }
                }
                return false;
            }

        });

        //check deep link redirection ->
        Bundle deepLinkBundle = getArguments();
        try {
        if (deepLinkBundle.getString("group_title_deep_link") != null ){
            sv.setQuery(deepLinkBundle.getString("group_title_deep_link",""),true);
        }
        }catch (NullPointerException e ){
            Log.d(TAG, "onStart: No deep link found --> moving on");
        }
    }

    private void searchUsers(String query) {
        //get current user
        userList.clear();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        for (User user : usersViewModel.getUsersLiveData().getValue()) {
            if (!user.getUid().equals(firebaseUser.getUid())) {
                if (user.getName().contains(query)) {
                    userList.add(user);
                }
            }
        }
        //adapter
        //adapter = new UsersAdapter(getActivity(), userList, UsersFragment.this);
        //refresh adapter
        adapter.notifyDataSetChanged();
        //set adapter to recycler view
        rvUsersList.setAdapter(adapter);



    }


    private void searchGroups(String query) {
        postsList.clear();
        for (Post post : postsViewModel.getOriginalPosts()) {
            if (post.getTitle().contains(query)) {
                // add it to post list
                postsList.add(post);
            }
        }
        //adapter
        groupsAdapter = new SearchGroupsAdapter(UsersFragment.this);
        //refresh adapter
        groupsAdapter.setPostList(postsList);
        //set adapter to recycler view
        rvGroupsList.setAdapter(groupsAdapter);
    }

/*    // go to other user profile when searched user clicked.
    @Override
    public void userClicked(int position, User user) {
        if(bundle == null ){
            bundle  = new Bundle();
        }
        bundle.putString("uid", user.getUid());
        postsViewModel.getNavController().navigate(R.id.otherUserProfileFragment, bundle);
    }*/

    // go to group view or join group view frags.
    @Override
    public void groupClicked(int position, List<Post> postList) {
        final Post post = postList.get(position);
        postsViewModel.select(post);
        bundle = common.checkUserType(post);
        bundle.putString("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        //if uid is already inside group. check if manager or not --> put data in bundle.
        if (post.getUsers().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            postsViewModel.getNavController().navigate(R.id.fragmentGroupView, bundle);
            Toast.makeText(getActivity(), "את/ה כבר חלק מן הקבוצה הזו", Toast.LENGTH_SHORT).show();
        } else {
            FragmentManager fragmentManager = this.getParentFragmentManager();
            FragmentPreviewGroupView preview = new FragmentPreviewGroupView(position, post);
            preview.setArguments(bundle);
            preview.show(fragmentManager, "register to group");
        }
    }
}