package com.s23010269.skill4u; // Corrected package name, removed extra dot

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.s23010269.skill4u.R; // Corrected R import
import com.s23010269.skill4u.ChallengeDetailDialogFragment; // Corrected ChallengeDetailDialogFragment import path

import java.util.List;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    private Context context;
    private List<Challenge> challengeList;

    public ChallengeAdapter(Context context, List<Challenge> challengeList) {
        this.context = context;
        this.challengeList = challengeList;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge challenge = challengeList.get(position);
        holder.titleText.setText(challenge.getTitle());
        holder.difficultyText.setText(challenge.getDifficulty());
        holder.usersJoinedText.setText("Users Joined: " + challenge.getUsersJoined());

        holder.itemView.setOnClickListener(v -> {
            // Ensure the context is an instance of FragmentActivity to get SupportFragmentManager
            if (context instanceof FragmentActivity) {
                ChallengeDetailDialogFragment dialog = ChallengeDetailDialogFragment.newInstance(challenge);
                dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "ChallengeDetail");
            }
        });
    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    public static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, difficultyText, usersJoinedText;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            // Corrected IDs to match item_challenge.xml
            titleText = itemView.findViewById(R.id.challengeTitleTextView);
            difficultyText = itemView.findViewById(R.id.challengeDifficultyTextView);
            usersJoinedText = itemView.findViewById(R.id.usersJoinedTextView);
        }
    }
}