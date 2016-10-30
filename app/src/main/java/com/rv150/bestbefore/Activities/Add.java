package com.rv150.bestbefore.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Resources;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by rv150 on 07.01.2016.
 */
public class Add extends AppCompatActivity {
    private TextView chooseDate;
    private TextView chooseDate2;
    private TextView bestBefore;

    private EditText enterName;
    private TextView date;
    private EditText days;
    private EditText quantity;
    private RadioButton radio1;
    private Spinner spinner;

    Calendar currentData = new GregorianCalendar();
    private int DIALOG_DATE = 1;
    private int myYear = currentData.get(Calendar.YEAR);
    private int myMonth = currentData.get(Calendar.MONTH);
    private int myDay = currentData.get(Calendar.DAY_OF_MONTH);

    private boolean isChanging = false;

    private int DayCreated;
    private int MonthCreated;
    private int YearCreated;
    private int HourCreated;
    private int MinuteCreated;
    private  int SecondCreated;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);
        TextView name = (TextView)findViewById(R.id.name);
        chooseDate = (TextView)findViewById(R.id.chooseDate);
        chooseDate2 = (TextView)findViewById(R.id.chooseDate2);
        bestBefore = (TextView)findViewById(R.id.bestBefore);

        spinner = (Spinner)findViewById(R.id.spinner);
        String[] items = new String[]{
                getString(R.string.days_in_add_act),
                getString(R.string.months_in_add_act)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_xml_spinner_layout,items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        enterName = (EditText)findViewById(R.id.enterName);
        date = (TextView)findViewById(R.id.date);
        days = (EditText)findViewById(R.id.days);
        quantity = (EditText) findViewById(R.id.enterQuantity);
        quantity.setText("1");

        Typeface font = Typeface.createFromAsset(getAssets(), "san.ttf");
        name.setTypeface(font);
        chooseDate.setTypeface(font);
        chooseDate2.setTypeface(font);
        bestBefore.setTypeface(font);

        enterName.setTypeface(font);
        date.setTypeface(font);
        days.setTypeface(font);
        quantity.setTypeface(font);

        if (myMonth < 9) {
            date.setText(myDay + "." + "0" + (myMonth + 1) + "." + myYear);
        }
        else {
            date.setText(myDay + "." + (myMonth + 1) + "." + myYear);
        }
        radio1 = (RadioButton)findViewById(R.id.radioButton1);
        RadioButton radio2 = (RadioButton)findViewById(R.id.radioButton2);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {                       // Изменение
            isChanging = true;
            setTitle(R.string.changing_product);
            String nameStr = extras.getString("name");
            enterName.setText(nameStr);
            myDay = extras.getInt(Resources.MY_DAY);
            myMonth = extras.getInt(Resources.MY_MONTH);
            myYear = extras.getInt(Resources.MY_YEAR);

            DayCreated = extras.getInt(Resources.DAY_CREATED);
            MonthCreated = extras.getInt(Resources.MONTH_CREATED);
            YearCreated = extras.getInt(Resources.YEAR_CREATED);
            HourCreated = extras.getInt(Resources.HOUR_CREATED);
            MinuteCreated = extras.getInt(Resources.MINUTE_CREATED);
            SecondCreated = extras.getInt(Resources.SECOND_CREATED);

            int quantityInt = extras.getInt(Resources.QUANTITY);


            if (myMonth < 9) {
                date.setText(myDay + "." + "0" + (myMonth + 1) + "." + myYear);
            }
            else {
                date.setText(myDay + "." + (myMonth + 1) + "." + myYear);
            }

            // Показать сразу "годен до"
            radio2.setChecked(true);
            chooseDate2.setVisibility(View.VISIBLE);
            chooseDate2.setText(R.string.chooseDateOfExpiry2);
            chooseDate.setVisibility(View.VISIBLE);
            chooseDate.setText(R.string.chooseDateOfExpiry);
            bestBefore.setVisibility(View.INVISIBLE);
            spinner.setVisibility(View.INVISIBLE);
            days.setVisibility(View.INVISIBLE);
            quantity.setText(Integer.toString(quantityInt));
        }

    }




    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_DATE) {
            return new DatePickerDialog(this, myCallBack, myYear, myMonth, myDay);
        }
        return super.onCreateDialog(id);
    }
    DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;
            if (myMonth < 9) {
                date.setText(myDay + "." + "0" + (myMonth + 1) + "." + myYear);
            }
            else {
                date.setText(myDay + "." + (myMonth + 1) + "." + myYear);
            }
        }
    };


    public void onDateClick(View view) {
        showDialog(DIALOG_DATE);
    }

    public void onRadioOneClick(View view) {
        bestBefore.setVisibility(View.VISIBLE);
        days.setVisibility(View.VISIBLE);
        chooseDate2.setText(R.string.chooseDateOfMan);
        chooseDate.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.VISIBLE);
    }

    public void onRadioTwoClick(View view) {
        chooseDate2.setVisibility(View.VISIBLE);
        chooseDate2.setText(R.string.chooseDateOfExpiry2);
        chooseDate.setVisibility(View.VISIBLE);
        chooseDate.setText(R.string.chooseDateOfExpiry);
        bestBefore.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.INVISIBLE);
        days.setVisibility(View.INVISIBLE);
    }

    public void onSaveClick(View view) {
        if ((enterName.getText().toString().equals("")) ||
                (radio1.isChecked() && days.getText().toString().equals(""))
                || quantity.getText().toString().equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.please_fill_all_fields, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Calendar date = new GregorianCalendar(myYear, myMonth, myDay);
        Calendar currentDate = new GregorianCalendar();


        String text_spinner = spinner.getSelectedItem().toString();
        boolean is_days = text_spinner.equals(getString(R.string.days_in_add_act));


        if (radio1.isChecked()) {
            if (compare(date, currentDate) > 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.wrong_date, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            double term = Double.parseDouble(days.getText().toString());
            if (term <= 0 || term % 0.5 != 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.wrong_best_before, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (is_days) {
                date.add(Calendar.DAY_OF_MONTH, (int) term);
            }
            else {
                date.add(Calendar.MONTH,  (int) term);
                if (term % 1 != 0 ) { // Если есть еще половинка
                    int daysInMonth = date.getActualMaximum(Calendar.DAY_OF_MONTH);
                    date.add(Calendar.DAY_OF_MONTH, daysInMonth / 2);
                }
            }

            if (compare(date, currentDate) < 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.product_is_expired, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }

        else {
            if (compare(date, currentDate) < 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.wrong_date, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }


        Intent intent = new Intent();
        intent.putExtra(Resources.NAME, enterName.getText().toString());
        intent.putExtra(Resources.DATE, date);
        final int quant = Integer.parseInt(quantity.getText().toString());
        intent.putExtra(Resources.QUANTITY, quant);
        if (isChanging) {
            Calendar createdAt = new GregorianCalendar(YearCreated, MonthCreated, DayCreated, HourCreated, MinuteCreated, SecondCreated);
            intent.putExtra(Resources.CREATED_AT, createdAt);
            setResult(Resources.RESULT_MODIFY, intent);   // Изменениe
        }
        else {
            Calendar createdAt = new GregorianCalendar();
            intent.putExtra(Resources.CREATED_AT, createdAt);
            setResult(Resources.RESULT_ADD, intent);   // Добавление
        }
        finish();
    }

    public void onCancelClick(View view) {
        finish();
    }

    public int compare(Calendar d1, Calendar d2) {
        if (d1.get(Calendar.YEAR) != d2.get(Calendar.YEAR))
            return d1.get(Calendar.YEAR) - d2.get(Calendar.YEAR);
        if (d1.get(Calendar.MONTH) != d2.get(Calendar.MONTH))
            return d1.get(Calendar.MONTH) - d2.get(Calendar.MONTH);
        return d1.get(Calendar.DAY_OF_MONTH) - d2.get(Calendar.DAY_OF_MONTH);
    }
}


