package com.shaikds.togather.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shaikds.togather.R;
import com.shaikds.togather.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.MemberViewHolder> {

    List<User> groupMembers = new ArrayList<>();
    Map<String, String> codesMap = new HashMap<>();
    Context context;
    public OnClickedView onClickedViewInterface;

    //empty constructor
    public GroupMembersAdapter(Context context, OnClickedView onClickedViewInterface) {
        this.context = context;
        this.onClickedViewInterface = onClickedViewInterface;

    }

    public void setCodesMap(Map<String, String> codesMap) {
        if (codesMap.size() == 0) {
            this.codesMap = codesMap;
            notifyDataSetChanged();
        } else {
            this.codesMap.clear();
            this.codesMap = codesMap;
            notifyDataSetChanged();
        }
    }

    public void setUsers(List<User> groupMembers) {
        if (groupMembers.size() == 0) {
            this.groupMembers = groupMembers;
            notifyDataSetChanged();
            return;
        } else {
            this.groupMembers.clear();
            this.groupMembers = groupMembers;
            notifyDataSetChanged();
        }
    }


    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.member_item, parent, false);

        return new MemberViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {

        try {
            final String url = groupMembers.get(position).getUrl();
            Picasso.get().load(url).into(holder.ivProfile);
        } catch (NullPointerException e) {

        }
        holder.ivProfile.setOnClickListener(v -> onClickedViewInterface.viewClicked(groupMembers, position));
        if (groupMembers.size() - 1 == position) {
        }
    }

    @Override
    public int getItemCount() {
        return groupMembers.size();
    }


    //View holder
    class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.member_item_iv);
        }
    }

    //Interface
    public interface OnClickedView {
        void viewClicked(List<User> userList, int position);

    }
}


