package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.view.View;
import com.example.afinal.databinding.ActivityMainBinding;

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

        Intent intent = new Intent(MainActivity.this, TimeIncrement.class);
        intent.putExtra("TOTAL_MINUTES", totalTime);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}