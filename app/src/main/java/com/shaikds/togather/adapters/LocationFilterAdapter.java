package com.shaikds.togather.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shaikds.togather.R;
import com.shaikds.togather.repository.IsraelLocationsRepo;

import java.util.List;

public class LocationFilterAdapter extends RecyclerView.Adapter<LocationFilterAdapter.LocationFilterViewHolder> {

    private static final String TAG = "LocationFilterAdapter";
    private final List<String> israelCities;
    OnClickedLocation onClickedLocation;

    public LocationFilterAdapter(OnClickedLocation onClickedLocation, Context context) {
        this.onClickedLocation = onClickedLocation;
        israelCities = new IsraelLocationsRepo().readFromFile(context);
    }

    @NonNull
    @Override
    public LocationFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        return new LocationFilterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationFilterViewHolder holder, int position) {
        holder.tvLocation.setText(israelCities.get(position));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onClickedLocation.onClickedLocation(israelCities.get(position), isChecked);
            Log.d(TAG, "onCheckedChanged: check changed at " + israelCities.get(position) + " " + isChecked);
        });
    }

    @Override
    public int getItemCount() {
        if (israelCities == null) {
            return 0;
        }
        return israelCities.size();
    }

    public interface OnClickedLocation {
        void onClickedLocation(String city, boolean isChecked);
    }

    static class LocationFilterViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvLocation;
        private boolean isChecked = false;

        public LocationFilterViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.location_item_check_box);
            tvLocation = itemView.findViewById(R.id.location_item_tv);

            itemView.setOnClickListener(v -> {
                isChecked = !isChecked;
                checkBox.setChecked(isChecked);
                checkBox.callOnClick();
            });
        }
    }
}
