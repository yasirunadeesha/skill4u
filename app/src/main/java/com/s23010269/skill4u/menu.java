package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class menu extends AppCompatActivity {

    ImageView backBtn;
    TextView menuHome, menuTodo,menuAchievements, menuPomo, menuChallenges, menuComm, menuLeader, menuAna, menuFriends, menuSkill;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String loggedInUsername = prefs.getString("USERNAME", null);

        // Back button
        backBtn = findViewById(R.id.backbtn);
        backBtn.setOnClickListener(v -> finish());

        menuHome = findViewById(R.id.menu_home);
        menuTodo = findViewById(R.id.menu_todo);
        menuPomo = findViewById(R.id.menu_pomo);
        menuAchievements = findViewById(R.id.menu_achievements);
        menuChallenges = findViewById(R.id.menu_challanges);
        menuComm = findViewById(R.id.menu_comm);
        menuLeader = findViewById(R.id.menu_leader);
        menuAna = findViewById(R.id.menu_ana);
        menuFriends = findViewById(R.id.men_friends);
        menuSkill = findViewById(R.id.menu_skill);

        menuHome.setOnClickListener(v -> startActivity(new Intent(menu.this, friends.class)));
        menuHome.setOnClickListener(v -> startActivity(new Intent(menu.this, home.class)));
        menuPomo.setOnClickListener(v -> startActivity(new Intent(menu.this, pomo.class)));
        menuTodo.setOnClickListener(v -> startActivity(new Intent(menu.this, todo.class)));
        menuSkill.setOnClickListener(v -> startActivity(new Intent(menu.this, skill.class)));
        menuChallenges.setOnClickListener(v -> startActivity(new Intent(menu.this, challenges.class)));
        menuComm.setOnClickListener(v -> startActivity(new Intent(menu.this, community.class)));
        menuLeader.setOnClickListener(v -> startActivity(new Intent(menu.this, lead.class)));
        menuAna.setOnClickListener(v -> startActivity(new Intent(menu.this,  analytics.class)));
        menuAna.setOnClickListener(v -> startActivity(new Intent(menu.this,  achievements.class)));


        menuFriends.setOnClickListener(v -> {
            if (loggedInUsername != null) {
                Intent intent = new Intent(menu.this, friends.class);
                intent.putExtra("USERNAME", loggedInUsername); // use uppercase key
                startActivity(intent);
            } else {
                Toast.makeText(menu.this, "User not logged in", Toast.LENGTH_SHORT).show();

            }
        });


    }
}
