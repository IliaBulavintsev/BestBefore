package com.rv150.bestbefore.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.Models.Group;
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
    private TextView bestBeforeTxt;

    private AutoCompleteTextView enterName;
    private TextView dateTV;
    private EditText days;
    private EditText quantityET;
    private RadioButton radio1;
    private Spinner spinnerBestBefore;
    private Spinner spinnerGroups;

    // For spinner with groups
    int wasSelected = 0;
    // For clear first time quantity ET
    boolean isFirstTimeGetFocused = true;

    private int DIALOG_DATE = 1;
    private boolean isChanging = false;
    private String groupName;
    private Calendar bestBefore;

    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor cursor;

    GroupDAO groupDAO;
    final List<String> groupNames = new ArrayList<>();
    ArrayAdapter<String> spinnerGroupsAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);
        TextView name = (TextView)findViewById(R.id.name);
        chooseDate = (TextView)findViewById(R.id.chooseDate);
        chooseDate2 = (TextView)findViewById(R.id.chooseDate2);
        bestBeforeTxt = (TextView)findViewById(R.id.bestBefore);
        spinnerBestBefore = (Spinner)findViewById(R.id.spinner);
        enterName = (AutoCompleteTextView) findViewById(R.id.enterName);
        dateTV = (TextView)findViewById(R.id.date);
        days = (EditText)findViewById(R.id.days);
        quantityET = (EditText) findViewById(R.id.enterQuantity);
        quantityET.setText("1");
        spinnerGroups = (Spinner) findViewById(R.id.spinner_groups);;
        radio1 = (RadioButton)findViewById(R.id.radioButton1);

        String[] spinnerItems = new String[]{
                getString(R.string.days_in_add_act),
                getString(R.string.months_in_add_act)};
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, R.layout.custom_xml_spinner_layout, spinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBestBefore.setAdapter(spinnerAdapter);


        groupDAO = new GroupDAO(getApplicationContext());
        dbHelper = new DBHelper(getApplicationContext());
        bestBefore = new GregorianCalendar();

        final List<Group> groups = groupDAO.getAll();
        groupNames.add(getString(R.string.all_products));
        for (Group group: groups) {
            groupNames.add(group.getName());
        }
        groupNames.add(getString(R.string.create_group_in_spinner));

         spinnerGroupsAdapter =
                new ArrayAdapter<>(this, R.layout.custom_xml_spinner_layout, groupNames);
        spinnerGroupsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroups.setAdapter(spinnerGroupsAdapter);

        spinnerGroups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals(getString(R.string.create_group_in_spinner)))
                {
                    showAddGroupDialog();
                }
                else {
                    wasSelected = position;
                }
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });


        quantityET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && isFirstTimeGetFocused){
                    quantityET.setText("");
                    isFirstTimeGetFocused = false;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Показать сразу "годен до"
        chooseDate2.setVisibility(View.VISIBLE);
        chooseDate2.setText(R.string.chooseDateOfExpiry2);
        chooseDate.setVisibility(View.VISIBLE);
        chooseDate.setText(R.string.chooseDateOfExpiry);
        bestBeforeTxt.setVisibility(View.INVISIBLE);
        spinnerBestBefore.setVisibility(View.INVISIBLE);
        days.setVisibility(View.INVISIBLE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString(Resources.GROUP_NAME);
            String nameStr = extras.getString("name");
            if (nameStr != null) {          // Изменение продукта
                isChanging = true;
                setTitle(R.string.changing_product);
                enterName.setText(nameStr);
                bestBefore = (Calendar) extras.get(Resources.DATE);
                int quantity = extras.getInt(Resources.QUANTITY);
                quantityET.setText(String.valueOf(quantity));
            }
        }

        if (groupName != null) {
            int pos = groupNames.indexOf(groupName);
            spinnerGroups.setSelection(pos);
        }


        setDateText(bestBefore); // Установка нужной даты в TextView

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
            int year = bestBefore.get(Calendar.YEAR);
            int month = bestBefore.get(Calendar.MONTH);
            int day = bestBefore.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(this, myCallBack, year, month, day);
        }
        return super.onCreateDialog(id);
    }
    DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            bestBefore.set(Calendar.YEAR, year);
            bestBefore.set(Calendar.MONTH, monthOfYear);
            bestBefore.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setDateText(bestBefore);
        }
    };


    public void onDateClick(View view) {
        showDialog(DIALOG_DATE);
    }

    public void onRadioOneClick(View view) {
        bestBeforeTxt.setVisibility(View.VISIBLE);
        days.setVisibility(View.VISIBLE);
        chooseDate2.setText(R.string.chooseDateOfMan);
        chooseDate.setVisibility(View.INVISIBLE);
        spinnerBestBefore.setVisibility(View.VISIBLE);
    }

    public void onRadioTwoClick(View view) {
        chooseDate2.setVisibility(View.VISIBLE);
        chooseDate2.setText(R.string.chooseDateOfExpiry2);
        chooseDate.setVisibility(View.VISIBLE);
        chooseDate.setText(R.string.chooseDateOfExpiry);
        bestBeforeTxt.setVisibility(View.INVISIBLE);
        spinnerBestBefore.setVisibility(View.INVISIBLE);
        days.setVisibility(View.INVISIBLE);
    }

    public void onSaveClick(View view) {
        if ((enterName.getText().toString().equals("")) ||
                (radio1.isChecked() && days.getText().toString().equals(""))
                || quantityET.getText().toString().equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.please_fill_all_fields, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }


        Calendar currentDate = new GregorianCalendar();

        String text_spinner = spinnerBestBefore.getSelectedItem().toString();
        boolean is_days = text_spinner.equals(getString(R.string.days_in_add_act));




        if (radio1.isChecked()) {
            if (compare(bestBefore, currentDate) > 0) {
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
                bestBefore.add(Calendar.DAY_OF_MONTH, (int) term);
            }
            else {
                bestBefore.add(Calendar.MONTH,  (int) term);
                if (term % 1 != 0 ) { // Если есть еще половинка
                    int daysInMonth = bestBefore.getActualMaximum(Calendar.DAY_OF_MONTH);
                    bestBefore.add(Calendar.DAY_OF_MONTH, daysInMonth / 2);
                }
            }

            if (compare(bestBefore, currentDate) < 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.product_is_expired, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }

        else {
            if (compare(bestBefore, currentDate) < 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.wrong_date, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }

        final int quantity = Integer.parseInt(quantityET.getText().toString());
        if (quantity <= 0) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.wrong_quantity, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }


        Intent intent = new Intent();
        String name = enterName.getText().toString();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        intent.putExtra(Resources.NAME, name);
        bestBefore.set(Calendar.HOUR_OF_DAY, 23);
        bestBefore.set(Calendar.MINUTE, 59);
        intent.putExtra(Resources.DATE, bestBefore);
        intent.putExtra(Resources.QUANTITY, quantity);

        groupName = spinnerGroups.getSelectedItem().toString();
        intent.putExtra(Resources.GROUP_NAME, groupName);
        if (isChanging) {
            setResult(Resources.RESULT_MODIFY, intent);   // Изменениe
        }
        else {
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

    private void setDateText(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (month < 9) {
            dateTV.setText(day + "." + "0" + (month + 1) + "." + year);
        }
        else {
            dateTV.setText(day + "." + (month + 1) + "." + year);
        }
    }

    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.creating_group);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input, 70, 0, 100, 0);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String result = input.getText().toString();
                result = result.substring(0, 1).toUpperCase() + result.substring(1);
                if (result.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.wrong_name, Toast.LENGTH_SHORT);
                    toast.show();
                    showAddGroupDialog();
                }
                else {
                    createGroup(result);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                spinnerGroups.setSelection(wasSelected);
            }
        });
        builder.show();
    }

    private void createGroup(String name) {
        Group newGroup = new Group(name);
        try {
            groupDAO.insertGroup(newGroup);
        }
        catch (RuntimeException e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.group_with_this_name_already_exists, Toast.LENGTH_SHORT);
            toast.show();
            spinnerGroups.setSelection(wasSelected);
            return;
        }
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.group_was_created, Toast.LENGTH_SHORT);
        toast.show();
        int count = spinnerGroupsAdapter.getCount();
        spinnerGroupsAdapter.insert(name, count - 1);
        spinnerGroupsAdapter.notifyDataSetChanged();
    }
}


