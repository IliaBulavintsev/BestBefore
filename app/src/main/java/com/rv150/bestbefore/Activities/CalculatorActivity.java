package com.rv150.bestbefore.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.rv150.bestbefore.R;

import java.util.Calendar;

/**
 * Created by ivan on 27.04.17.
 */

public class CalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        setTitle(R.string.calculator);

        Calendar now = Calendar.getInstance();

        final TextView result = (TextView) findViewById(R.id.result);

        DatePicker datePicker = (DatePicker) findViewById(R.id.date);
        datePicker.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        result.setText(String.valueOf(year + monthOfYear + dayOfMonth));
                    }
                });

        Spinner spinnerStorageLife = (Spinner)findViewById(R.id.spinner_storage_life);
        String[] terms = new String[]{
                getString(R.string.days_in_add_act),
                getString(R.string.months_in_add_act)};
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, R.layout.custom_xml_spinner_layout, terms);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStorageLife.setAdapter(spinnerAdapter);
    }
}
