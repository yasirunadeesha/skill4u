package com.s23010269.skill4u;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

// adapter to display achievements in a ListView
public class AchievementAdapter extends ArrayAdapter<achievements.Achievement> {

    private final Context context;
    private final List<achievements.Achievement> achievementList; // List of achievements to show

    // Constructor
    public AchievementAdapter(Context context, List<achievements.Achievement> list) {
        super(context, 0, list);
        this.context = context;
        this.achievementList = list;
    }

    // Get view for each item in the ListView
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        achievements.Achievement achievement = achievementList.get(position); // Current achievement

        // Inflate layout if not already created
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.achievement_item, parent, false);
        }

        // Find views in the layout
        TextView titleTextView = convertView.findViewById(R.id.titleTextView);
        TextView statusTextView = convertView.findViewById(R.id.statusTextView);
        TextView pointsTextView = convertView.findViewById(R.id.pointsTextView);

        // Set values in the views
        titleTextView.setText(achievement.title); // Achievement title
        statusTextView.setText(achievement.isCompleted ? "✅ Completed" : "❌ Incomplete"); // Status
        pointsTextView.setText("Reward: " + achievement.pointReward + " pts"); // Reward points

        return convertView; // Return the completed view
    }
}
