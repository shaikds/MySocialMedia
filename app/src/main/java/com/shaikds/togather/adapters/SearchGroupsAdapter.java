package com.shaikds.togather.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shaikds.togather.R;
import com.shaikds.togather.model.Post;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class SearchGroupsAdapter extends RecyclerView.Adapter<SearchGroupsAdapter.SearchGroupsViewHolder> {
    private List<Post> postList;
    protected IsGroupClicked isGroupClickedInterface;
    private SearchGroupsViewHolder firstViewHolder;


    // REQUIRED
    public SearchGroupsAdapter(IsGroupClicked isGroupClicked) {
        postList = new ArrayList<>();
        this.isGroupClickedInterface = isGroupClicked;
    }

    public void setPostList(List<Post> postList) {
        this.postList.clear();
        this.postList = postList;
    }



    @NonNull
    @Override
    public SearchGroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_search, parent, false);
        return new SearchGroupsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchGroupsViewHolder holder, int position) {
        if (position == 0) {
            firstViewHolder = holder;
        }
        Post post = postList.get(position);
        try {
            holder.tvCount.setText(MessageFormat.format("{0} אנשים בקבוצה", post.getUsersCount()));
            holder.tvTitle.setText(post.getTitle());
            holder.tvLocation.setText(post.getLocation());
            holder.tvDates.setText(MessageFormat.format("{0} - {1}", post.getStartTime(), post.getEndTime()));
            if (post.getUid() != (null)) {
                holder.tvLabel.setText("קבוצת רכישה");
            }
            Picasso.get().load(post.getPostImageUri()).into(holder.ivPostImg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  handle item click
        holder.itemView.setOnClickListener(v -> isGroupClickedInterface.groupClicked(position, postList));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class SearchGroupsViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel, tvDates, tvLocation, tvTitle, tvCount;
        ImageView ivPostImg;

        public SearchGroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDates = itemView.findViewById(R.id.item_group_search_dates);
            tvLabel = itemView.findViewById(R.id.item_group_search_tv_label);
            tvLocation = itemView.findViewById(R.id.item_group_search_location);
            tvTitle = itemView.findViewById(R.id.item_group_search_title);
            tvCount = itemView.findViewById(R.id.item_group_search_member_count);
            ivPostImg = itemView.findViewById(R.id.item_group_search_iv);
        }
    }

    public interface IsGroupClicked {
        void groupClicked(int position, List<Post> postList);
    }
}

