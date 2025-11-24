package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminProfileScreen extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTitle;
    private TextView tvLogout;

    private TextView tvNameValue;
    private TextView tvEmailValue;
    private TextView tvPwdValue;
    private ImageView imgAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile_screen);

        // === 绑定 XML 中的控件（id 与你的 xml 保持一致）===
        btnBack      = findViewById(R.id.btn_back);
        tvTitle      = findViewById(R.id.tv_title);
        tvLogout     = findViewById(R.id.tv_logout);

        imgAvatar    = findViewById(R.id.img_avatar);
        tvNameValue  = findViewById(R.id.tv_name_value);
        tvEmailValue = findViewById(R.id.tv_email_value);
        tvPwdValue   = findViewById(R.id.tv_pwd_value);

        // === 返回按钮：直接关闭当前页 ===
        btnBack.setOnClickListener(v -> finish());

        // === 右上角登出：这里做一个安全的默认实现 ===
        // 如果你的项目里有 WelcomeActivity，会跳回欢迎页；没有就仅 Toast + finish()
        tvLogout.setOnClickListener(v -> {
            try {
                // TODO: 如接入 Firebase，可在这里 signOut()
                Intent i = new Intent(this, WelcomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.log_out), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // === 显示占位信息（后续接 Firebase 再替换为真实数据）===
        // 头像：你项目里已有 profile.png
        imgAvatar.setImageResource(R.drawable.profile);

        // 名字 / 邮箱：使用 strings.xml 的占位键，兼容本地化并避免硬编码告警
        tvNameValue.setText(getString(R.string.placeholder_name));   // 例如 "Admin John"
        tvEmailValue.setText(getString(R.string.placeholder_email)); // 例如 "admin@example.com"

        // 密码展示为掩码占位（也可放到 strings.xml）
        tvPwdValue.setText("••••••••");
    }
}
