package com.shaikds.togather.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shaikds.togather.R;
import com.shaikds.togather.model.Code;
import com.shaikds.togather.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PurchaseMembersAdapter extends RecyclerView.Adapter<PurchaseMembersAdapter.PurchaseMembersViewHolder> {

    List<User> groupMembers = new ArrayList<>();
    List<Code> codesList = new ArrayList<>();
    Context context;
    public PurchaseMembersAdapter.OnClickedView onClickedViewInterface;

    //empty constructor
    public PurchaseMembersAdapter(Context context, PurchaseMembersAdapter.OnClickedView onClickedViewInterface) {
        this.context = context;
        this.onClickedViewInterface = onClickedViewInterface;

    }

    public void setCodesList(List<Code> codesList) {
        if (codesList.size() == 0) {
            this.codesList = codesList;
            notifyDataSetChanged();
        } else {
            this.codesList.clear();
            this.codesList = codesList;
            notifyDataSetChanged();
        }
    }

    public void setUsers(List<User> groupMembers) {
        if (groupMembers.size() == 0) {
            this.groupMembers = groupMembers;
            notifyDataSetChanged();
        } else {
            this.groupMembers.clear();
            this.groupMembers = groupMembers;
            notifyDataSetChanged();
        }

    }

    public List<Code> getCodesList() {
        return codesList;
    }

    @NonNull
    @Override
    public PurchaseMembersAdapter.PurchaseMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.purchase_member_item, parent, false);
        return new PurchaseMembersViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseMembersAdapter.PurchaseMembersViewHolder holder, int position) {
        final String name = groupMembers.get(position).getName();
        final String phoneNumber = groupMembers.get(position).getPhone();
        holder.tvName.setText(name);
        holder.tvPhone.setText(phoneNumber);
        try {
            final String url = groupMembers.get(position).getUrl();
            final String code = codesList.get(position).getCode();// get the code by uid.
            Picasso.get().load(url).into(holder.ivProfile);
            if (code == null) {
                holder.tvCode.setText("רעננו את הדף");
            }
            holder.tvCode.setText(code);
        } catch (Exception e) {
            holder.tvCode.setText("רעננו את הדף");
        }
        holder.itemView.setOnClickListener(v -> onClickedViewInterface.viewClicked(groupMembers, position));

    }

    @Override
    public int getItemCount() {
        return groupMembers.size();
    }


    //View holder
    static class PurchaseMembersViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvCode, tvName, tvPhone;

        public PurchaseMembersViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.member_item_iv);
            tvCode = itemView.findViewById(R.id.purchase_item_code);
            tvName = itemView.findViewById(R.id.purchase_item_name);
            tvPhone = itemView.findViewById(R.id.purchase_item_phone_number);

        }
    }

    //Interface
    public interface OnClickedView {
        void viewClicked(List<User> userList, int position);

    }


}



