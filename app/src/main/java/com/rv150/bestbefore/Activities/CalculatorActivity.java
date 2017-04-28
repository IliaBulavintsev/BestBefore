package com.rv150.bestbefore.Activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Services.StatService;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ivan on 27.04.17.
 */

public class CalculatorActivity extends AppCompatActivity {
    private final Calendar currentDate;

    {
        currentDate = Calendar.getInstance();
        currentDate.set(Calendar.HOUR_OF_DAY, 23);
        currentDate.set(Calendar.MINUTE, 59);
    }

    private Spinner spinnerStorageLife;
    private TextView result;
    private TextView resultBestBefore;
    private int currentTerm = -1;

    private int visits;

    private static final String CALC_VISITS = "calculator_visits";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        setTitle(R.string.calculator);

        result = (TextView) findViewById(R.id.result);
        resultBestBefore = (TextView) findViewById(R.id.result_best_before);
        TextView help = (TextView) findViewById(R.id.help);

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


        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        visits = sPrefs.getInt(CALC_VISITS, 0);
        visits++;
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putInt(CALC_VISITS, visits);
        editor.apply();

        if (visits < 4) {
            help.setVisibility(View.VISIBLE);
        }

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

        StatService.openCalculator(this, visits);
    }



    private void makeCalculations() {
        if (currentTerm == -1) {
            result.setText("");
            resultBestBefore.setText("");
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
            result.setTextColor(getResources().getColor(R.color.md_red_500));
        } else if (days > 0){
            resultText = getResources().getQuantityString(R.plurals.calculatorFresh, days, days);
            if (visits < 4) {
                resultText += '*';
            }
            if (days <= 2) {
                result.setTextColor(Color.rgb(220, 180, 0));
            }
            else {
                result.setTextColor(getResources().getColor(R.color.md_green_500));
            }
        }
        else {
            resultText = getString(R.string.today_is_the_last_day);
            result.setTextColor(Color.rgb(220, 180, 0));
        }
        result.setText(resultText);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        resultBestBefore.setText(dateFormat.format(resultDate.getTime()));
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
            return 0;   // Последний день срока
        }
        return days;
    }
}