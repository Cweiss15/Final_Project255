package com.example.afinal;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import com.example.afinal.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private NumberPicker hourPicker;

    private NumberPicker minutePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(this::handleFabClick);

        hourPicker = findViewById(R.id.hour_picker);
        minutePicker = findViewById(R.id.minute_picker);

        setupTimePickers();
    }

    private void setupTimePickers() {
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(12);
        hourPicker.setValue(0);
        hourPicker.setWrapSelectorWheel(true);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(0);
        minutePicker.setWrapSelectorWheel(true);
    }

    private void handleFabClick(View view) {
        int selectedHours = hourPicker.getValue();
        int selectedMinutes = minutePicker.getValue();
        int totalTime = selectedHours * 60 + selectedMinutes;
        List<String> schedule = pomodoroIncrements(totalTime);
        StringBuilder message = new StringBuilder("Pomodoro Plan:\n");
        for (String s : schedule) {
            message.append(s).append("\n");
        }

        showPomodoroDialog(message.toString());
    }

        private List<String> pomodoroIncrements(int totalMinutes) {
            final int WORK_DURATION = 25;
            final int SHORT_BREAK = 5;
            final int LONG_BREAK = 15;

            List<String> schedule = new ArrayList<>();
            int timeLeft = totalMinutes;
            int sessionCount = 0;

            while (timeLeft >= WORK_DURATION) {
                schedule.add("Work: 25 min");
                timeLeft -= WORK_DURATION;
                sessionCount++;

                if (timeLeft > 0) {
                    if (sessionCount % 4 == 0) {
                        schedule.add("Long Break: 15 min");
                    } else {
                        schedule.add("Short Break: 5 min");
                    }
                }
            }

            if (timeLeft > 0) {
                schedule.add("Final Work: " + timeLeft + " min");
            }

            return schedule;
        }

    private void showPomodoroDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Pomodoro Plan")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}