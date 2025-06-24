package com.s23010269.skill4u;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class lead extends AppCompatActivity {

    private ListView leaderboardDetails;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lead);


        leaderboardDetails = findViewById(R.id.leaderboard_details);
        databaseHelper = new DatabaseHelper(this);


        ArrayList<String> userTimes = databaseHelper.getActivityTimesByUser();


        if (userTimes.isEmpty()) {
            userTimes.add("No data available");
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userTimes);
        leaderboardDetails.setAdapter(adapter);
    }
}
