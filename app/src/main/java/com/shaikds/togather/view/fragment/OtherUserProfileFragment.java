package com.shaikds.togather.view.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shaikds.togather.R;
import com.shaikds.togather.adapters.ProfilePagerAdapter;
import com.shaikds.togather.adapters.ProfilesAdapter;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.model.User;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class OtherUserProfileFragment extends Fragment implements ProfilesAdapter.OnClickedPost {
    private static final String TAG = "OtherUserProfileFrag";
    TextView tvTitle, tvProfession, tvCompany, tvDescription, tvWebsite, tvStoreCount, tvLikesCount, tvGroupsCount;
    Button btnFollow;
    ImageView ivProfile;
    String currentUID;

    MainSearchPostsViewModel postsViewModel;
    SearchUsersViewModel usersViewModel;
    Bundle mBundle;
    String uid;
    NavController navController;
    BottomNavigationView bottomMenu;
    private ProfilePagerAdapter adapterViewPager;
    private ViewPager2 vpPager;


    public OtherUserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_other_user_profile, container, false);
        findViewsById(v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomMenu = getActivity().findViewById(R.id.bottom_nav);
        bottomMenu.setVisibility(View.GONE);

        currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        navController = Navigation.findNavController(view);
        onBackPressed();

        mBundle = this.getArguments();
        if (mBundle == null) {
            navController.navigate(R.id.action_otherUserProfileFragment_to_searchFragment);
        } else {
            uid = mBundle.getString("uid");
            mBundle.putString("userType", "notRegistered");
        }

        if (uid.equals(currentUID)) { // same user  -- > send to profile fragment.
            navController.navigate(R.id.action_otherUserProfileFragment_to_profileFragment);
            bottomMenu.setSelectedItemId(R.id.profile_bottom);
        }
        usersViewModel = new ViewModelProvider(requireActivity()).get(SearchUsersViewModel.class);
        User specificUser = usersViewModel.getSpecificUser(uid);
        if (specificUser != null) {
            userInfo(specificUser);
        } else {
            navController.navigate(R.id.action_otherUserProfileFragment_to_searchFragment);
            Toast.makeText(getContext(), "משתמש לא נבחר", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onViewCreated: No UID of specific user");
        }

        TabLayout tabLayout = view.findViewById(R.id.other_user_tab_layout);
        vpPager = view.findViewById(R.id.other_user_vp2);
        adapterViewPager = new ProfilePagerAdapter(getChildFragmentManager(), getLifecycle(), view, uid);
        vpPager.setAdapter(adapterViewPager);

        new TabLayoutMediator(tabLayout, vpPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("חנות");
                    } else if (position == 1) {
                        tab.setText("קבוצות");
                    }
                }
        ).attach();

        postsViewModel = new ViewModelProvider(getActivity()).get(MainSearchPostsViewModel.class);
        final List<Post> allUserPosts = new ArrayList<>();
        List<Post> dinamicGroupsList = new ArrayList<>();
        List<Post> posts = postsViewModel.getAllPosts().getValue();
        //store count.
        dinamicGroupsList.clear();
        allUserPosts.clear();
        allUserPosts.addAll(posts);
        dinamicGroupsList = postsViewModel.getGroupsByManager(allUserPosts, uid); // send other list and not the original posts -->cause it will change other post arrays.
        tvStoreCount.setText(MessageFormat.format("{0} למכירה", dinamicGroupsList.size()));
        // now for groups count.
        dinamicGroupsList.clear();
        allUserPosts.clear();
        allUserPosts.addAll(posts);
        dinamicGroupsList = postsViewModel.getGroupsByMember(allUserPosts, uid);
        tvGroupsCount.setText(MessageFormat.format("{0} קבוצות", dinamicGroupsList.size()));
        // followers count.
//        tvLikesCount.setText("0 עוקבים "); //TODO
    }

    private void findViewsById(View view) {
        tvCompany = view.findViewById(R.id.tv_other_profile_company);
        tvDescription = view.findViewById(R.id.tv_other_profile_desc_tv);
        tvLikesCount = view.findViewById(R.id.tv_profile_followers);
        tvStoreCount = view.findViewById(R.id.tv_other_profile_posts_number);
        tvProfession = view.findViewById(R.id.other_profile_tv_profession);
        tvTitle = view.findViewById(R.id.other_profile_tv_title);
        tvWebsite = view.findViewById(R.id.tv_other_profile_website);
        btnFollow = view.findViewById(R.id.ib_other_profile_follow);
        ivProfile = view.findViewById(R.id.other_profile_iv);
        tvGroupsCount = view.findViewById(R.id.tv_other_profile_groups_counter);
    }

    private void userInfo(User user) {
        tvTitle.setText(user.getName());
        tvWebsite.setText(user.getWeb());
        tvDescription.setText(user.getBio());
        tvProfession.setText(user.getProf());
        //tvLikesCount.setText();
        //tvPostsCount.setText();
        try {
            Picasso.get().load(user.getUrl()).into(ivProfile);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClickedPost(List<Post> postsList, int position) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bottomMenu.setVisibility(View.VISIBLE);
    }

    private void onBackPressed() {

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                bottomMenu.setSelectedItemId(R.id.profile_bottom);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }
}

