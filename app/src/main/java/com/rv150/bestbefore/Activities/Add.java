package com.rv150.bestbefore.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Services.DBHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by rv150 on 07.01.2016.
 */
public class Add extends AppCompatActivity {
    private TextView chooseDate;
    private TextView chooseDate2;
    private TextView bestBefore;

    private AutoCompleteTextView enterName;
    private TextView date;
    private EditText days;
    private EditText quantity;
    private RadioButton radio1;
    private RadioButton radio2;
    private Spinner spinner;

    private int DIALOG_DATE = 1;
    private boolean isChanging = false;

    private int DayCreated;
    private int MonthCreated;
    private int YearCreated;
    private int HourCreated;
    private int MinuteCreated;
    private int SecondCreated;

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor cursor;

    private int myYear;
    private int myMonth;
    private int myDay;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);
        TextView name = (TextView)findViewById(R.id.name);
        chooseDate = (TextView)findViewById(R.id.chooseDate);
        chooseDate2 = (TextView)findViewById(R.id.chooseDate2);
        bestBefore = (TextView)findViewById(R.id.bestBefore);
        spinner = (Spinner)findViewById(R.id.spinner);
        enterName = (AutoCompleteTextView) findViewById(R.id.enterName);
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
        radio1 = (RadioButton)findViewById(R.id.radioButton1);
        radio2 = (RadioButton)findViewById(R.id.radioButton2);

        String[] spinnerItems = new String[]{
                getString(R.string.days_in_add_act),
                getString(R.string.months_in_add_act)};
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, R.layout.custom_xml_spinner_layout, spinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        dbHelper = new DBHelper(getApplicationContext());

        // Инициализация переменных сегодняшним числом
        Calendar currentData = new GregorianCalendar();
        myYear = currentData.get(Calendar.YEAR);
        myMonth = currentData.get(Calendar.MONTH);
        myDay = currentData.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Показать сразу "годен до"
        chooseDate2.setVisibility(View.VISIBLE);
        chooseDate2.setText(R.string.chooseDateOfExpiry2);
        chooseDate.setVisibility(View.VISIBLE);
        chooseDate.setText(R.string.chooseDateOfExpiry);
        bestBefore.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.INVISIBLE);
        days.setVisibility(View.INVISIBLE);

        Bundle extras = getIntent().getExtras();
        if (extras == null)  {                      // Добавление продукта
            setDateText(myDay, myMonth, myYear);
        }
        else  {                                     // Изменение продукта
            isChanging = true;
            setTitle(R.string.changing_product);
            String nameStr = extras.getString("name");
            enterName.setText(nameStr);
            myDay = extras.getInt(Resources.MY_DAY);
            myMonth = extras.getInt(Resources.MY_MONTH);
            myYear = extras.getInt(Resources.MY_YEAR);
            setDateText(myDay, myMonth, myYear);

            DayCreated = extras.getInt(Resources.DAY_CREATED);
            MonthCreated = extras.getInt(Resources.MONTH_CREATED);
            YearCreated = extras.getInt(Resources.YEAR_CREATED);
            HourCreated = extras.getInt(Resources.HOUR_CREATED);
            MinuteCreated = extras.getInt(Resources.MINUTE_CREATED);
            SecondCreated = extras.getInt(Resources.SECOND_CREATED);

            int quantityInt = extras.getInt(Resources.QUANTITY);
            quantity.setText(Integer.toString(quantityInt));
        }


        String[] projection = {
                DBHelper.AutoCompletedProducts.COLUMN_NAME_NAME
        };

        String sortOrder =
                DBHelper.AutoCompletedProducts.COLUMN_NAME_NAME + " ASC";

       //получаем данные из бд
       db = dbHelper.getReadableDatabase();
       cursor = db.query(
                DBHelper.AutoCompletedProducts.TABLE_NAME, // The table to query
                projection,                               // The columns to return
                null,                                   // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        String [] popularProducts = {
                "баранина",
                "бекон",
                "брокколи",
                "брынза",
                "буженина",
                "ветчина",
                "говядина",
                "икра",
                "икра красная",
                "икра кабачковая",
                "йогурт",
                "кетчуп",
                "кефир",
                "капуста",
                "капуста квашеная",
                "капуста цветная",
                "колбаса",
                "колбаса вареная",
                "колбаса сырокопченая",
                "колбаса варено-копченая",
                "колбаса докторская",
                "колбаса сервелат",
                "креветки",
                "крылышки",
                "кукуруза",
                "курица",
                "лосось",
                "макароны",
                "майонез",
                "маслины",
                "масло",
                "масло сливочное",
                "масло подсолнечное",
                "масло оливковое",
                "молоко",
                "мороженое",
                "мясо",
                "окунь",
                "окорок",
                "оливки",
                "осетр",
                "паштет",
                "пельмени",
                "печенье",
                "печень",
                "пицца",
                "рис",
                "рыба",
                "ряженка",
                "свинина",
                "селедка",
                "сливки",
                "сметана",
                "сок",
                "сок яблочный",
                "сок вишневый",
                "сок мультифруктовый",
                "сок апельсиновый",
                "сок виноградный",
                "сок томатный",
                "сок ананасовый",
                "сосиски",
                "судак",
                "скумбрия",
                "сыр",
                "сыр плавленый",
                "сырок глазированный",
                "творог",
                "творог обезжиренный",
                "творог полужирный",
                "творог жирный",
                "томатная паста",
                "телятина",
                "фарш",
                "хрен",
                "шампиньоны",
                "яйцо куриное",
                "яйцо перепелиное"
        };



        List<String> items = new ArrayList<>();
        items.addAll(Arrays.asList(popularProducts));

        while (cursor.moveToNext()) {
            String product = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBHelper.AutoCompletedProducts.COLUMN_NAME_NAME));
            product = product.toLowerCase();
            if (!items.contains(product)) {
                items.add(product);
            }
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>
                    (this, R.layout.support_simple_spinner_dropdown_item, items);
        enterName.setAdapter(adapter);
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
            setDateText(myDay, myMonth, myYear);
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
            Calendar createdAt = new GregorianCalendar(YearCreated, MonthCreated,
                    DayCreated, HourCreated, MinuteCreated, SecondCreated);
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

    private int compare(Calendar d1, Calendar d2) {
        if (d1.get(Calendar.YEAR) != d2.get(Calendar.YEAR))
            return d1.get(Calendar.YEAR) - d2.get(Calendar.YEAR);
        if (d1.get(Calendar.MONTH) != d2.get(Calendar.MONTH))
            return d1.get(Calendar.MONTH) - d2.get(Calendar.MONTH);
        return d1.get(Calendar.DAY_OF_MONTH) - d2.get(Calendar.DAY_OF_MONTH);
    }

    private void setDateText(int day, int month, int year) {
        if (month < 9) {
            date.setText(day + "." + "0" + (month + 1) + "." + year);
        }
        else {
            date.setText(day + "." + (month + 1) + "." + year);
        }
    }
}


