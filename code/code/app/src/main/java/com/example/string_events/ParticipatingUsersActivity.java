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

public class ParticipatingUsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participating_users);

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        ListView list = findViewById(R.id.listParticipating);
        list.setAdapter(new UserAdapter(this, mockUsers(UserItem.Status.PARTICIPATING)));

        findViewById(R.id.btnSendParticipating).setOnClickListener(v ->
                Toast.makeText(this, "Message sent to participating users (demo)", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btnExportParticipating).setOnClickListener(v ->
                Toast.makeText(this, "Exported participating users (demo)", Toast.LENGTH_SHORT).show()
        );
    }

    private List<UserItem> mockUsers(UserItem.Status status) {
        List<UserItem> data = new ArrayList<>();
        data.add(new UserItem("Daisy", "daisy@mail.com", status));
        data.add(new UserItem("Edward", "edward@mail.com", status));
        data.add(new UserItem("Fiona", "fiona@mail.com", status));
        return data;
    }
}