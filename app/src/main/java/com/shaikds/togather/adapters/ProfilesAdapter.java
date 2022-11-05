package com.shaikds.togather.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shaikds.togather.R;
import com.shaikds.togather.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfilesAdapter extends RecyclerView.Adapter<ProfilesAdapter.ProfileViewHolder> {
    List<Post> postList = new ArrayList<>();
    OnClickedPost onClickedPost;
    Context context;
    private final String TAG = "ProfileAdapter";

    public ProfilesAdapter(@NonNull Context context, OnClickedPost onClickedPost) {
        this.context = context;
        this.onClickedPost = onClickedPost;
    }

    public void setPostList(List<Post> postList) {
        this.postList.clear();
        this.postList = postList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);
        return new ProfilesAdapter.ProfileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        final Post post = postList.get(position);
        //show this post in this profile only if its the same uid like post manager.
        if (post.getPostImageUri() != null) {
            Picasso.get().load(post.getPostImageUri()).into(holder.btnProfileItem);
            holder.btnProfileItem.setOnClickListener(v -> {
                onClickedPost.onClickedPost(postList, position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public interface OnClickedPost {
        void onClickedPost(List<Post> postsList, int position);
    }


    class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageButton btnProfileItem;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            btnProfileItem = itemView.findViewById(R.id.profile_item_ib);
            btnProfileItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickedPost.onClickedPost(postList, getAdapterPosition());
        }
    }
}
