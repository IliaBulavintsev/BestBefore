package com.rv150.bestbefore.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.rv150.bestbefore.R;

import java.util.Calendar;

/**
 * Created by ivan on 27.04.17.
 */

public class CalculatorActivity extends AppCompatActivity {

    private final Calendar currentDate = Calendar.getInstance();
    private Spinner spinnerStorageLife;
    private TextView result;
    private int currentTerm = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        setTitle(R.string.calculator);

        result = (TextView) findViewById(R.id.result);

        Calendar now = Calendar.getInstance();
        DatePicker datePicker = (DatePicker) findViewById(R.id.date);
        datePicker.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        currentDate.set(year, monthOfYear, dayOfMonth);
                        makeCalculations();
                    }
                });

        spinnerStorageLife = (Spinner)findViewById(R.id.spinner_storage_life);
        String[] terms = new String[]{
                getString(R.string.days_in_add_act),
                getString(R.string.months_in_add_act)};
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, R.layout.custom_xml_spinner_layout, terms);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStorageLife.setAdapter(spinnerAdapter);

        spinnerStorageLife.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                makeCalculations();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        EditText input = (EditText) findViewById(R.id.best_before);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    currentTerm = -1;
                }
                else {
                    currentTerm = Integer.valueOf(s.toString());
                }
                makeCalculations();
            }
        });
    }


    private void makeCalculations() {
        if (currentTerm == -1) {
            result.setText("");
            return;
        }
        String textSpinner = spinnerStorageLife.getSelectedItem().toString();
        boolean isDays = textSpinner.equals(getString(R.string.days_in_add_act));

        Calendar resultDate = (Calendar) currentDate.clone();
        if (isDays) {
            resultDate.add(Calendar.DAY_OF_MONTH, currentTerm);
        } else {
            resultDate.add(Calendar.MONTH, currentTerm);
        }

        int days = getDaysLeft(resultDate);

        String resultText;
        if (days < 0) {
            days *= -1;
            resultText = getResources().getQuantityString(R.plurals.calculatorSpoil, days, days);
        } else if (days > 0){
            resultText = getResources().getQuantityString(R.plurals.calculatorFresh, days, days);
        }
        else {
            resultText = getString(R.string.today_is_the_last_day);
        }
        result.setText(resultText);
    }


    private int getDaysLeft (Calendar sourceDate) {
        Calendar currentDate = Calendar.getInstance();
        long difference = sourceDate.getTimeInMillis() - currentDate.getTimeInMillis();
        int days = (int) (difference / (24 * 60 * 60 * 1000));
        if (days < 0) {
            days--;
        }
        if (days == 0) {
            if (difference < 0) {
                return -1;        // В первый день просрочки
            }
            return 0;   // Последний день съесть
        }
        return days;
    }
}
