package com.example.afinal;

import android.os.Bundle;
// Import Menu and MenuItem
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast; // For example action

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull; // For @NonNull on onOptionsItemSelected
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// ... other imports ...
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.os.CountDownTimer; // Assuming this is used

import java.util.ArrayList; // Assuming this is used
import java.util.List; // Assuming this is used
import java.util.Locale; // Assuming this is used


public class TimeIncrement extends AppCompatActivity {
    // ... your existing class variables ...
    final int WORK_DURATION = 25;
    final int SHORT_BREAK = 5;
    final int LONG_BREAK = 15;
    int timeLeft;
    private List<String> schedule;
    private int currentSessionIndex = 0;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean isTimerRunning = false;
    private boolean isPaused = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_time_increment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Timer");
        }

        View mainContentView = findViewById(R.id.main);
        if (mainContentView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContentView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        int totalMinutes = getIntent().getIntExtra("TOTAL_MINUTES", 0);
        this.schedule = pomodoroIncrements(totalMinutes);
        if (this.schedule != null && !this.schedule.isEmpty()) {
            startSession(this.schedule);
        } else {
            TextView sessionLabel = findViewById(R.id.session_label);
            TextView timerText = findViewById(R.id.timer_text);
            if (sessionLabel != null) sessionLabel.setText("No Session");
            if (timerText != null) timerText.setText("00:00");
        }

        FloatingActionButton fabPauseResume = findViewById(R.id.pause_resume);
        FloatingActionButton fabSkip = findViewById(R.id.skip);

        if (fabPauseResume != null) {
            fabPauseResume.setOnClickListener(v -> {
                if (isTimerRunning) {
                    pauseTimer();
                } else {
                    resumeTimer();
                }
            });
        }

        if (fabSkip != null) {
            fabSkip.setOnClickListener(v -> {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                currentSessionIndex++;
                if (this.schedule != null && !this.schedule.isEmpty()) {
                    startSession(this.schedule);
                }
            });
        }
    }

    // --- Add these methods for the menu ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Handle settings action
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_about) {
            // Handle about action
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // --- End of menu methods ---


    // ... your other methods (pomodoroIncrements, startSession, etc.) ...
    private List<String> pomodoroIncrements(int totalMinutes) {
        schedule = new ArrayList<>();
        int sessionCount = 0;
        timeLeft = totalMinutes;
        while (timeLeft >= WORK_DURATION) {
            schedule.add("Work");
            timeLeft -= WORK_DURATION;
            sessionCount++;

            if (timeLeft > 0) {
                if (sessionCount % 4 == 0) {
                    schedule.add("Extended Break");
                } else {
                    schedule.add("Break");
                }
            }
        }
        if (timeLeft > 0) {
            schedule.add("Final Work");
        }
        return schedule;
    }

    private void startSession(List<String> schedule) {
        if (currentSessionIndex >= schedule.size()) return;
        String session = schedule.get(currentSessionIndex);
        TextView sessionLabel = findViewById(R.id.session_label);
        if (sessionLabel != null) {
            sessionLabel.setText(session);
        }
        int durationMinutes = extractMinutes(session);
        timeLeftInMillis = durationMinutes * 60 * 1000L;
        startTimer();
    }

    private int extractMinutes(String session) {
        if (session.contains("Final Work")) {
            return timeLeft;
        } else if (session.contains("Work")) {
            return WORK_DURATION;
        } else if (session.contains("Extended Break")) {
            return LONG_BREAK;
        } else if (session.contains("Break")) {
            return SHORT_BREAK;
        } else {
            return 0;
        }
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }
            @Override
            public void onFinish() {
                isTimerRunning = false;
                currentSessionIndex++;
                if (TimeIncrement.this.schedule != null && !TimeIncrement.this.schedule.isEmpty()) {
                    startSession(TimeIncrement.this.schedule);
                }
            }
        }.start();
        isTimerRunning = true;
        isPaused = false;
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        isPaused = true;
    }

    private void resumeTimer() {
        // Only resume if it was paused and there's time left
        if (isPaused && timeLeftInMillis > 0) {
            startTimer();
        }
    }

    private void updateTimerText() {
        long minutes = timeLeftInMillis / 60000;
        long seconds = (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        TextView timerText = findViewById(R.id.timer_text);
        if (timerText != null) {
            timerText.setText(timeFormatted);
        }
    }
}
