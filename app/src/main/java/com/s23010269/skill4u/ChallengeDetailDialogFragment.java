package com.s23010269.skill4u;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Button; // Import Button
import android.widget.ImageView; // Import ImageView for close button

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class ChallengeDetailDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_DIFFICULTY = "difficulty";
    private static final String ARG_USERS_JOINED = "users_joined";

    public static ChallengeDetailDialogFragment newInstance(Challenge challenge) {
        ChallengeDetailDialogFragment fragment = new ChallengeDetailDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, challenge.getTitle());
        args.putString(ARG_DESCRIPTION, challenge.getDescription());
        args.putString(ARG_DIFFICULTY, challenge.getDifficulty());
        args.putInt(ARG_USERS_JOINED, challenge.getUsersJoined());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            // Set background drawable with rounded corners if desired, or just transparent
            // dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View view = inflater.inflate(R.layout.dialog_challenge_detail, container, false);

        TextView titleText = view.findViewById(R.id.dialogChallengeTitle);
        TextView difficultyText = view.findViewById(R.id.dialogChallengeDifficulty);
        TextView descriptionText = view.findViewById(R.id.dialogChallengeDescription);
        TextView usersJoinedText = view.findViewById(R.id.dialogChallengeUsersJoined);
        ImageView closeBtn = view.findViewById(R.id.closeButton); // Corrected ID
        Button joinButton = view.findViewById(R.id.joinButton); // Added join button reference

        Bundle args = getArguments();
        if (args != null) {
            titleText.setText(args.getString(ARG_TITLE));
            difficultyText.setText("Difficulty: " + args.getString(ARG_DIFFICULTY));
            descriptionText.setText(args.getString(ARG_DESCRIPTION));
            usersJoinedText.setText("Users Joined: " + args.getInt(ARG_USERS_JOINED));
        }

        closeBtn.setOnClickListener(v -> dismiss());

        joinButton.setOnClickListener(v -> {
            // Handle join button click
            // For example, show a toast or interact with a backend service
            // Toast.makeText(getContext(), "Joined " + titleText.getText(), Toast.LENGTH_SHORT).show();
            dismiss(); // Dismiss the dialog after joining
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // It's usually better for a dialog to be WRAP_CONTENT or specific dimensions,
            // but if MATCH_PARENT is intended for a full-screen dialog, it's fine.
            // For a more standard dialog that doesn't fill the screen, you might use:
            // dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // Or set a specific width/height, e.g., 90% of screen width
            // int width = getResources().getDisplayMetrics().widthPixels;
            // int height = getResources().getDisplayMetrics().heightPixels;
            // dialog.getWindow().setLayout((int) (width * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}