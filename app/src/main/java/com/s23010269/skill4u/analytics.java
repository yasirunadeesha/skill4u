package com.s23010269.skill4u;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class analytics extends AppCompatActivity {

    private ImageView openMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        openMenu = findViewById(R.id.openmenu);
        openMenu.setOnClickListener(view -> {
            Intent intent = new Intent(analytics.this, menu.class);
            startActivity(intent);
        });
    }
}