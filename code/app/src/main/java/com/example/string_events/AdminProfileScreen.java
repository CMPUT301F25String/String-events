package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Screen that displays the admin's profile information and provides basic actions:
 * back navigation and logout to the welcome screen.
 */
public class AdminProfileScreen extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTitle;
    private TextView tvLogout;

    private TextView tvNameValue;
    private TextView tvEmailValue;
    private TextView tvPwdValue;
    private ImageView imgAvatar;

    /**
     * Initializes the profile UI, wires click handlers, and populates placeholder data.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile_screen);


        btnBack      = findViewById(R.id.btn_back);
        tvTitle      = findViewById(R.id.tv_title);
        tvLogout     = findViewById(R.id.tv_logout);

        imgAvatar    = findViewById(R.id.img_avatar);
        tvNameValue  = findViewById(R.id.tv_name_value);
        tvEmailValue = findViewById(R.id.tv_email_value);
        tvPwdValue   = findViewById(R.id.tv_pwd_value);


        btnBack.setOnClickListener(v -> finish());



        tvLogout.setOnClickListener(v -> {
            try {

                Intent i = new Intent(this, WelcomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.log_out), Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        imgAvatar.setImageResource(R.drawable.profile);


        tvNameValue.setText(getString(R.string.placeholder_name));
        tvEmailValue.setText(getString(R.string.placeholder_email));


        tvPwdValue.setText("••••••••");
    }
}
