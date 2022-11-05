package com.shaikds.togather.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shaikds.togather.R;
import com.shaikds.togather.adapters.SearchAdapter;
import com.shaikds.togather.adapters.SearchCategoriesAdapter;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.model.Tours;
import com.shaikds.togather.model.User;
import com.shaikds.togather.view.activity.LoginActivity;
import com.shaikds.togather.viewmodel.AuthViewModel;
import com.shaikds.togather.viewmodel.CategoriesViewModel;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements SearchCategoriesAdapter.OnClickedCategory, SearchAdapter.handleGroupClick, SearchAdapter.onRvFinishedCallback {

    public ImageButton btnSearchUser, btnFilter, btnReplayGuide;
    public RecyclerView rvCategories;
    private static final String TAG = "SearchFragment";
    SearchCategoriesAdapter adapterCategories;
    SearchAdapter adapterSearchMain;
    Bundle bundleUserType;
    ConstraintLayout emptyLayout;
    AuthViewModel authViewModel;

    MainSearchPostsViewModel viewModelMain;
    CategoriesViewModel viewModelCategories;
    SearchUsersViewModel usersViewModel;
    RecyclerView rvSearch;
    NavController navController;

    FirebaseUser firebaseUser;


    public SearchFragment() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_fragment, container, false);
        authViewModel = new ViewModelProvider(getActivity()).get(AuthViewModel.class);
        //TODO: OBSERVE THE VIEW MODEL LOGGED STATUS .
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        checkUserStatus(); // send user to login if not logged in


        //Category Buttons Recycler View//
        //-----------------------------//
        rvCategories = getView().findViewById(R.id.search_category_rv);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // Main Search Recycler View
        //-----------------------------//
        rvSearch = getView().findViewById(R.id.rv_main_search);
        rvSearch.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        btnSearchUser = getView().findViewById(R.id.btn_search_f2);
        btnReplayGuide = getView().findViewById(R.id.search_ib_replay);


        btnReplayGuide.setOnClickListener(view -> {
            final Tours tours = new Tours(SearchFragment.this);
            tours.askUserShowTour(adapterSearchMain);
        });
        //btn search when clicked send to search users fragment
        btnSearchUser.setOnClickListener(v -> {
            navController.navigate(R.id.action_searchFragment_to_usersFragment);
        });


        //---------------------//
        rvSearch.setAdapter(adapterSearchMain);
        rvCategories.setAdapter(adapterCategories);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnFilter = view.findViewById(R.id.search_filter_imb);
        navController = Navigation.findNavController(view);
        emptyLayout = view.findViewById(R.id.search_empty);
        adapterSearchMain = new SearchAdapter(SearchFragment.this, this);

        try {
            bundleUserType = getArguments();
        } catch (Exception e) {
            bundleUserType = new Bundle();
        }

        btnFilter.setOnClickListener(v1 -> {
            navController.navigate(R.id.action_searchFragment_to_settingsFragment);
        });

        //user for adapter
        usersViewModel = new ViewModelProvider(requireActivity()).get(SearchUsersViewModel.class);
        checkUserStatus();
        User currentUser = usersViewModel.getSpecificUser(firebaseUser.getUid());
        usersViewModel.myUser(currentUser);
        usersViewModel.getUsersLiveData().observe(getViewLifecycleOwner(), userList -> {
            try {
                if (userList.size() > 0) {
                    adapterSearchMain.setUsersList(userList);
                }
            } catch (Exception e) {
                Log.d(TAG, "onChangedUsersViewModel: " + e.getMessage());
            }
        });


        adapterCategories = new SearchCategoriesAdapter(this);

        viewModelCategories = new ViewModelProvider(requireActivity()).get(CategoriesViewModel.class);
        viewModelCategories.gettAllCategoriesName().observe(getViewLifecycleOwner(), categories -> {
            adapterCategories.setCategoriesList(categories);
            rvCategories.setAdapter(adapterCategories);

        });


        viewModelMain = new ViewModelProvider(requireActivity()).get(MainSearchPostsViewModel.class);
        viewModelMain.setGetNavController(view, navController);
        try {
            viewModelMain.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
                if (bundleUserType != null) { // check filter first(from settings fragment.)
                    if (bundleUserType.getStringArrayList("cityArray") != null ||
                            bundleUserType.getString("settingsFilter") != null) {
                        checkSettingsFilter(posts);
                        return; //MUST
                    }
                }
                if (posts.size() == 0) {
                    rvSearch.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                } else {
                    if (viewModelMain.getOriginalPosts() == null|| viewModelMain.getOriginalPosts().size()==0) {
                        viewModelMain.setOriginalPosts(posts);
                    }
                    emptyLayout.setVisibility(View.GONE);
                    rvSearch.setVisibility(View.VISIBLE);
                    adapterSearchMain.setPostsList(posts);
                    rvSearch.setAdapter(null);
                    rvSearch.setAdapter(adapterSearchMain);
                }

            });
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), "שגיאה בהורדת הקבוצות, בדקו את חיבור האינטרנט", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "postsViewModel: " + e.getMessage());

        }
    }

    @Override
    public void onCategoryClicked(List<String> categoriesList, int position) {
        // change to filtered posts by category name.
        Log.d(TAG, "onCategoryClicked: " + categoriesList.get(position));
        List<Post> filteredPosts = viewModelMain.getFilteredPosts(categoriesList.get(position)).getValue();
        //early exit
        if (filteredPosts == null) {
            return;
        }
        if (filteredPosts.size() > 0) {
            emptyLayout.setVisibility(View.GONE);
            adapterSearchMain.setPostsList(filteredPosts);
            rvSearch.setAdapter(adapterSearchMain);
        }
    }


    @Override
    public void groupTitleClicked(int position, Post post) {
        //check if he's a member or manager by uid with the post .
        viewModelMain.select(post);
        checkUserStatus();
        if (post.getUsers().contains(firebaseUser.getUid())) { // user is inside group?
            checkUserType(post);
            navController.navigate(R.id.action_searchFragment_to_fragmentGroupView, bundleUserType);

        } else {     // Not a member and not the manager but he's a logged user, so PREVIEW .
            FragmentManager fragmentManager = this.getParentFragmentManager();
            FragmentPreviewGroupView preview = new FragmentPreviewGroupView(position, post);
            preview.setArguments(bundleUserType);
            preview.show(fragmentManager, "register to group");
        }
    }


    @Override
    public void groupJoinClicked(int position, Post post) {
        if (post.getUsers().contains(firebaseUser.getUid())) { //if uid is already inside group. check if manager or not --> put data in bundle.
            Toast.makeText(getActivity(), "את/ה כבר חלק מן הקבוצה הזו", Toast.LENGTH_SHORT).show();
        } else {
            FragmentManager fragmentManager = this.getParentFragmentManager();
            FragmentPreviewGroupView preview = new FragmentPreviewGroupView(position, post);
            preview.setArguments(bundleUserType);
            preview.show(fragmentManager, "register to group");
        }
    }

    @Override
    public void groupLikeClicked(SearchAdapter.PostViewHolder holder, int groupLikesCount, Post post) {
        try {
            if (!holder.isLiked) {
                usersViewModel.getMyUser().getGroupLikes().remove(post.getGroupId());// add groupId to user like list.
            } else {
                usersViewModel.getMyUser().getGroupLikes().add(post.getGroupId());// remove groupId to user like list.
            }
            usersViewModel.updateUser(usersViewModel.getMyUser()); // update in db .
        } catch (NullPointerException e) {
            // needs to complete create user activity.(go to profile will direct them to this activity.)
            navController.navigate(R.id.profileFragment);
        }
    }

    @Override
    public void groupOptionsClicked(int position, Post post) {
        //options --> share.
        //if uid is same like in post, so show button delete.
        viewModelMain.select(post);
        checkUserType(post);
        try {
            bundleUserType.putBoolean("isPurchaseStarted", post.isGroupStarted());
        } catch (NullPointerException ignored) {
            Log.d(TAG, "groupOptionsClicked: group purchasing isnt started.");
        }
        BottomSheetMenu bottomSheetMenu = new BottomSheetMenu();
        bottomSheetMenu.setArguments(bundleUserType);
        bottomSheetMenu.show(getParentFragmentManager(), "bottomsheet");
    }

    @Override
    public void sellerGroupClicked(int position, Post post) {
        if (post.getUid() != null) { // if there is uid --> there is a manager .
            bundleUserType = new Bundle();
            bundleUserType.putString("uid", post.getUid());
            navController.navigate(R.id.action_searchFragment_to_otherUserProfileFragment, bundleUserType);        //go to other user profile
        } else { // power group
            Toast.makeText(getActivity(), "לכח רכישה אין מנהל כרגע, ניתן להפוך למנהל לאחר הצטרפות לקבוצה.", Toast.LENGTH_SHORT).show();
        }
    }

    //------ METHODS ---------- //


    // --- GETTERS --- //

    public Bundle getBundleUserType() {
        return bundleUserType;
    }

    public MainSearchPostsViewModel getViewModelMain() {
        return viewModelMain;
    }


    //user is logged or not?
    private void checkUserStatus() {
        if (firebaseUser == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    /* put data in bundle*/
    public void checkUserType(Post post) {
        bundleUserType = new Bundle();
        try {
            if (post.getUid().equals(firebaseUser.getUid())) { //manager --> show btn edit and delete.
                bundleUserType.putString(getString(R.string.bundle_group_user_type), "manager");
            } else if (post.getUsers().contains(firebaseUser.getUid())) {// normal user --> dont show btn edit and delete.
                bundleUserType.putString("userType", "user");
            } else {
                bundleUserType.putString("userType", "notRegistered");

            }
        } catch (NullPointerException e) { // the group has no manager.
            e.printStackTrace();
            if (post.getUsers().contains(firebaseUser.getUid())) {// normal user
                bundleUserType.putString("userType", "user");
            } else {
                bundleUserType.putString("userType", "notRegistered");
            }

        }
    }

    // after coming back from settings filter fragment.
    private void checkSettingsFilter(List<Post> posts) {
        try {
            final String filter = bundleUserType.getString("settingsFilter", "");
            List<Post> finalList = new ArrayList<>();
            if (bundleUserType.getStringArrayList("cityArray") != null) {
                // cities chosen
                List<Post> postsList = viewModelMain.filterByLocation(bundleUserType.getStringArrayList("cityArray"));
                if (postsList.size() == 0) {// if filtered by location posts are 0 so return empty.
                    emptyLayout.setVisibility(View.VISIBLE);
                    rvSearch.setVisibility(View.GONE);
                    return;
                } else {
                    finalList = checkSettingsBundle(filter, postsList, finalList);
                }
            } else { // no cities chosen
                finalList = checkSettingsBundle(filter, posts, finalList);
            }
            if (finalList.size() == 0) {
                emptyLayout.setVisibility(View.VISIBLE);
                rvSearch.setVisibility(View.GONE);
                return;
            }// final list size is bigger than 0.
            emptyLayout.setVisibility(View.GONE);
            rvSearch.setVisibility(View.VISIBLE);
            adapterSearchMain.setPostsList(finalList);
            rvSearch.setAdapter(adapterSearchMain);

        } catch (NullPointerException e) {
            Log.d(TAG, "checkSettingsFilter: NO BUNDLE FOUND");
        }
    }

    private List<Post> checkSettingsBundle(String filter, List<Post> postsList, List<Post> finalList) {
        if (filter.equals("קבוצת רכישה בלבד")) {
            finalList = viewModelMain.getGroupsNoPower(postsList);
        } else if (filter.equals("כח רכישה בלבד")) {
            finalList = viewModelMain.getPowerGroupsOnly(postsList);
        } else { // ALL GROUPS WEVE GOT IN FINAL LIST UNTIL NOW
            finalList = postsList;
        }
        bundleUserType.putStringArrayList("cityArray", null);
        bundleUserType.putString("settingsFilter", null);
        return finalList;
    }

    @Override
    public void onRvFinished(@NonNull SearchAdapter.PostViewHolder viewHolder) {
        final Tours tours = new Tours(SearchFragment.this);
        tours.checkSearchShared(adapterSearchMain);
    }
}
