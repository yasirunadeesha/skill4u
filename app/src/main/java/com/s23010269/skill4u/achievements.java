package com.s23010269.skill4u;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class achievements extends AppCompatActivity {

    private ImageView openMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openMenu = findViewById(R.id.openmenu);
        setContentView(R.layout.activity_achievements);


        openMenu.setOnClickListener(view -> {
            Intent intent = new Intent(achievements.this, menu.class);
            startActivity(intent);
        });
    }
}