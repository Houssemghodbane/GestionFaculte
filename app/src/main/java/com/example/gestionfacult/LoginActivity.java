package com.example.gestionfacult;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private LinearLayout loadingContainer;
    private ProgressBar progressBar;
    private TextView tvStatus;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isLoading = false;

    // Progress controls (time driven)
    private int progress = 0;
    private static final int TICK_MS = 50;      // update cadence
    private static final int TOTAL_MS = 1200;   // total "server" duration

    // Timing bookkeeping
    private long loadingStartUptime = 0L;       // when loading started
    private long pauseStartUptime = 0L;         // when pause began
    private long totalPausedMs = 0L;            // total paused time accumulated

    private String pendingEmailForNextScreen = null;

    private final Runnable progressTick = new Runnable() {
        @Override public void run() {
            if (!isLoading) return;

            long now = SystemClock.uptimeMillis();
            long effectiveElapsed = now - loadingStartUptime - totalPausedMs;
            effectiveElapsed = Math.max(0, effectiveElapsed);

            int newProgress = (int) Math.min(100,
                    Math.round(100.0 * effectiveElapsed / TOTAL_MS));

            if (newProgress != progress) {
                progress = newProgress;
                progressBar.setProgress(progress);
                tvStatus.setText("Loading… " + progress + "%");
            }

            if (progress >= 100) {
                // Reached the goal: stop and navigate
                setLoading(false);
                goToWelcome(pendingEmailForNextScreen);
                return;
            }

            handler.postDelayed(this, TICK_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loadingContainer = findViewById(R.id.loadingContainer);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        btnLogin.setOnClickListener(v -> onLoginClicked());
    }

    private void onLoginClicked() {
        if (isLoading) return;

        String email = textOf(etEmail);
        String pass  = textOf(etPassword);

        if (!isValidEmail(email)) {
            etEmail.setError("Invalid email");
            etEmail.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            etPassword.setError("Min 6 characters");
            etPassword.requestFocus();
            return;
        }

        pendingEmailForNextScreen = email;
        setLoading(true);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        btnLogin.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        loadingContainer.setVisibility(loading ? View.VISIBLE : View.GONE);

        if (loading) {
            // Reset timing + progress
            loadingStartUptime = SystemClock.uptimeMillis();
            pauseStartUptime = 0L;
            totalPausedMs = 0L;
            progress = 0;
            progressBar.setProgress(progress);
            tvStatus.setText("Loading… 0%");

            handler.removeCallbacks(progressTick);
            handler.postDelayed(progressTick, TICK_MS);
        } else {
            handler.removeCallbacks(progressTick);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isLoading) {
            // Freeze time & ticks
            pauseStartUptime = SystemClock.uptimeMillis();
            handler.removeCallbacks(progressTick);
            tvStatus.setText("Paused at " + progress + "%");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoading) {
            // Accumulate paused time and resume ticking
            if (pauseStartUptime != 0L) {
                totalPausedMs += SystemClock.uptimeMillis() - pauseStartUptime;
                pauseStartUptime = 0L;
            }
            handler.postDelayed(progressTick, TICK_MS);
            tvStatus.setText("Resumed… " + progress + "%");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Defensive: ensure no stray ticks when Activity not visible
        handler.removeCallbacks(progressTick);
    }

    private void goToWelcome(String email) {
        Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
        Bundle data = new Bundle();
        data.putString(EXTRA_EMAIL, email);
        intent.putExtras(data);
        startActivity(intent);
        finish();
    }

    private boolean isValidEmail(String s) {
        return !TextUtils.isEmpty(s) && Patterns.EMAIL_ADDRESS.matcher(s).matches();
    }

    private String textOf(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
