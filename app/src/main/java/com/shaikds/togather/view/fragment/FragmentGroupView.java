package com.shaikds.togather.view.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.shaikds.togather.R;
import com.shaikds.togather.adapters.GroupMembersAdapter;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.model.Tours;
import com.shaikds.togather.model.User;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class FragmentGroupView extends Fragment implements GroupMembersAdapter.OnClickedView {
    private static final String TAG = "FragmentGroupView";
    ScrollView sv;
    private ExpandableTextView tvDescription;
    public TextView tvPrice, tvGroupPrice, tvMaxPeople, tvMinPeople, tvEmail, tvUserName, tvTitle, tvDates, tvPeopleCount, tvPageTitle, tvShipmentMethod;
    public Button btnShare, btnLeave, btnMail, btnEdit, btnBeManager, btnPurchase;
    private ImageButton ibBack;
    private ImageView ivItem;
    private NavController navController;
    private Bundle bundleUserType;
    private RecyclerView rvUsers;
    private GroupMembersAdapter membersAdapter;
    private List<User> userList = new ArrayList<>();
    private Post currentPost;
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private MainSearchPostsViewModel postsViewModel;
    private SearchUsersViewModel usersViewModel;
    private Uri dynamicLink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_group_view, container, false);
        handleOnBackPressed();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        final String currentUID = firebaseUser.getUid();
        btnPurchase.setActivated(false);
        userList.clear();
        usersViewModel = new ViewModelProvider(requireActivity()).get(SearchUsersViewModel.class);
        postsViewModel = new ViewModelProvider(requireActivity()).get(MainSearchPostsViewModel.class);
        //update ui by current group selected in view model.
        currentPost = postsViewModel.getSelectedGroup().getValue();
        navController = Navigation.findNavController(view);
        // UI buttons visibility by user type
        bundleUserType = this.getArguments();
        if (bundleUserType == null) {   //if user is NOT manager OR registered user, navigate back to SearchFragment.
            navController.navigate(R.id.action_fragmentGroupView_to_searchFragment);
        } else { // bundle is not null.
            try {
                if (currentPost.getUid() != null) {  //purchase group
                    if (bundleUserType.getString("userType").equals("manager")) { // it's the manager.
                        btnEdit.setVisibility(View.VISIBLE);
                        btnMail.setVisibility(View.GONE);
                        btnBeManager.setVisibility(View.GONE);
                    } else if (bundleUserType.getString("userType").equals("user")) {// member inside purchase group.
                        btnBeManager.setVisibility(View.GONE);
                        btnMail.setVisibility(View.GONE);
                        btnEdit.setVisibility(View.GONE);
                    }
                } else {
                    btnBeManager.setVisibility(View.VISIBLE);
                    btnMail.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.GONE);
                }
            } catch (NullPointerException e) {// power group member
                btnBeManager.setVisibility(View.VISIBLE);
                btnMail.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
            }
        }

        // back button navigation .
        ibBack.setOnClickListener(v -> navController.navigate(R.id.action_fragmentGroupView_to_searchFragment));


        postsViewModel.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
            for (Post post : posts) {
                if (post.getGroupId().equals(currentPost.getGroupId())) {
                    postsViewModel.select(post);
                    currentPost = post;
                    updateUI(currentPost);
                    checkPurchaseStarted(currentPost ); // for purchase btn .
                    usersViewModel.setGroupUsers(currentPost); // sets the users in group by current post(RV).
                }
            }
        });

        // Users list recycler.
        membersAdapter = new GroupMembersAdapter(getContext(), this);
        List<String> usersUIDs = new ArrayList<>();
        usersUIDs.addAll(currentPost.getUsers());

        if (usersUIDs.contains(currentPost.getUid())) {
            usersUIDs.remove(currentPost.getUid()); // remove manager only from this list.
        }


        for (String usersUID : usersUIDs) {
            User specificUser = usersViewModel.getSpecificUser(usersUID);
            if (specificUser != null) { // only if the user isnt null --> save it.
                userList.add(specificUser);
            }
        }
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        membersAdapter.setUsers(userList);
        rvUsers.setAdapter(membersAdapter);


        //leave group
        btnLeave.setOnClickListener(v -> {
            leaveGroup(currentPost, postsViewModel, firebaseUser.getUid());
        });

        //be the manager --> Power groups ONLY .
        btnBeManager.setOnClickListener(v -> {
            navController.navigate(R.id.action_fragmentGroupView_to_fragmentBeManager, bundleUserType);
        });

        //send prepared mail to company
        btnMail.setOnClickListener(v -> composeEmail());

        //go to edit group fragment
        btnEdit.setOnClickListener(v -> {
            bundleUserType.putSerializable("specificUser", usersViewModel.getSpecificUser(currentUID));
            navController.navigate(R.id.action_fragmentGroupView_to_fragmentEditGroup, bundleUserType);
        });


        //share
        btnShare.setOnClickListener(v ->
        {
            final String postTitle = postsViewModel.getSelectedGroup().getValue().getTitle();
            Intent intent = new Intent(Intent.ACTION_SEND);
            //TODO: APP LINK IN MESSAGE HERE!!
            intent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.group_view_share_string), postTitle, postTitle, "\nLink To App\n\n"));
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "שיתוף קבוצת רכישה"));
        });

        btnPurchase.setOnClickListener(v -> {
            if (currentPost.getUid() == null) {
                Snackbar.make(getActivity(), getView(), "המתינו להצטרפות מנהל או שלחו הצעה לניהול הקבוצה בכדי שקבוצה זו תהפוך לקבוצת רכישה רשמית", BaseTransientBottomBar.LENGTH_SHORT).show();
            } else if (!btnPurchase.isActivated()) {
                final int minPeople = Integer.parseInt(currentPost.getMinPeople());
                final int x = minPeople + 1 - (currentPost.getUsersCount());
                Snackbar.make(getActivity(), getView(), "חסרים עוד " + x + " אנשים בקבוצה כדי שאהפוך לירוק ותוכלו לרכוש. שתפו את הקבוצה לחבריכם!", BaseTransientBottomBar.LENGTH_SHORT).show();
            } else {
                bundleUserType.putString("groupId", currentPost.getGroupId());
                bundleUserType.putString("managerUid", currentPost.getUid());
                navController.navigate(R.id.action_fragmentGroupView_to_fragmentPayment, bundleUserType);
            }
        });

        // making tour if first user
        Tours tours = new Tours(this);
        tours.checkGroupViewShared(sv);
    }


    // METHODS //
    //find views by id .
    private void findViews(View view) {
        tvShipmentMethod = view.findViewById(R.id.group_frag_shipment_tv);
        sv = view.findViewById(R.id.sv_group_view);
        tvPageTitle = view.findViewById(R.id.group_view_title);
        tvPeopleCount = view.findViewById(R.id.fragment_group_view_tv_counter);
        ivItem = view.findViewById(R.id.group_frag_post_iv);
        tvDescription = view.findViewById(R.id.group_frag_item_details).findViewById(R.id.group_frag_item_details);
        tvEmail = view.findViewById(R.id.group_frag_email_tv);
        tvDates = view.findViewById(R.id.group_frag_date_tv);
        tvGroupPrice = view.findViewById(R.id.group_frag_price_discount);
        tvPrice = view.findViewById(R.id.group_frag_price_original);
        tvMaxPeople = view.findViewById(R.id.group_frag_people_max_tv);
        tvMinPeople = view.findViewById(R.id.group_frag_people_min_tv);
        tvTitle = view.findViewById(R.id.group_frag_title_tv);
        tvUserName = view.findViewById(R.id.group_frag_name_tv);
        btnEdit = view.findViewById(R.id.group_frag_edit_btn);
        btnMail = view.findViewById(R.id.group_frag_send_mail);
        btnBeManager = view.findViewById(R.id.group_frag_request_manager);
        btnLeave = view.findViewById(R.id.group_frag_leave_group);
        btnShare = view.findViewById(R.id.group_frag_share_btn);
        rvUsers = view.findViewById(R.id.group_frag_rv_users);
        btnPurchase = view.findViewById(R.id.group_frag_purchase);
        ibBack = view.findViewById(R.id.group_view_btn_back);
    }

    //leave group -- > shows a dialog.
    public void leaveGroup(Post post, MainSearchPostsViewModel viewModel, String currentUid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("יציאה מהקבוצה").setMessage("האם אתה בטוח שברצונך לצאת מהקבוצה?")
                .setPositiveButton("כן", (dialogInterface, i) -> {
                    viewModel.removeUserFromGroup(post, currentUid);
                    Toast.makeText(requireContext(), "יצאת מהקבוצה בהצלחה", Toast.LENGTH_SHORT).show();
                    navController.navigate(R.id.action_fragmentGroupView_to_searchFragment);
                }).setNegativeButton("לא", (dialogInterface, i) -> {
        });
        builder.create();
        builder.show();
    }

    //update ui
    private void updateUI(Post currentPost) {

        // try to attach data to ui by currentPost data .
        tvTitle.setText(currentPost.getTitle());
        tvDescription.setText(currentPost.getDescription());
        tvGroupPrice.setText(String.format("%s ש''ח", currentPost.getGroupPrice()));
        Picasso.get().load(currentPost.getPostImageUri()).into(ivItem);
        tvShipmentMethod.setText(String.format("אופן אספקת המוצר - %s", currentPost.getShipmentMethod()));

        if (currentPost.getUid() == null) {// if group without manager show all users count
            tvPeopleCount.setText(MessageFormat.format("{0} אנשים בקבוצה", String.valueOf(currentPost.getUsersCount())));

        } else {// if group with manager -> show user count - 1.
            tvPeopleCount.setText(MessageFormat.format("{0} אנשים בקבוצה", String.valueOf(currentPost.getUsersCount() - 1)));
        }

        try {
            if (currentPost.getUid() == null) {
                tvPrice.setText("__");
                tvGroupPrice.setText("__");
                tvDates.setText(String.format("%s-%s", currentPost.getStartTime(), "פתוח"));
                tvEmail.setText("אין מנהל");
                tvUserName.setText("אין מנהל");
                tvMaxPeople.setText("__");
                tvMinPeople.setText("__");
                tvPageTitle.setText("כח רכישה");
            } else {
                final User mUser = usersViewModel.getSpecificUser(currentPost.getUid());
                tvPageTitle.setText("קבוצת רכישה");
                tvPrice.setText(String.format("%s ש''ח", currentPost.getOriginalPrice()));
                tvMaxPeople.setText(currentPost.getMaxPeople());
                tvMinPeople.setText(currentPost.getMinPeople());
                tvEmail.setText(mUser.getEmail());
                tvUserName.setText(mUser.getName());
                tvDates.setText(String.format("%s-%s", currentPost.getStartTime(), currentPost.getEndTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "updateUI: " + e.getMessage());
        }
    }


    // send mail btn method.
    private void composeEmail() {
        createDeepLink();

    }

    //checks if purchase needs to be updated.
    private void checkPurchaseStarted(Post mPost) {
        if (mPost.getUid() == null) { // first condition --> check if it's a power group.
            return;
        }
        if (!mPost.isGroupStarted()) {// if the group didn't start --> check if it needs to be started.(the purchase part)
            final int usersCount = mPost.getUsersCount();
            final String minPeople = mPost.getMinPeople();


            if (usersCount-1 >= Integer.parseInt(minPeople)) { // minPeople<=Current userCount?
                btnPurchase.setActivated(true);
                mPost.setGroupStarted(true); // PURCHASE STARTS.
                postsViewModel.updatePostsList(mPost); // update post in db.
                postsViewModel.select(mPost); // reselect the updated post.
                updateUI(mPost);
                if (firebaseUser.getUid().equals(currentPost.getUid())) {
                    btnLeave.setVisibility(View.GONE);
                }
                btnEdit.setVisibility(View.GONE);
                btnPurchase.setBackground(getResources().getDrawable(R.drawable.back_button));
                btnPurchase.setTextColor(getResources().getColor(R.color.background));
            } else { // usersCount isn't reach min people.
                btnPurchase.setActivated(false);
            }
        } else { // group started is true - > just show the btn. no need to use listener, cuz its old state.
            if (firebaseUser.getUid().equals(currentPost.getUid())) { // dont show leave for managers only.
                btnLeave.setVisibility(View.GONE);
            }
            btnPurchase.setActivated(true);
            btnEdit.setVisibility(View.GONE);
            btnPurchase.setBackground(getResources().getDrawable(R.drawable.back_button));
            btnPurchase.setTextColor(getResources().getColor(R.color.background));

        }
    }

    //interfaces implement
    @Override
    public void viewClicked(List<User> userList, int position) {
        bundleUserType.putString("uid", userList.get(position).getUid());
        navController.navigate(R.id.action_fragmentGroupView_to_otherUserProfileFragment, bundleUserType);
        Toast.makeText(requireContext(), "USER CLICKED", Toast.LENGTH_SHORT).show();
    }

    /* show the bottom navigation menu when this fragment killed */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "finalize: FRAGMENT IS DESTROYED");
//        bottomNavigationView.setVisibility(View.VISIBLE);
    }


    //deep link when composing an email .(POWER GROUPS ONLY)
    // i created deep link from fragment group view. ( WITH ID OF GROUP ) .
    private Uri createDeepLink() {
        try {
            DynamicLink task = FirebaseDynamicLinks.getInstance().createDynamicLink().
                    setLink(Uri.parse("https://www.togatheril.com/"))
                    .setDomainUriPrefix("https://togatheril.page.link")
                    // Open links with this app on Android
                    .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                            //TODO:.setFallbackUrl() -- goes to google play.
                            .build())
                    // Open links with com.example.ios on iOS
                    .buildDynamicLink();
            Log.d(TAG, "createDeepLink: " + task.getUri().toString());
            // manual link
            String sharelinktext = "https://togatheril.page.link/?" +
                    "link=https://www.togatheril.com/groups/id=" + currentPost.getGroupId() +
                    "&apn=" + getActivity().getPackageName();
//                "&st=" + currentPost.getGroupId();
            Log.d(TAG, "createDeepLink: " + Uri.parse(sharelinktext));

            Task<ShortDynamicLink> task1 = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLongLink(Uri.parse(sharelinktext))
                    .buildShortDynamicLink()
                    .addOnCompleteListener(getActivity(), task2 -> {
                        if (task2.isComplete())
                            dynamicLink = task2.getResult().getShortLink();
                        Log.d(TAG, "createDeepLink: " + dynamicLink.toString());
                        String subject = "הזמנה לניהול קבוצת רכישה" /* Your subject here */;
                        String body = String.format(getResources().getString(R.string.group_mail_body), currentPost.getTitle(), dynamicLink.toString());
                        String chooserTitle = "הזמנה לניהול כח רכישה";/* Your chooser title here */
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                        if (emailIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                            startActivity(emailIntent.createChooser(emailIntent, chooserTitle));
                        }
                    });

            return dynamicLink;
        } catch (Exception e) {
            Log.w(TAG, "createDeepLink: ", e.getCause().fillInStackTrace());
            Toast.makeText(getActivity(), "נכשל ביצירת לינק, אנא נסו שנית מאוחר יותר", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void handleOnBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.searchFragment, bundleUserType);
            }
        });
    }
}
