package com.s23010269.skill4u;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ChallengeDetailDialogFragment extends DialogFragment {

    private static final String ARG_CHALLENGE = "challenge_object"; // Key for passing Challenge object in Bundle

    // Listener interface to communicate with the host activity
    public interface OnChallengeInteractionListener {
        void onChallengeJoined(Challenge challenge); // Called when a challenge is joined
    }

    private OnChallengeInteractionListener challengeInteractionListener; // Reference to the listener

    // Factory method to create a new instance of the dialog with a Challenge object
    public static ChallengeDetailDialogFragment newInstance(Challenge challenge) {
        ChallengeDetailDialogFragment fragment = new ChallengeDetailDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CHALLENGE, challenge); // Pass Challenge object to fragment
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure the host activity implements the listener
        if (context instanceof OnChallengeInteractionListener) {
            challengeInteractionListener = (OnChallengeInteractionListener) context;
        } else if (getParentFragment() instanceof OnChallengeInteractionListener) {
            challengeInteractionListener = (OnChallengeInteractionListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChallengeInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        challengeInteractionListener = null; // Clear listener reference
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE); // Remove title from dialog
        }

        View view = inflater.inflate(R.layout.dialog_challenge_detail, container, false); // Inflate layout

        // Initialize UI elements
        TextView titleText = view.findViewById(R.id.dialogChallengeTitle);
        TextView difficultyText = view.findViewById(R.id.dialogChallengeDifficulty);
        TextView descriptionText = view.findViewById(R.id.dialogChallengeDescription);
        TextView usersJoinedText = view.findViewById(R.id.dialogChallengeUsersJoined);
        TextView pointsText = view.findViewById(R.id.dialogChallengePoints);
        TextView relatedLinksText = view.findViewById(R.id.dialogRelatedLinks);
        ImageView closeBtn = view.findViewById(R.id.closeButton);
        Button joinButton = view.findViewById(R.id.joinButton);

        Bundle args = getArguments();
        Challenge challenge = null; // Placeholder for the challenge object
        if (args != null) {
            challenge = (Challenge) args.getSerializable(ARG_CHALLENGE); // Get Challenge from arguments
            if (challenge != null) {
                final Challenge currentChallenge = challenge; // Make effectively final for lambdas

                // Set UI elements with challenge data
                titleText.setText(currentChallenge.getTitle());
                difficultyText.setText("Difficulty: " + currentChallenge.getDifficulty());
                descriptionText.setText(currentChallenge.getDescription());
                usersJoinedText.setText("Users Joined: " + currentChallenge.getUsersJoined());
                pointsText.setText("Points: " + currentChallenge.getPoints());

                // Display related links if available
                if (currentChallenge.getRelatedLinks() != null && !currentChallenge.getRelatedLinks().isEmpty()) {
                    relatedLinksText.setVisibility(View.VISIBLE);
                    relatedLinksText.setText("Related Links: " + currentChallenge.getRelatedLinks());
                    relatedLinksText.setOnClickListener(v -> {
                        // Open the first link in browser
                        String url = currentChallenge.getRelatedLinks().split(",")[0].trim();
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "http://" + url;
                        }
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(browserIntent);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Could not open link: " + url, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    relatedLinksText.setVisibility(View.GONE); // Hide if no links
                }

                // Adjust join button based on join/completion status
                if (currentChallenge.isJoined()) {
                    joinButton.setText("Already Joined");
                    joinButton.setEnabled(false);
                    if (currentChallenge.isCompleted()) {
                        joinButton.setText("Completed"); // Show completed text
                    }
                } else {
                    joinButton.setText("Join Challenge");
                    joinButton.setEnabled(true); // Enable join button
                }
            }
        }

        // Close button dismisses dialog
        closeBtn.setOnClickListener(v -> dismiss());

        final Challenge finalChallengeForJoin = challenge; // Ensure effectively final for lambda

        // Join button logic
        joinButton.setOnClickListener(v -> {
            if (finalChallengeForJoin != null && !finalChallengeForJoin.isJoined()) {
                if (challengeInteractionListener != null) {
                    challengeInteractionListener.onChallengeJoined(finalChallengeForJoin); // Notify activity
                }
                dismiss(); // Close dialog after joining
            }
        });

        return view; // Return the dialog view
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // Make dialog full screen
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
