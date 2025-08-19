// File: app/src/main/java/com/s23010269/skill4u/ChallengeAdapter.java
package com.s23010269.skill4u;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    private Context context;
    private List<Challenge> challengeList;
    private OnChallengeActionListener listener;

    public ChallengeAdapter(Context context, List<Challenge> challengeList, OnChallengeActionListener listener) {
        this.context = context;
        this.challengeList = challengeList;
        this.listener = listener;
    }

    // Interface for click events
    public interface OnChallengeActionListener {
        void onJoinChallenge(Challenge challenge);
        void onCompleteChallenge(Challenge challenge);
        void onChallengeClick(Challenge challenge);
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

        holder.challengeTitleTextView.setText(challenge.getTitle());
        holder.challengeDifficultyTextView.setText(challenge.getDifficulty());
        holder.usersJoinedTextView.setText(challenge.getUsersJoined() + " users joined");

        // Set up click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChallengeClick(challenge);
            }
        });

        // Conditionally show/hide and configure the "Complete" button
        if (challenge.isJoined() && !challenge.isCompleted()) {
            holder.completeButton.setVisibility(View.VISIBLE);
            holder.completeButton.setText("Complete");
            holder.completeButton.setBackgroundResource(R.drawable.challengebutton_background);
            holder.completeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCompleteChallenge(challenge);
                }
            });
        } else {
            holder.completeButton.setVisibility(View.GONE); // Hide if not joined or already completed
        }

        // Display points
        holder.challengePointsTextView.setText(challenge.getPoints() + " pts");
    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    public void updateChallengeList(List<Challenge> newList) {
        this.challengeList = newList;
        notifyDataSetChanged();
    }

    static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView challengeTitleTextView;
        TextView challengeDifficultyTextView;
        TextView usersJoinedTextView;
        TextView challengePointsTextView;
        Button completeButton;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            challengeTitleTextView = itemView.findViewById(R.id.challengeTitleTextView);
            challengeDifficultyTextView = itemView.findViewById(R.id.challengeDifficultyTextView);
            usersJoinedTextView = itemView.findViewById(R.id.usersJoinedTextView);
            challengePointsTextView = itemView.findViewById(R.id.challengePointsTextView);
            completeButton = itemView.findViewById(R.id.completeButton);
        }
    }
}