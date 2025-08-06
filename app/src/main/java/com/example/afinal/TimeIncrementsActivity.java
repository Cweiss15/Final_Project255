package com.example.afinal;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.example.afinal.R;

public class TimeIncrementsActivity extends AppCompatActivity {

    final int WORK_DURATION = 25;
    final int SHORT_BREAK = 5;
    final int LONG_BREAK = 15;
    int timeLeft;
    private List<String> schedule;
    private int currentSessionIndex=0;
    private CountDownTimer countDownTimer;

    private long timeLeftInMillis;

    private boolean isTimerRunning = false;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_time_increments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        int totalMinutes = getIntent().getIntExtra("TOTAL_MINUTES", 0);
        List<String> schedule = pomodoroIncrements(totalMinutes);
        startSession(schedule);

        FloatingActionButton fabPauseResume = findViewById(R.id.pause_resume);
        FloatingActionButton fabSkip = findViewById(R.id.skip);

        fabPauseResume.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                resumeTimer();
            }
        });

        fabSkip.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            currentSessionIndex++;
            startSession(schedule);
        });
    }


    private List<String> pomodoroIncrements(int totalMinutes) {


        schedule = new ArrayList<>();
        int sessionCount = 0;
        timeLeft =totalMinutes;
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
        sessionLabel.setText(session);

        int durationMinutes = extractMinutes(session);
        timeLeftInMillis = durationMinutes * 60 * 1000L;
        startTimer();
    }

    private int extractMinutes(String session) {
        if (session.contains("Final Work")) {
            // Extract final custom work session at the end like "Final Work: 7 min"
            return timeLeft;
        } else if (session.contains("Work")) {
            return WORK_DURATION;
        } else if (session.contains("Extended Break")) {
            return LONG_BREAK;
        } else if (session.contains("Break")) {
            return SHORT_BREAK;
        } else {
            return 0;  // fallback
        }
    }

    private void startTimer() {
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
                startSession(schedule);
            }
        }.start();

        isTimerRunning = true;
        isPaused = false;
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        isTimerRunning = false;
        isPaused = true;
    }

    private void resumeTimer() {
        startTimer(); // Resumes using timeLeftInMillis
    }

    private void updateTimerText() {
        long minutes = timeLeftInMillis / 60000;
        long seconds = (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        TextView timerText = findViewById(R.id.timer_text);
        timerText.setText(timeFormatted);
    }
}