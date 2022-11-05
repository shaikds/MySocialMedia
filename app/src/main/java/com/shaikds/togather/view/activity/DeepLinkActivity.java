package com.shaikds.togather.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.shaikds.togather.model.Post;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.shaikds.togather.R;
import com.squareup.picasso.Picasso;

public class DeepLinkActivity extends AppCompatActivity {
    TextView tvLocation, tvMemberCount, tvTitle, tvPrice;
    ImageView iv;
    Button btnRegister;
    MainSearchPostsViewModel postsViewModel;
    Post currentPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link);
        tvLocation = findViewById(R.id.deep_link_tv_location);
        tvMemberCount = findViewById(R.id.deep_link_people_count);
        tvTitle = findViewById(R.id.deep_link_title);
        tvPrice = findViewById(R.id.deep_link_group_price);
        iv = findViewById(R.id.deep_link_iv);
        btnRegister = findViewById(R.id.deep_link_btn_join);

        String groupId = getIntent().getExtras().getString("groupId");

        //go to login
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(DeepLinkActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            DeepLinkActivity.this.finish();
        });

        postsViewModel = new ViewModelProvider(this).get(MainSearchPostsViewModel.class);
        postsViewModel.getAllPosts().observe(this, posts -> {
            if (posts != null) {
                currentPost = postsViewModel.getGroupById(groupId);
                if (currentPost != null) {
                    tvTitle.setText(currentPost.getTitle());
                    tvLocation.setText(currentPost.getLocation());
                    tvMemberCount.setText(String.valueOf(currentPost.getUsersCount()));
                    tvPrice.setText(currentPost.getGroupPrice());
                    Picasso.get().load(currentPost.getPostImageUri()).into(iv);
                    postsViewModel.select(currentPost);
                    saveGroupIdInSharedP(groupId);
                }
            }
        });
    }


    private void saveGroupIdInSharedP(String groupId) {
        SharedPreferences sharedPref = getSharedPreferences("tours_sp",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.deep_link_group_id), groupId);
        editor.putInt(getString(R.string.deep_link_group_position), postsViewModel.getIndex());
        editor.apply();
    }

}