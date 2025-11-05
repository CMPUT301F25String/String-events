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

public class CanceledUsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canceled_users);

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        ListView list = findViewById(R.id.listCanceled);
        list.setAdapter(new UserAdapter(this, mockUsers(UserItem.Status.CANCELED)));

        findViewById(R.id.btnSendCanceled).setOnClickListener(v ->
                Toast.makeText(this, "Message sent to canceled users (demo)", Toast.LENGTH_SHORT).show()
        );
    }

    private List<UserItem> mockUsers(UserItem.Status status) {
        List<UserItem> data = new ArrayList<>();
        data.add(new UserItem("Alice", "alice@mail.com", status));
        data.add(new UserItem("Bob", "bob@mail.com", status));
        data.add(new UserItem("Charlie", "charlie@mail.com", status));
        return data;
    }
}