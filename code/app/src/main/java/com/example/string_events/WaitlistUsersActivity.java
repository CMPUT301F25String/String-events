package com.example.string_events;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.string_events.UserAdapter;
import com.example.string_events.UserItem;

import java.util.ArrayList;
import java.util.List;

public class WaitlistUsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_users);

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        ListView list = findViewById(R.id.listWaitlist);
        list.setAdapter(new UserAdapter(this, mockUsers(UserItem.Status.WAITLIST)));

        findViewById(R.id.btnSendWaitlist).setOnClickListener(v ->
                Toast.makeText(this, "Message sent to waitlist users (demo)", Toast.LENGTH_SHORT).show()
        );
    }

    private List<UserItem> mockUsers(UserItem.Status status) {
        List<UserItem> data = new ArrayList<>();
        data.add(new UserItem("Grace", "grace@mail.com", status));
        data.add(new UserItem("Henry", "henry@mail.com", status));
        data.add(new UserItem("Ivy", "ivy@mail.com", status));
        return data;
    }
}
