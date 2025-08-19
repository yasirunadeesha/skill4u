package com.s23010269.skill4u;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardUser> userList;

    public LeaderboardAdapter(List<LeaderboardUser> userList) {
        this.userList = userList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, pointsText;

        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.user_name);
            pointsText = view.findViewById(R.id.user_points);
        }
    }

    @NonNull
    @Override
    public LeaderboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardAdapter.ViewHolder holder, int position) {
        LeaderboardUser user = userList.get(position);
        holder.nameText.setText(user.getName());
        holder.pointsText.setText(String.valueOf(user.getTotalPoints()));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
