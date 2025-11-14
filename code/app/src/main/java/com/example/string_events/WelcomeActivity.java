
package com.example.string_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        MaterialButton signInButton = findViewById(R.id.btnSignIn);
        MaterialButton registerButton = findViewById(R.id.btnRegister);

        signInButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
        });

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterScreen.class));
            finish();
        });
    }

    @Override protected void onStart() {
        super.onStart();
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        boolean remember = sp.getBoolean("remember", false);
        if (remember) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
        }
    }
}
