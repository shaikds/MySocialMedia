package com.shaikds.togather.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shaikds.togather.R;
import com.shaikds.togather.adapters.ProfilesAdapter;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class FragmentProfileStorePage extends Fragment implements ProfilesAdapter.OnClickedPost {
    private static final String TAG = "FragProfileStorePage";
    private String title;
    private int page;
    MainSearchPostsViewModel postsViewModel;
    String uid;
    ProfilesAdapter postsAdapter;
    RecyclerView rvStoreGroups;
    List<Post> allUserPosts = new ArrayList<>();
    List<Post> allMemberGroups = new ArrayList<>();
    static Bundle mBundle;
    NavController navController;

    public FragmentProfileStorePage() {
        // Required empty public constructor

    }

    public FragmentProfileStorePage(int page, String title, String uid) {
        mBundle = new Bundle();
        mBundle.putString("profilePageTitle", title);
        mBundle.putInt("profilePageId", page);
        this.setArguments(mBundle);
        this.uid = uid;
    }


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("profilePageId", 0);
        title = getArguments().getString("profilePageTitle");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile_store_page, container, false);
        rvStoreGroups = v.findViewById(R.id.rv_profile_store_page);
        page = getArguments().getInt("profilePageId", 0);
        title = getArguments().getString("profilePageTitle");
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvStoreGroups.setLayoutManager(new GridLayoutManager(getContext(), 3));
        postsAdapter = new ProfilesAdapter(requireContext(), this);
        postsViewModel = new ViewModelProvider(requireActivity()).get(MainSearchPostsViewModel.class);
        navController = postsViewModel.getNavController();
        postsViewModel.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
            allUserPosts.clear();
            allMemberGroups.clear();
            allMemberGroups.addAll(postsViewModel.getOriginalPosts());
            allUserPosts = postsViewModel.getGroupsByManager(allMemberGroups, uid); // send other list and not the original posts -->cause it will change other post arrays.
            if (allUserPosts != null) {
/*                while (posts.size() < allUserPosts.size()) {
                    allUserPosts.remove(0);
                }*/
                postsAdapter.setPostList(allUserPosts);
                rvStoreGroups.setAdapter(postsAdapter);
            }

        });

    }

    @Override
    public void onClickedPost(List<Post> postsList, int position) {
        //check if he's a member or manager by uid with the post .
        postsViewModel.select(postsList.get(position));
        if (postsList.get(position).getUsers().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) { // user is inside group?
            checkUserType(postsList.get(position));

        } else {     // Not a member and not the manager but he's a logged user, so PREVIEW .
            FragmentManager fragmentManager = this.getParentFragmentManager();
            FragmentPreviewGroupView preview = new FragmentPreviewGroupView(position, postsList.get(position));
            preview.show(fragmentManager, "register to group");
        }
    }

    /* put data in bundle*/
    void checkUserType(Post post) {
        mBundle = new Bundle();
        try {
            if (post.getUid().equals(uid)) { //manager .
                mBundle.putString("userType", "manager");

            } else if (post.getUsers().contains(uid)) {// normal user .
                mBundle.putString("userType", "user");

            } else { // not registered .
                mBundle.putString("userType", "notRegistered");
            }
        } catch (NullPointerException e) { // the group has no manager.
            e.printStackTrace();
            if (post.getUsers().contains(uid)) {// normal user
                mBundle.putString("userType", "user");
            } else {
                mBundle.putString("userType", "notRegistered");
            }

        }
        navController.navigate(R.id.action_global_fragment_group_view, mBundle);

    }

}