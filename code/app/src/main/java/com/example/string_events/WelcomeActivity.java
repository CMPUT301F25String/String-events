package com.example.string_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

/**
 * Entry screen offering navigation to sign-in or registration.
 * <p>
 * On start, optionally auto-redirects to {@link MainActivity} if a persisted
 * "remember" flag is present in {@code SharedPreferences} ("auth").
 */
public class WelcomeActivity extends AppCompatActivity {

    /**
     * Inflates the welcome layout and wires primary CTA buttons for sign-in and registration.
     *
     * @param savedInstanceState previously saved state, or {@code null}
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        ((MaterialButton) findViewById(R.id.btnSignIn))
                .setOnClickListener(v -> startActivity(new Intent(this, LoginScreen.class)));
        ((MaterialButton) findViewById(R.id.btnRegister))
                .setOnClickListener(v -> startActivity(new Intent(this, RegisterScreen.class)));
    }

    /**
     * Checks the persisted "remember" preference and redirects to {@link MainActivity}
     * when enabled; otherwise stays on the welcome screen.
     */
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
