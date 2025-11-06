package com.example.string_events;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 ListView 展示三张卡片：
 * 1. In Progress（黄色）
 * 2. Scheduled（绿色）
 * 3. Finished（红色）
 */
public class AdminEventManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_management_screen);

        TextView title = findViewById(R.id.tv_title);
        title.setText(getString(R.string.event_management));

        ImageButton back = findViewById(R.id.btn_back);
        back.setOnClickListener(v -> finish());

        ListView listView = findViewById(R.id.list_admin_events);

        // 准备数据（使用你项目已有的资源名）
        List<AdminEvent> data = new ArrayList<>();
        data.add(new AdminEvent(
                "Badminton Drop In",
                "11:00 am",
                getString(R.string.sample_location),
                "XYZ",
                AdminEvent.Status.IN_PROGRESS,
                R.drawable.sample_event,
                R.drawable.community_centre_image
        ));
        data.add(new AdminEvent(
                "Badminton Drop In",
                "11:00 am",
                getString(R.string.sample_location),
                "XYZ",
                AdminEvent.Status.SCHEDULED,
                R.drawable.sample_event,
                R.drawable.community_centre_image
        ));
        data.add(new AdminEvent(
                "Badminton Drop In",
                "11:00 am",
                getString(R.string.sample_location),
                "XYZ",
                AdminEvent.Status.FINISHED,
                R.drawable.sample_event,
                R.drawable.community_centre_image
        ));

        // 绑定 ListView
        AdminEventAdapter adapter = new AdminEventAdapter(this, data);
        listView.setAdapter(adapter);
    }
}
