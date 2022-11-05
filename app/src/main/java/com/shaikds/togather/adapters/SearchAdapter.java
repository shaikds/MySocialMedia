package com.shaikds.togather.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shaikds.togather.R;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.PostViewHolder> {

    List<Post> postsList;
    final String TAG = "SearchAddapter";
    private handleGroupClick clickListener;
    private onRvFinishedCallback rvFinishedCallback;
    private List<User> usersList;
    User postManager;
    private PostViewHolder firstViewHolder;
    private int groupLikesCount;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


    public SearchAdapter(handleGroupClick clickListener, onRvFinishedCallback rvFinishedCallback) {
        this.clickListener = clickListener;
        this.rvFinishedCallback = rvFinishedCallback;
        this.usersList = new ArrayList<>();
        this.postsList = new ArrayList<>();
    }

    public void setUsersList(List<User> usersList) {
        this.usersList = usersList;
    }

    public void setPostsList(List<Post> postsList) {
/*        if (this.postsList.size() > 0) {
            this.postsList.clear();
        }*/
        //for removing from list not relevant groups .
/*        for (Iterator<Post> iterator = postsList.iterator(); iterator.hasNext(); ) {
            final Post post = iterator.next();
            // dont show this group --> no one can join it anyway.(should i leave it?)

            if (post.getMaxPeople()!=null && post.getMaxPeople().equals(post.getMinPeople())) {
                iterator.remove();
            }
        }*/
        this.postsList = postsList;
        notifyDataSetChanged();
    }

    //GETTERS
    public PostViewHolder getFirstViewHolder() {
        return firstViewHolder;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        if (position == 0 && holder != null) {
            firstViewHolder = holder;
        }
        if (position == 1) {
            rvFinishedCallback.onRvFinished(holder);
        }

        final Post post = postsList.get(position);
        groupLikesCount = groupLikesCount(holder, post.getGroupId());
        if (post.getUid() != null) {
            // people count / minimum people to start purchasing
            holder.tvPeopleCount.setText(MessageFormat.format(" {0}/{1} אנשים", post.getUsersCount() - 1, post.getMinPeople()));
            // show start and end dates of this group .
            holder.tvDate.setText(MessageFormat.format("{0} - {1}", post.getStartTime(), post.getEndTime()));
            for (User user : usersList) {
                if (post.getUid().equals(user.getUid())) { // if the post manager uid equals to specific user uid -->create user for post.
                    postManager = user;
                    Picasso.get().load(postManager.getUrl()).into(holder.ivProfile);
                }
            }
        } else {
            // power group ! no manager.
            holder.tvPeopleCount.setText(MessageFormat.format("{0}/{1}", post.getUsersCount(), "פתוח"));
            holder.tvDate.setText(MessageFormat.format("{0}-{1}", post.getStartTime(), "פתוח"));
        }
        // default.
        Picasso.get().load(post.getPostImageUri()).into(holder.ivItemPhoto);
        holder.btnLike.setText(String.valueOf(groupLikesCount));
        holder.tvLocation.setText(post.getLocation());
        holder.tvTitle.setText(post.getTitle());
        holder.tvPrice.setText(post.getGroupPrice());
        holder.tvLocation.setText(post.getLocation());

        //title of purchase group .
        holder.tvTitle.setOnClickListener(v -> {
            clickListener.groupTitleClicked(position, post);
            Log.d(TAG, "PostAdapter: group title clicked");
        });

        // image of manager profile
        holder.ivProfile.setOnClickListener(v -> {
            clickListener.sellerGroupClicked(position, post);
            Log.d(TAG, "GroupAdapter: seller profile pic clicked");
        });

        //likes functionality
        holder.btnLike.setOnClickListener(v -> {
            if (!holder.isLiked) {
                // liked.
                holder.isLiked = true;
                groupLikesCount = holder.likesCount += 1;
                holder.btnLikeText(String.valueOf(groupLikesCount)); // update ui .
                holder.btnLike.setBackgroundResource(R.drawable.ic_baseline_like_24);

                // not liked
            } else {
                groupLikesCount = holder.likesCount -= 1;
                holder.btnLikeText(String.valueOf(groupLikesCount)); // update ui .
                holder.isLiked = false;
                holder.btnLike.setBackgroundResource(R.drawable.ic_baseline_dislike_24);

            }
            clickListener.groupLikeClicked(holder, groupLikesCount, post);
            Log.d(TAG, "GroupAdapter: like group clicked");
        });

        // join group .
        holder.btnJoinGroup.setOnClickListener(v -> {
            clickListener.groupJoinClicked(position, post);
            Log.d(TAG, "GroupAdapter: join group clicked");
        });
        // options of group .
        holder.btnOptions.setOnClickListener(v -> {
            clickListener.groupOptionsClicked(position, post);
            Log.d(TAG, "GroupAdapter: options group clicked");
        });

        // label and people count are changing, if group started purchasing .
        try {
            final int groupDiscount = Integer.parseInt(post.getGroupPrice()); // maybe null because power groups.
            final int regularPrice = Integer.parseInt(post.getOriginalPrice());
            if (post.isGroupStarted()) {
                holder.tvDiscountPer.setText("הרכישה החלה!");
                holder.tvPeopleCount.setText(MessageFormat.format(" {0}/{1} אנשים", post.getUsersCount() - 1, post.getMaxPeople()));
                if (post.getUsersCount() - 1 == Integer.parseInt(post.getMaxPeople())) {
                    holder.tvDiscountPer.setText("הרכישה הסתיימה");
                }
            } else {
                holder.tvDiscountPer.setText
                        (MessageFormat.format("{0}% חסכון", 100 - (groupDiscount * 100 / regularPrice)));
            }

        } catch (Exception e) {
            holder.tvPrice.setText("?");
            holder.tvDiscountPer.setText("כח רכישה");
            e.printStackTrace();
            Log.d(TAG, "onBindViewHolder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        if (postsList == null || postsList.isEmpty()) {
            return 0;
        } else {
            return postsList.size();
        }
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {

        public TextView tvTitle, tvDate, tvLocation, tvPrice, tvPeopleCount, tvDiscountPer;
        private ImageView ivItemPhoto, ivProfile;
        public Button btnLike;
        public ImageButton btnOptions, btnJoinGroup;

        public int likesCount = 0;
        public boolean isLiked = false;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            findViewsById();
            //navController = Navigation.findNavController(itemView);
            //clicking on buttons
        }

        private void findViewsById() {
            tvTitle = itemView.findViewById(R.id.tv_title_post_item);
            tvDate = itemView.findViewById(R.id.tv_time_post_item);
            tvLocation = itemView.findViewById(R.id.tv_location_post_item);
            tvPrice = itemView.findViewById(R.id.tv_price_post_item);
            ivItemPhoto = itemView.findViewById(R.id.iv_post_item);
            tvPeopleCount = itemView.findViewById(R.id.tv_people_number_post_item);
            ivProfile = itemView.findViewById(R.id.iv_profile_post_item);
            btnJoinGroup = itemView.findViewById(R.id.join_group_btn_post_item);
            btnLike = itemView.findViewById(R.id.like_btn_post_item);
            btnOptions = itemView.findViewById(R.id.options_btn_post_item);
            tvDiscountPer = itemView.findViewById(R.id.tv_search_item_discount);
        }

        void btnLikeText(String text) {
            btnLike.setText(text);
        }


    }


    public interface handleGroupClick {
        void groupTitleClicked(int position, Post post);

        void groupJoinClicked(int position, Post post);

        void groupLikeClicked(PostViewHolder holder, int likesCount, Post post);

        void groupOptionsClicked(int position, Post post);

        void sellerGroupClicked(int position, Post post);

    }

    public interface onRvFinishedCallback {
        void onRvFinished(@NonNull PostViewHolder viewHolder);
    }

    private int groupLikesCount(PostViewHolder viewHolder, String groupId) {
        for (User user : usersList) {

            if (user.getGroupLikes().size() > 0) { // there are groupIds inside this array
                if (user.getGroupLikes().contains(groupId)) { // if the current groupId equals firebase user uid
                    if (user.getUid().equals(firebaseUser.getUid())) {// if its my user --> change the ui .
                        // I liked it so isLiked is true.
                        viewHolder.isLiked = true;
                        viewHolder.btnLike.setBackgroundResource(R.drawable.ic_baseline_like_24);
                    }
                    // current post group id is in user likes --> add 1 to like count.
                    viewHolder.likesCount += 1;
                }
            }
        }
        return viewHolder.likesCount;
    }
}