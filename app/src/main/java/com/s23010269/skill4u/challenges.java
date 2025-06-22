package com.s23010269.skill4u;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;


public class challenges extends AppCompatActivity {

    LinearLayout challengeContainer;

    private ImageView openMenu;
    static class ChallengeItem {
        String title;
        String description;
        boolean isCompleted;

        ChallengeItem(String title, String description) {
            this.title = title;
            this.description = description;
            this.isCompleted = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        openMenu = findViewById(R.id.openmenu);
        challengeContainer = findViewById(R.id.challengeContainer);

        List<ChallengeItem> challenges = Arrays.asList(
                new ChallengeItem("Read A 500-Word Article Or Book", "Spend time reading something informative or inspiring."),
                new ChallengeItem("Write A 100-Word Story Or Journal", "Reflect on your day or create a short story."),
                new ChallengeItem("Practice A 5-Minute Guided Meditation", "Use an app or video to meditate for 5 minutes.")
        );

        for (ChallengeItem challenge : challenges) {
            addChallengeCard(challenge);
        }

        openMenu.setOnClickListener(view -> {
            Intent intent = new Intent(challenges.this, menu.class);
            startActivity(intent);
        });

    }

    private void addChallengeCard(ChallengeItem challenge) {
        TextView challengeView = new TextView(this);
        challengeView.setText(challenge.title);
        challengeView.setTextSize(16f);
        challengeView.setPadding(40, 40, 40, 40);
        challengeView.setBackgroundColor(Color.parseColor("#222222"));
        challengeView.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 30);
        challengeView.setLayoutParams(layoutParams);

        challengeView.setOnClickListener(v -> showChallengePopup(challenge));

        challengeContainer.addView(challengeView);
    }

    private void showChallengePopup(ChallengeItem challenge) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_challenge, null);

        TextView titleView = popupView.findViewById(R.id.popupTitle);
        TextView descView = popupView.findViewById(R.id.popupDescription);
        Button completeButton = popupView.findViewById(R.id.completeButton);

        titleView.setText(challenge.title);
        descView.setText(challenge.description);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        completeButton.setOnClickListener(v -> {
            challenge.isCompleted = true;
            Toast.makeText(this, "Marked as complete!", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        popupWindow.showAtLocation(findViewById(R.id.challengeLayout), Gravity.CENTER, 0, 0);
    }
}
