package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);

        // 事件管理按钮 → AdminEventManagementActivity
        ImageButton btnEvents = findViewById(R.id.btnEvents);
        btnEvents.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventManagementActivity.class))
        );

        // 图片管理按钮 → ImageManagementActivity
        ImageButton btnImages = findViewById(R.id.btnImages);
        btnImages.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, ImageManagementActivity.class))
        );

        // 用户资料管理按钮 → ProfileScreen
        ImageButton btnProfiles = findViewById(R.id.btnProfiles);
        btnProfiles.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, ProfileScreen.class))
        );

        // 通知日志按钮 → NotificationScreen
        ImageButton btnNotifLog = findViewById(R.id.btnNotifLog);
        btnNotifLog.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, NotificationScreen.class))
        );

        // 管理员个人资料按钮 → ProfileScreen（或 AdminProfileScreen）
        ImageButton btnMyProfile = findViewById(R.id.btnMyProfile);
        btnMyProfile.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfileScreen.class))
        );
    }
}
