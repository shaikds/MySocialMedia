package com.shaikds.togather.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shaikds.togather.R;
import com.shaikds.togather.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.searchViewHolder> {
    Context context;
    List<User> usersList;

    final static String TAG = "UsersAdapter";
    OnClickedUser onClickedUserInterface;

    public UsersAdapter(Context context, List<User> usersList, OnClickedUser onClickedUserInterface) {
        this.context = context;
        this.usersList = usersList;
        this.onClickedUserInterface = onClickedUserInterface;
    }

    public void setUsersLists(List<User> userList) {
        this.usersList.clear();
        this.usersList.addAll(userList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public searchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View v = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
        return new searchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull searchViewHolder holder, int position) {
        //get data
        String userImg = usersList.get(position).getUrl();
        String userName = usersList.get(position).getName();
        String userProfession = usersList.get(position).getProf();

        //set data
        holder.tvName.setText(userName);
        holder.tvJob.setText(userProfession);
        try {
            Picasso.get().load(userImg).placeholder(R.drawable.ic_baseline_person_24).into(holder.ivProfileImg);
        } catch (Exception e) {
            Log.d(TAG, "onBindViewHolder: Problem in try catch in here");
        }
        //handle item click
        holder.itemView.setOnClickListener(v -> onClickedUserInterface.userClicked(position, usersList.get(position)));
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //view holder class
    class searchViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfileImg;
        TextView tvName, tvJob;

        public searchViewHolder(@NonNull View itemView) {
            super(itemView);


            //init views
            ivProfileImg = itemView.findViewById(R.id.user_profile_iv_listitem);
            tvName = itemView.findViewById(R.id.user_name_listitem);
            tvJob = itemView.findViewById(R.id.user_description_list_item);

            //set on click Listener
        }
    }

    public interface OnClickedUser {
        void userClicked(int position, User user);
    }
}
