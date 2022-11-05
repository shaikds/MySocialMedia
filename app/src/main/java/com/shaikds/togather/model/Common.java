package com.shaikds.togather.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class Common {
    public static FirebaseDatabase rtDatabase = FirebaseDatabase.getInstance("https://mysocialmedia-eb38f-default-rtdb.firebaseio.com/");
    private Context context;

    public Common(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void intentMethod(Activity activity) {
        Intent intent = new Intent(getContext(), activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getContext().startActivity(intent);
    }


    /* put data in bundle*/
    public Bundle checkUserType(Post post) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundleUserType = new Bundle();
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
        return bundleUserType;
    }


}

