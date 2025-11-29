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

    /**
     * Checks the persisted "remember" preference and redirects to {@link MainActivity}
     * when enabled; otherwise stays on the welcome screen.
     */
    @Override protected void onStart() {
        super.onStart();

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);

        boolean remember = sp.getBoolean("remember", false);
        String role = sp.getString("role", null);
        String adminUsername = sp.getString("adminUsername", null);

        if (remember && role != null) {
            Intent intent;
            // the saved login was for an admin
            if ("admin".equals(role)) {
                intent = new Intent(this, AdminDashboardActivity.class);
                if (adminUsername != null) {
                    intent.putExtra("adminUsername", adminUsername);
                }
            } else {
                // reset the user to an entrant and open events screen
                sp.edit().putString("role", "entrant").apply();
                intent = new Intent(this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        }
    }
}
