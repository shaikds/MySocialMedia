package com.shaikds.togather.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.shaikds.togather.adapters.ProfilePagerAdapter;
import com.shaikds.togather.view.activity.CreateProfile;
import com.shaikds.togather.view.activity.ImageActivity;
import com.shaikds.togather.view.activity.LoginActivity;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.R;
import com.shaikds.togather.view.activity.UpdateProfile;
import com.shaikds.togather.model.User;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private ImageView ivProfile;
    private TextView tvName, tvProfession, tvBio, tvWeb, tvCompany, tvGroupCount, tvFollowers, tvStoreCount;
    private ImageButton ibEdit, ibMenu;
    private String uid;
    private ProfilePagerAdapter adapterViewPager;
    private ViewPager2 vpPager;
    private static String TAG = "ProfileFragment";
    private MainSearchPostsViewModel postsViewModel;
    private SearchUsersViewModel usersViewModel;
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://mysocialmedia-eb38f-default-rtdb.firebaseio.com/");
    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;
    private Bundle mBundle;
    private NavController navController;
    private List<Post> dinamicGroupsList = new ArrayList<>();
    private List<Post> allUserPosts = new ArrayList<>();
    private User specificUser;

    //empty constructor
    public ProfileFragment() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
/*        if (firebaseUser.getPhoneNumber() == null) {
            sendUserToLoginActivity();
        }*/
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        firestore = FirebaseFirestore.getInstance();
        ivProfile = view.findViewById(R.id.profile_iv);
        tvName = view.findViewById(R.id.f1_tv_name);
        tvProfession = view.findViewById(R.id.tv_profile_profession);
        tvBio = view.findViewById(R.id.tv_profile_desc_tv);
        tvWeb = view.findViewById(R.id.tv_profile_website);
        tvCompany = view.findViewById(R.id.tv_profile_company);
        ibEdit = view.findViewById(R.id.f1_ib_edit);
        ibMenu = view.findViewById(R.id.f1_ib_menu);
        tvFollowers = view.findViewById(R.id.tv_profile_followers);
        tvGroupCount = view.findViewById(R.id.tv_profile_user_count_group);
        tvStoreCount = view.findViewById(R.id.tv_store_groups_f1);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        //if deep linked and registered : go straight to specific group view .

        if (mBundle == null) {
            mBundle = new Bundle();
        } else {
            mBundle = getArguments();
        }

        try {
            uid = firebaseUser.getUid();

        } catch (NullPointerException e) {
            sendUserToLoginActivity();
            Toast.makeText(getActivity(), "משתמש לא רשום", Toast.LENGTH_SHORT).show();
        }

        //view pager of store and my groups.
        TabLayout tabLayout = view.findViewById(R.id.profile_frag_tab_layout);
        vpPager = view.findViewById(R.id.vpPager);
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

        //get user data for and stack it to ui .
        usersViewModel = new ViewModelProvider(requireActivity()).get(SearchUsersViewModel.class);
        usersViewModel.getUsersLiveData().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users != null) {
                    specificUser = usersViewModel.getSpecificUser(uid); // current user .
                    if (specificUser == null) {
                        // if no user in db -- > go to create profile activity.(new user)
                        // if no user in db -- > go to create profile activity.(new user)
                        Intent intent = new Intent(getActivity(), CreateProfile.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                    userInfo(specificUser); // attach to ui the user data.
                } else {
                    Toast.makeText(requireContext(), "בעיה בהורדת רשימת משתמשים", Toast.LENGTH_SHORT).show();
                }
            }
        });

        postsViewModel = new ViewModelProvider(requireActivity()).get(MainSearchPostsViewModel.class);
        postsViewModel.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
            //store count.
            dinamicGroupsList.clear();
            allUserPosts.clear();
            allUserPosts.addAll(postsViewModel.getOriginalPosts());
            dinamicGroupsList = postsViewModel.getGroupsByManager(allUserPosts, uid); // send other list and not the original posts -->cause it will change other post arrays.
            tvStoreCount.setText(MessageFormat.format("{0} למכירה", dinamicGroupsList.size()));
            // now for groups count.
            dinamicGroupsList.clear();
            allUserPosts.clear();
            allUserPosts.addAll(postsViewModel.getOriginalPosts());
            dinamicGroupsList = postsViewModel.getGroupsByMember(allUserPosts, uid);
            tvGroupCount.setText(MessageFormat.format("{0} קבוצות", dinamicGroupsList.size()));
            // followers count.
            tvFollowers.setText("0 עוקבים "); //TODO
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ibEdit.setOnClickListener(this);
        ibMenu.setOnClickListener(this);
        ivProfile.setOnClickListener(this);
        tvWeb.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.f1_ib_edit:
                Intent intent = new Intent(getActivity(), UpdateProfile.class);
                intent.putExtra("mPass", specificUser.getPassword());
                startActivity(intent);
                break;
            case R.id.f1_ib_menu:
                BottomSheetMenu bottomSheetMenu = new BottomSheetMenu();
                mBundle.putString("store", tvStoreCount.getText().toString().replace(" למכירה", ""));
                mBundle.putString("groups", tvGroupCount.getText().toString().replace(" קבוצות", ""));

                bottomSheetMenu.setArguments(mBundle);
                bottomSheetMenu.show(getChildFragmentManager(), "bottomsheet");
                break;
            case R.id.profile_iv:
                Intent intent1 = new Intent(getActivity(), ImageActivity.class);
                intent1.putExtra("user", (Serializable) specificUser);
                startActivity(intent1);

            case R.id.tv_profile_website:
                try {
                    String url = tvWeb.getText().toString();
                    Intent intent2 = new Intent(Intent.ACTION_VIEW);
                    intent2.setData(Uri.parse(url));
                    startActivity(intent2);

                } catch (Exception e) {
                    Toast.makeText(getActivity(), "קישור לא תקין", Toast.LENGTH_SHORT).show();
                }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseUser == (null)) {
            sendUserToLoginActivity();
        }
    }

    //Methods:
    //-------//
    private void sendUserToLoginActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();

    }

    private void userInfo(User user) {
        try {
            tvName.setText(user.getName());
            tvWeb.setText(user.getWeb());
            tvBio.setText(user.getBio());
            tvCompany.setText(user.getCompany());
            tvProfession.setText(user.getProf());
            Picasso.get().load(user.getUrl()).into(ivProfile);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
