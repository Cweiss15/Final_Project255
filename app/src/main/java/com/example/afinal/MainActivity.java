package com.example.afinal;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import com.example.afinal.databinding.ActivityMainBinding;
import com.google.android.material.textfield.TextInputEditText;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the inputs
        TextInputEditText inputField = findViewById(R.id.input_text);
        outState.putString("TASK_NAME", inputField.getText().toString());
        outState.putInt("HOUR_VALUE", hourPicker.getValue());
        outState.putInt("MINUTE_VALUE", minutePicker.getValue());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the inputs
        if (savedInstanceState != null) {
            String taskName = savedInstanceState.getString("TASK_NAME", "");
            int hour = savedInstanceState.getInt("HOUR_VALUE", 0);
            int minute = savedInstanceState.getInt("MINUTE_VALUE", 0);

            TextInputEditText inputField = findViewById(R.id.input_text);
            inputField.setText(taskName);
            hourPicker.setValue(hour);
            minutePicker.setValue(minute);
        }
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

        TextInputEditText inputField = findViewById(R.id.input_text);
        String taskNameInput = inputField.getText().toString().trim();
        if (taskNameInput.isEmpty()) {
            Snackbar.make(view, "Please enter a task name", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (totalTime <= 0) {
            Snackbar.make(view, "Please enter a valid task time", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, TimeIncrement.class);
        intent.putExtra("TOTAL_MINUTES", totalTime);
        intent.putExtra("SELECTED_HOURS", selectedHours);
        intent.putExtra("SELECTED_MINUTES", selectedMinutes);
        intent.putExtra("TASK_NAME", taskNameInput);
        startActivity(intent);
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

        if (id == R.id.action_auto_start) {
            item.setChecked(!item.isChecked());
            boolean autoStart = item.isChecked();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean("auto_start_next", autoStart).apply();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}