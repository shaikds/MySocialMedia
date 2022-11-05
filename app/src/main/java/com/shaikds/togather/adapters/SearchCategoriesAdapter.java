package com.shaikds.togather.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shaikds.togather.R;

import java.util.List;

public class SearchCategoriesAdapter extends RecyclerView.Adapter<SearchCategoriesAdapter.CategoriesViewHolder> {
    List<String> categoriesList;
    OnClickedCategory onClickedCategory;


    public SearchCategoriesAdapter(OnClickedCategory onClickedCategory) {
        this.onClickedCategory = onClickedCategory;
    }

    public void setCategoriesList(List<String> categoriesList) {
        this.categoriesList = categoriesList;
    }

    @NonNull
    @Override
    public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoriesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesViewHolder holder, int position) {
        holder.tvCategoryName.setText(categoriesList.get(position));
    }


    @Override
    public int getItemCount() {
        if (categoriesList.isEmpty()) {
            return 0;
        } else {
            return categoriesList.size();
        }
    }

    //Interface
    public interface OnClickedCategory {
        void onCategoryClicked(List<String> categoriesList, int position);
    }

    // Inner Class
    class CategoriesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvCategoryName;

        public CategoriesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);

            tvCategoryName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickedCategory.onCategoryClicked(categoriesList, getAdapterPosition());
        }
    }
}
