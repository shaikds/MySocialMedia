package com.shaikds.togather.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.shaikds.togather.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    ImageView imageView;
    Intent deepLinkIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        imageView = findViewById(R.id.splash_iv_logo);
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
            @Override
            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                // get deep link from result.
                Uri deepLink = null;
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.getLink();
                    Log.d(TAG, "referLink: " + deepLink.toString());
                    String referLink = deepLink.toString();
                    try {
                        referLink = referLink.substring(referLink.lastIndexOf("=") + 1);
                        Log.d(TAG, "substring: " + referLink);
                        deepLinkIntent = new Intent(SplashScreen.this, DeepLinkActivity.class);
                        deepLinkIntent.putExtra("groupId", referLink);
                    } catch (Exception e) {
                        Log.d(TAG, "catch: " + e.getMessage());
                    }
                }
            }
        });

        intentHandler(user);
        return super.onCreateView(parent, name, context, attrs);

    }

    private void intentHandler(FirebaseUser user) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (deepLinkIntent == null) {
                    if (user != null) { // user already exists.
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else { // user needs to login/sign up.
                        Intent intent1 = new Intent(SplashScreen.this, LoginActivity.class);
                        startActivity(intent1);
                        finish();
                    }
                } else { //
                    startActivity(deepLinkIntent);
                    finish();
                    SplashScreen.this.finish();

                }
            }
        }, 2000);

    }
}