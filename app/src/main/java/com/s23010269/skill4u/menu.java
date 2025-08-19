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
    TextView menuHome, menuTodo, menuAchievements, menuPomo, menuChallenge, menuComm, menuLeader, menuAna, menuFriend, menuSkill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        // Back button
        backBtn = findViewById(R.id.backbtn);
        backBtn.setOnClickListener(v -> finish());

        // Menu item references
        menuHome = findViewById(R.id.menu_home);
//        menuTodo = findViewById(R.id.menu_todo);
        menuPomo = findViewById(R.id.menu_pomo);
        menuAchievements = findViewById(R.id.menu_achievements);
        menuChallenge = findViewById(R.id.menu_challange);
        menuComm = findViewById(R.id.menu_comm);
        menuLeader = findViewById(R.id.menu_leader);
        menuAna = findViewById(R.id.menu_ana);
        menuFriend = findViewById(R.id.menu_friend);
        menuSkill = findViewById(R.id.menu_skill);

        // Home
        menuHome.setOnClickListener(v -> startActivity(new Intent(menu.this, home.class)));

        // Pomo
        menuPomo.setOnClickListener(v -> startActivity(new Intent(menu.this, pomo.class)));

        menuFriend.setOnClickListener(v -> startActivity(new Intent(menu.this, friend.class)));

        // Skill Buddy
        menuSkill.setOnClickListener(v -> startActivity(new Intent(menu.this, skill.class)));

        // You can uncomment and update these when needed:
        // menuTodo.setOnClickListener(v -> startActivity(new Intent(menu.this, todo.class)));
         menuAchievements.setOnClickListener(v -> startActivity(new Intent(menu.this, achievements.class)));
         menuChallenge.setOnClickListener(v -> startActivity(new Intent(menu.this, ChallengeActivity.class)));
        menuComm.setOnClickListener(v -> startActivity(new Intent(menu.this, community.class)));
        menuLeader.setOnClickListener(v -> startActivity(new Intent(menu.this, leaderboard.class)));
         menuAna.setOnClickListener(v -> startActivity(new Intent(menu.this, analytics.class)));
    }
}
