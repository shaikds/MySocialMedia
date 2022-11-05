package com.shaikds.togather.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.shaikds.togather.R;

import com.shaikds.togather.model.Post;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fr.tvbarthel.lib.blurdialogfragment.SupportBlurDialogFragment;

public class FragmentPreviewGroupView extends SupportBlurDialogFragment {
    private static final String TAG = "PreviewGroupView";
    private int position;
    private Post post;
    Button btnRegister;
    MainSearchPostsViewModel postsViewModel;
    FirebaseUser firebaseUser;
    Bundle bundleUserType;

    public FragmentPreviewGroupView(int position, Post post) {
        // Required empty public constructor
        this.position = position;
        this.post = post;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_preview_group_view, container, false);
        btnRegister = v.findViewById(R.id.fragment_preview_btn);
        btnRegister.setOnClickListener(v1 -> groupJoinClicked(position, post));
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postsViewModel = new ViewModelProvider(requireActivity()).get(MainSearchPostsViewModel.class);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected float getDownScaleFactor() {
        return (float) 4.0;
    }

    @Override
    protected int getBlurRadius() {
        // Allow to customize the blur radius factor.
        return 8;
    }


    @Override
    protected boolean isActionBarBlurred() {
        // Enable or disable the blur effect on the action bar.
        // Disabled by default.
        return false;
    }

    @Override
    protected boolean isDimmingEnable() {
        // Enable or disable the dimming effect.
        // Disabled by default.
        return false;
    }

    @Override
    protected boolean isRenderScriptEnable() {
        // Enable or disable the use of RenderScript for blurring effect
        // Disabled by default.
        return false;
    }

    @Override
    protected boolean isDebugEnable() {
        // Enable or disable debug mode.
        // False by default.
        return false;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        this.dismiss();
    }

    private void groupJoinClicked(int position, Post post) {
        int maxPeople = 0;
        postsViewModel.select(post);

        // If he's not a member of this group, try to ADD him.
        // users count is smaller than max people defined in group.
        final int usersCount = post.getUsersCount();
        try {
            maxPeople = Integer.parseInt(post.getMaxPeople());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(requireActivity(), "כוח רכישה", Toast.LENGTH_SHORT).show();
            postsViewModel.addUserToGroup(position, firebaseUser.getUid());
            checkUserType(post);
            postsViewModel.getNavController().navigate(R.id.action_searchFragment_to_fragmentGroupView, bundleUserType);
            Log.d(TAG, "groupJoinClicked: This group is Power Group --> not active.");
            this.dismiss();
            return; // IMPORTANT SO IT WONT GO ON.
        }
        if (usersCount-1 < maxPeople) { // there is a place to join .
            Log.d(TAG, "groupJoinClicked: This group is Selling Group -->  active.");
            postsViewModel.addUserToGroup(position, firebaseUser.getUid());
            checkUserType(postsViewModel.getSelectedGroup().getValue());
            Toast.makeText(requireActivity(), "הצטרפת בהצלחה לקבוצה", Toast.LENGTH_SHORT).show();
            postsViewModel.getNavController().navigate(R.id.fragmentGroupView, bundleUserType);
        }
        if (maxPeople <= usersCount-1) { // no place left.
            Toast.makeText(requireActivity(), "נגמר המקום. נסה קבוצה אחרת", Toast.LENGTH_LONG).show();
        }
        this.dismiss();
    }

    /* put data in bundle*/
    void checkUserType(Post post) {
        bundleUserType = new Bundle();
        try {
            if (post.getUid().equals(firebaseUser.getUid())) { //manager --> show btn edit and delete.
                bundleUserType.putString("userType", "manager");
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
}