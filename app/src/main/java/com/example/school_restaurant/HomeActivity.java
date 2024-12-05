package com.example.school_restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialisation des boutons
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignup = findViewById(R.id.btnSignup);

        // Navigation vers l'écran de connexion
        btnLogin.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Navigation vers l'écran d'inscription
        btnSignup.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
