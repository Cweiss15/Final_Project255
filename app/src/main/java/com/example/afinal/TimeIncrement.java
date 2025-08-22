package com.example.afinal;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.CountDownTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TimeIncrement extends AppCompatActivity {
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
        setContentView(R.layout.activity_time_increment);

        setupToolbar();
        setupSessions();
        setupFabs();
    }

    private void setupFabs() {
        FloatingActionButton fabPauseResume = findViewById(R.id.pause_resume);
        FloatingActionButton fabSkip = findViewById(R.id.skip);
        FloatingActionButton fabBack = findViewById(R.id.back);

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
                if (this.schedule != null && !this.schedule.isEmpty()) {
                    if (currentSessionIndex < this.schedule.size() - 1) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        currentSessionIndex++;
                        startSession(this.schedule);
                    } else {
                        Snackbar.make(v, "This is your last session! You got this, no more skips!", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (fabBack != null) {
            fabBack.setOnClickListener(v -> {
                if (this.schedule != null && !this.schedule.isEmpty()) {
                    if (currentSessionIndex > 0) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        currentSessionIndex--;
                        startSession(this.schedule);
                    } else {
                        Snackbar.make(v, "No more previous sessions", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setupSessions() {
        TextView taskName = findViewById(R.id.task_name_and_time);
        String taskNameInput = getIntent().getStringExtra("TASK_NAME");
        int selectedHours = getIntent().getIntExtra("SELECTED_HOURS", 0);
        int selectedMinutes = getIntent().getIntExtra("SELECTED_MINUTES", 0);
        int totalMinutes = getIntent().getIntExtra("TOTAL_MINUTES", 0);

        if (taskName != null) {
            taskName.setText(taskNameInput + "  -  " + selectedHours + "h " + selectedMinutes + "m");
        }

        this.schedule = pomodoroIncrements(totalMinutes);
        if (this.schedule != null && !this.schedule.isEmpty()) {
            startSession(this.schedule);
        } else {
            TextView sessionLabel = findViewById(R.id.session_label);
            TextView timerText = findViewById(R.id.timer_text);
            if (sessionLabel != null) sessionLabel.setText("No Session");
            if (timerText != null) timerText.setText("00:00");
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Timer");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("CURRENT_SESSION_INDEX", currentSessionIndex);
        outState.putLong("TIME_LEFT", timeLeftInMillis);
        outState.putBoolean("IS_TIMER_RUNNING", isTimerRunning);
        outState.putBoolean("IS_PAUSED", isPaused);
        outState.putStringArrayList("SCHEDULE", new ArrayList<>(schedule));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        currentSessionIndex = savedInstanceState.getInt("CURRENT_SESSION_INDEX", 0);
        timeLeftInMillis = savedInstanceState.getLong("TIME_LEFT", 0);
        isTimerRunning = savedInstanceState.getBoolean("IS_TIMER_RUNNING", false);
        isPaused = savedInstanceState.getBoolean("IS_PAUSED", false);
        schedule = savedInstanceState.getStringArrayList("SCHEDULE");

        if (schedule != null && !schedule.isEmpty()) {
            TextView sessionLabel = findViewById(R.id.session_label);
            if (sessionLabel != null) {
                sessionLabel.setText(schedule.get(currentSessionIndex));
            }
            updateTimerText();
            if (isTimerRunning && !isPaused) {
                startTimer();
            } else if (isPaused) {
                updateTimerText();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_enable_skips) {
            item.setChecked(!item.isChecked());
            boolean skipsEnabled = item.isChecked();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean("enable_skips", skipsEnabled).apply();

            FloatingActionButton fabSkip = findViewById(R.id.skip);
            if (fabSkip != null) {
                fabSkip.setVisibility(skipsEnabled ? View.VISIBLE : View.GONE);
            }

            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about_dialog_title));
        builder.setMessage(getString(R.string.about_dialog_message));
        builder.setPositiveButton(getString(R.string.dialog_ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TimeIncrement.this);
                boolean autoStart = prefs.getBoolean("auto_start_next", true);

                if (autoStart) {
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
