package com.example.gestionfacult;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnExplore; // rename

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnExplore = findViewById(R.id.btnExplore); // match XML

        String user = "Student";
        Bundle b = getIntent().getExtras();
        if (b != null) {
            user = b.getString(LoginActivity.EXTRA_EMAIL, user);
        }
        tvWelcome.setText("Welcome, " + user + "👋");

        btnExplore.setOnClickListener(this::navigateToMenu); // keep behavior
    }

    private void navigateToMenu(View view) {
        Intent intent = new Intent(WelcomeActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_students) {
            Toast.makeText(this, "Students clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            return true;
        }
        return false;
    }
}


