package com.s23010269.skill4u;

        import android.content.Intent;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;

        import androidx.appcompat.app.AppCompatActivity;

        import java.util.ArrayList;

public class friends extends AppCompatActivity {

    private TextView myFriendsTab, addFriendsTab, friendRequestTab;
    private LinearLayout myFriendsContainer, addFriendsContainer, friendRequestContainer;
    private ImageView openMenu;
    private String currentUsername;

    private ArrayList<String> myFriends = new ArrayList<>();        // Load from DB ideally
    private ArrayList<String> onlineUsers = new ArrayList<>();      // Load from DB
    private ArrayList<String> incomingRequests = new ArrayList<>(); // Load from DB

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        // Get current username from intent (use uppercase "USERNAME" for consistency)
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null) currentUsername = "";

        dbHelper = new DatabaseHelper(this);
        openMenu = findViewById(R.id.openmenu);
        myFriendsTab = findViewById(R.id.comm_screen);
        addFriendsTab = findViewById(R.id.your_post_screen);
        friendRequestTab = findViewById(R.id.friend_request);

        myFriendsContainer = findViewById(R.id.my_friends_container);
        addFriendsContainer = findViewById(R.id.add_friends_container);
        friendRequestContainer = findViewById(R.id.friend_request_container);

        setupTabListeners();

        loadMyFriends();         // Load actual friends from DB or dummy
        loadIncomingRequests();  // Load requests from DB or dummy
        loadOnlineUsersFromDB(); // Load other users

        showMyFriends();
        populateAllLists();

        openMenu.setOnClickListener(view -> {
            Intent intent = new Intent(friends.this, menu.class);
            startActivity(intent);
        });
    }

    private void loadOnlineUsersFromDB() {
        onlineUsers.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + DatabaseHelper.USER_COL_USERNAME +
                        " FROM " + DatabaseHelper.USER_TABLE +
                        " WHERE " + DatabaseHelper.USER_COL_USERNAME + " != ?",
                new String[]{currentUsername});

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(0);
                if (!myFriends.contains(username)) {
                    onlineUsers.add(username);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void loadMyFriends() {
        myFriends.clear();
        // TODO: Replace with actual DB query to get friends of currentUsername
        myFriends.add("alice");
        myFriends.add("bob");
    }

    private void loadIncomingRequests() {
        incomingRequests.clear();
        // TODO: Replace with actual DB query to get incoming friend requests for currentUsername
        incomingRequests.add("charlie");
    }

    private void setupTabListeners() {
        myFriendsTab.setOnClickListener(v -> showMyFriends());
        addFriendsTab.setOnClickListener(v -> showAddFriends());
        friendRequestTab.setOnClickListener(v -> showFriendRequests());
    }

    private void showMyFriends() {
        myFriendsContainer.setVisibility(View.VISIBLE);
        addFriendsContainer.setVisibility(View.GONE);
        friendRequestContainer.setVisibility(View.GONE);

        highlightTab(myFriendsTab, addFriendsTab, friendRequestTab);
    }

    private void showAddFriends() {
        myFriendsContainer.setVisibility(View.GONE);
        addFriendsContainer.setVisibility(View.VISIBLE);
        friendRequestContainer.setVisibility(View.GONE);

        highlightTab(addFriendsTab, myFriendsTab, friendRequestTab);
    }

    private void showFriendRequests() {
        myFriendsContainer.setVisibility(View.GONE);
        addFriendsContainer.setVisibility(View.GONE);
        friendRequestContainer.setVisibility(View.VISIBLE);

        highlightTab(friendRequestTab, myFriendsTab, addFriendsTab);
    }

    private void highlightTab(TextView selected, TextView... others) {
        selected.setBackgroundResource(R.drawable.selected_tab_bg);
        selected.setTextColor(getResources().getColor(android.R.color.white));
        for (TextView other : others) {
            other.setBackgroundResource(0);
            other.setTextColor(getResources().getColor(android.R.color.white)); // changed to black for contrast
        }
    }

    private void populateAllLists() {
        fillContainer(myFriendsContainer, myFriends, "Unfollow");
        fillContainer(addFriendsContainer, onlineUsers, "Add");
        fillContainer(friendRequestContainer, incomingRequests, "Accept");
    }

    private void fillContainer(LinearLayout container, ArrayList<String> items, String buttonLabel) {
        container.removeAllViews();

        // Copy list to avoid ConcurrentModificationException during removal in listener
        for (String name : new ArrayList<>(items)) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(16, 16, 16, 16);
            row.setBackgroundResource(R.drawable.todo_item_background);

            TextView tv = new TextView(this);
            tv.setText(name);
            tv.setTextSize(18f);
            tv.setTextColor(getResources().getColor(android.R.color.white));
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tv.setLayoutParams(tvParams);

            Button btn = new Button(this);
            btn.setText(buttonLabel);

            btn.setOnClickListener(v -> {
                switch (buttonLabel) {
                    case "Unfollow":
                        myFriends.remove(name);
                        // TODO: Remove friend relation from DB
                        break;
                    case "Add":
                        if (!myFriends.contains(name)) {
                            myFriends.add(name);
                            // TODO: Add friend relation to DB
                        }
                        onlineUsers.remove(name);
                        break;
                    case "Accept":
                        if (!myFriends.contains(name)) {
                            myFriends.add(name);
                            // TODO: Add friend relation to DB and remove request from DB
                        }
                        incomingRequests.remove(name);
                        break;
                }
                populateAllLists();
            });

            row.addView(tv);
            row.addView(btn);

            container.addView(row);
        }
    }
}