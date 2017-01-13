package com.rv150.bestbefore.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Services.DBHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by rv150 on 07.01.2016.
 */
public class Add extends AppCompatActivity {
    private AutoCompleteTextView enterName;
    private TextView dateProducedTV;
    private EditText dateProducedET;
    private ImageView dateProducedIV;
    private TextView okayBeforeOrDaysTV;
    private EditText okayBeforeOrDaysET;

    private EditText quantityET;
    private RadioButton radioDateProduced;
    private RadioButton radioOkayBefore;
    private ImageView okayBeforeIV;
    private Spinner spinnerStorageLife;
    private Spinner spinnerGroups;
    private Spinner spinnerQuantity;

    // For spinner with groups
    private int wasSelected = 0;
    // For clear only first time
    private boolean quantityFirstTimeGetFocused = true;

    boolean showDateProduced;

    // Для отличия, какой date picker открыт
    private boolean firstDialogOpened;

    private final int DIALOG_DATE_PRODUCED = 1;
    private final int DIALOG_OKAY_BEFORE = 2;
    private boolean isChanging = false;
    private String groupName;
    private Calendar dateProduced;
    private Calendar okayBefore;

    private DBHelper dbHelper;

    private GroupDAO groupDAO;
    private ProductDAO productDAO;
    private final List<String> groupNames = new ArrayList<>();
    private  ArrayAdapter<String> spinnerGroupsAdapter;
    private SharedPreferences sPrefs;

    private Product mProduct;

    public static final String TAG = "Add activity";

    private boolean flagForDateProduced = true;
    private int previousDateProducedLength = 0;
    private boolean isDateProducedFirstTimeOpened = true;
    private boolean flagForOkayBefore = true;
    private int previousOkayBeforeLength = 0;
    private boolean isOkayBeforeFirstTimeOpened = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);

        sPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        enterName = (AutoCompleteTextView) findViewById(R.id.enterName);
        dateProducedTV = (TextView) findViewById(R.id.date_produced_TV);
        dateProducedET = (EditText) findViewById(R.id.date_produced_ET);
        dateProducedIV = (ImageView) findViewById(R.id.date_produced_IV);
        okayBeforeOrDaysTV = (TextView) findViewById(R.id.best_before_or_days_TV);
        okayBeforeOrDaysET = (EditText) findViewById(R.id.best_before_or_days_ET);
        okayBeforeIV = (ImageView) findViewById(R.id.okay_before_IV);

        spinnerStorageLife = (Spinner)findViewById(R.id.spinner_storage_life);

        quantityET = (EditText) findViewById(R.id.quantityET);
        quantityET.setText("1");
        spinnerGroups = (Spinner) findViewById(R.id.spinner_groups);


        radioOkayBefore = (RadioButton) findViewById(R.id.radioButtonOkayBefore);
        radioDateProduced = (RadioButton)findViewById(R.id.radioButtonDateProduced);
        boolean lastCheckedIsOkayBefore = sPrefs.getBoolean(Resources.LAST_RADIO_WAS_OKAY_BEFORE, true);
        boolean preferenceEnabled = sPrefs.getBoolean("remember_radiobuttons", true);
        if (preferenceEnabled && !lastCheckedIsOkayBefore) {
            radioOkayBefore.setChecked(false);
            radioDateProduced.setChecked(true);
            onRadioDateManClick(null);
        }



        spinnerQuantity = (Spinner) findViewById(R.id.spinner_quantity);

        String[] terms = new String[]{
                getString(R.string.days_in_add_act),
                getString(R.string.months_in_add_act)};
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, R.layout.custom_xml_spinner_layout, terms);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStorageLife.setAdapter(spinnerAdapter);

        int size = Measures.values().length;
        String[] measures = new String[size];
        for (int i = 0; i < size; ++i) {
            measures[i] = Measures.values()[i].getText();
        }

        ArrayAdapter<String> spinnerQuantityAdapter =
                new ArrayAdapter<>(this, R.layout.custom_xml_spinner_layout, measures);
        spinnerQuantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuantity.setAdapter(spinnerQuantityAdapter);

        groupDAO = new GroupDAO(getApplicationContext());
        productDAO = new ProductDAO(getApplicationContext());
        dbHelper = new DBHelper(getApplicationContext());

        okayBefore = Calendar.getInstance();
        dateProduced = Calendar.getInstance();

        final SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final List<Group> groups = groupDAO.getAll();
        final String mainGroupName = sPrefs.getString(Resources.MAIN_GROUP_NAME, getString(R.string.all_products));
        groupNames.add(mainGroupName);
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

        showDateProduced = sPrefs.getBoolean("use_date_produced", true);
        if (!showDateProduced) {
            dateProducedTV.setVisibility(View.GONE);
            dateProducedET.setVisibility(View.GONE);
            dateProducedIV.setVisibility(View.GONE);
        }

        boolean showQuantity = sPrefs.getBoolean("use_quantity", true);
        if (!showQuantity) {
            TextView quantityTV = (TextView) findViewById(R.id.quantityTV);
            quantityTV.setVisibility(View.GONE);
            quantityET.setVisibility(View.GONE);
        }

        boolean useGroups = sPrefs.getBoolean("use_groups", true);
        if (!useGroups) {
            TextView groupTV = (TextView) findViewById(R.id.groupTV);
            groupTV.setVisibility(View.GONE);
            spinnerGroups.setVisibility(View.GONE);
        }



        quantityET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && quantityFirstTimeGetFocused){
                    quantityET.setText("");
                    quantityFirstTimeGetFocused = false;
                }
            }
        });


        boolean showHelp = sPrefs.getBoolean(Resources.PREF_SHOW_HELP_IN_ADD_ACTIVITY, true);
        if (showHelp) {
            new AlertDialog.Builder(this).setTitle(R.string.help)
                    .setMessage(R.string.help_in_add_activity)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putBoolean(Resources.PREF_SHOW_HELP_IN_ADD_ACTIVITY, false);
            editor.apply();
        }





        dateProducedET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    previousDateProducedLength = 0;
                    return;
                }
                int len = s.length();
                int selectionPos = dateProducedET.getSelectionStart() == 0? 0 : dateProducedET.getSelectionStart() - 1;
                if (previousDateProducedLength > s.length() && flagForDateProduced && s.toString().charAt(selectionPos) == '.') {
                    flagForDateProduced = false;
                    String newValue = s.toString().substring(0, selectionPos) + s.toString().substring(selectionPos + 1, len);
                    dateProducedET.setText(newValue);
                    dateProducedET.setSelection(selectionPos);
                    previousDateProducedLength = newValue.length();
                    return;
                }

                // Добавление точки после второго введенного символа (или пятого)
                if (flagForDateProduced && (selectionPos == 1 || selectionPos == 4) && (previousDateProducedLength < s.length())) {
                    if (s.toString().charAt(selectionPos) != '.') {
                        if (len <= selectionPos + 1 || len > selectionPos + 1 && s.toString().charAt(selectionPos + 1) != '.') {
                            String newValue = s.toString().substring(0, selectionPos + 1) + '.' + s.toString().substring(selectionPos + 1, len);
                            dateProducedET.setText(newValue);
                            dateProducedET.setSelection(selectionPos + 2);
                        }
                        flagForDateProduced = false;
                        previousDateProducedLength = dateProducedET.getText().toString().length();
                        return;
                    }
                    else {
                        String newValue = s.toString().substring(0, selectionPos - 1) + '0' + s.toString().substring(selectionPos - 1, len);

                        if (len > selectionPos + 1 && s.toString().charAt(selectionPos + 1) == '.') {
                            newValue = newValue.substring(0, selectionPos + 1) + newValue.substring(selectionPos + 2, len + 1);
                        }

                        dateProducedET.setText(newValue);
                        dateProducedET.setSelection(selectionPos + 2);
                        previousDateProducedLength = newValue.length();
                        return;
                    }
                }

                // Добавление точки в случае стирания прошлой точки и нахождении даты в виде 15|____
                if (flagForDateProduced && (selectionPos == 2 || selectionPos == 5) && (previousDateProducedLength < s.length())) {
                    if (s.toString().charAt(selectionPos) != '.') {
                        flagForDateProduced = false;
                        String newValue = s.toString().substring(0, selectionPos) + '.' + s.toString().substring(selectionPos, len);
                        dateProducedET.setText(newValue);
                        dateProducedET.setSelection(selectionPos + 2);
                        previousDateProducedLength = newValue.length();
                        return;
                    }
                }


                flagForDateProduced = true;
                previousDateProducedLength = s.length();
            }



            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        okayBeforeOrDaysET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("") || radioDateProduced.isChecked()) {
                    previousOkayBeforeLength = 0;
                    return;
                }
                int len = s.length();
                int selectionPos = okayBeforeOrDaysET.getSelectionStart() == 0? 0 : okayBeforeOrDaysET.getSelectionStart() - 1;
                if (previousOkayBeforeLength > s.length() && flagForOkayBefore && s.toString().charAt(selectionPos) == '.') {
                    flagForOkayBefore = false;
                    String newValue = s.toString().substring(0, selectionPos) + s.toString().substring(selectionPos + 1, len);
                    okayBeforeOrDaysET.setText(newValue);
                    okayBeforeOrDaysET.setSelection(selectionPos);
                    previousOkayBeforeLength = newValue.length();
                    return;
                }

                // Добавление точки после второго введенного символа (или пятого)
                if (flagForOkayBefore && (selectionPos == 1 || selectionPos == 4) && (previousOkayBeforeLength < s.length())) {
                    if (s.toString().charAt(selectionPos) != '.') {
                        if (len <= selectionPos + 1 || len > selectionPos + 1 && s.toString().charAt(selectionPos + 1) != '.') {
                            String newValue = s.toString().substring(0, selectionPos + 1) + '.' + s.toString().substring(selectionPos + 1, len);
                            okayBeforeOrDaysET.setText(newValue);
                            okayBeforeOrDaysET.setSelection(selectionPos + 2);
                        }
                        flagForOkayBefore = false;
                        previousOkayBeforeLength = okayBeforeOrDaysET.getText().toString().length();
                        return;
                    }
                    else {
                        String newValue = s.toString().substring(0, selectionPos - 1) + '0' + s.toString().substring(selectionPos - 1, len);

                        if (len > selectionPos + 1 && s.toString().charAt(selectionPos + 1) == '.') {
                            newValue = newValue.substring(0, selectionPos + 1) + newValue.substring(selectionPos + 2, len + 1);
                        }

                        okayBeforeOrDaysET.setText(newValue);
                        okayBeforeOrDaysET.setSelection(selectionPos + 2);
                        previousOkayBeforeLength = newValue.length();
                        return;
                    }
                }

                // Добавление точки в случае стирания прошлой точки и нахождении даты в виде 15|____
                if (flagForOkayBefore && (selectionPos == 2 || selectionPos == 5) && (previousOkayBeforeLength < s.length())) {
                    if (s.toString().charAt(selectionPos) != '.') {
                        flagForOkayBefore = false;
                        String newValue = s.toString().substring(0, selectionPos) + '.' + s.toString().substring(selectionPos, len);
                        okayBeforeOrDaysET.setText(newValue);
                        okayBeforeOrDaysET.setSelection(selectionPos + 2);
                        previousOkayBeforeLength = newValue.length();
                        return;
                    }
                }


                flagForOkayBefore = true;
                previousOkayBeforeLength = s.length();
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });




        dateProducedET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                boolean clear = sPrefs.getBoolean("clear_date_field", true);
                if (hasFocus && clear && isDateProducedFirstTimeOpened) {
                    dateProducedET.setText("");
                    isDateProducedFirstTimeOpened = false;
                }
            }
        });
        okayBeforeOrDaysET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                boolean clear = sPrefs.getBoolean("clear_date_field", true);
                if (hasFocus && clear && isOkayBeforeFirstTimeOpened) {
                    okayBeforeOrDaysET.setText("");
                    isOkayBeforeFirstTimeOpened = false;
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mProduct = extras.getParcelable(Product.class.getName());
            if (mProduct != null) {          // Изменение продукта
                isChanging = true;
                setTitle(R.string.changing_product);
                enterName.setText(mProduct.getTitle());
                okayBefore = mProduct.getDate();
                dateProduced = mProduct.getProduced();
                quantityET.setText(String.valueOf(mProduct.getQuantity()));
                long groupId = mProduct.getGroupId();
                if (groupId != -1) {
                    Group group = groupDAO.get(groupId);
                    groupName = group.getName();
                }
                int measure = mProduct.getMeasure();
                spinnerQuantity.setSelection(measure);
            } else {
                mProduct = new Product();
                long groupId = extras.getLong(Resources.GROUP_ID, Resources.ID_MAIN_GROUP);
                if (groupId != Resources.ID_MAIN_GROUP) {
                    Group group = groupDAO.get(groupId);
                    groupName = group.getName();
                }
            }
        } else {
            mProduct = new Product();
        }

        if (groupName != null) {
            int pos = groupNames.indexOf(groupName);
            spinnerGroups.setSelection(pos);
        }


        setDateText(dateProduced, dateProducedET);
        if (radioOkayBefore.isChecked()) {
            setDateText(okayBefore, okayBeforeOrDaysET); // Установка нужной даты в EditText
        }

        String[] popularProducts = {
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
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
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
        if (id == DIALOG_DATE_PRODUCED) {
            int year = dateProduced.get(Calendar.YEAR);
            int month = dateProduced.get(Calendar.MONTH);
            int day = dateProduced.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(this, myCallBack, year, month, day);
        }
        if (id == DIALOG_OKAY_BEFORE) {
            int year = okayBefore.get(Calendar.YEAR);
            int month = okayBefore.get(Calendar.MONTH);
            int day = okayBefore.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(this, myCallBack, year, month, day);
        }
        return super.onCreateDialog(id);
    }
    DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            if (firstDialogOpened) {    // Установка даты изготовления
                dateProduced.set(Calendar.YEAR, year);
                dateProduced.set(Calendar.MONTH, monthOfYear);
                dateProduced.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setDateText(dateProduced, dateProducedET);
            }
            else {  // Установка "годен до"
                okayBefore.set(Calendar.YEAR, year);
                okayBefore.set(Calendar.MONTH, monthOfYear);
                okayBefore.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setDateText(okayBefore, okayBeforeOrDaysET);
            }
        }
    };


    public void onDateProducedClick(View view) {
        firstDialogOpened = true;
        showDialog(DIALOG_DATE_PRODUCED);
    }
    public void onOkayBeforeClick(View view)
    {
        firstDialogOpened = false;
        showDialog(DIALOG_OKAY_BEFORE);
    }



    public void onRadioOkayBeforeClick(View view) {
        okayBeforeOrDaysTV.setText(R.string.okayBefore);
        okayBeforeOrDaysET.setHint(R.string.dateFormat);
        setDateText(okayBefore, okayBeforeOrDaysET);
        spinnerStorageLife.setVisibility(View.GONE);
        okayBeforeIV.setVisibility(View.VISIBLE);
        if (!showDateProduced) {
            dateProducedTV.setVisibility(View.GONE);
            dateProducedET.setVisibility(View.GONE);
            dateProducedIV.setVisibility(View.GONE);
        }
    }

    public void onRadioDateManClick(View view) {
        okayBeforeOrDaysTV.setText(R.string.bestBefore);
        okayBeforeOrDaysET.setHint("");
        okayBeforeOrDaysET.setText("");
        spinnerStorageLife.setVisibility(View.VISIBLE);
        okayBeforeIV.setVisibility(View.GONE);
        dateProducedTV.setVisibility(View.VISIBLE);
        dateProducedET.setVisibility(View.VISIBLE);
        dateProducedIV.setVisibility(View.VISIBLE);
    }





    public void onSaveClick(View view) {
        if ((enterName.getText().toString().equals("")) ||
                (dateProducedET.getVisibility() != View.GONE &&
                        dateProducedET.getText().toString().equals("")) ||
                okayBeforeOrDaysET.getText().toString().equals("")
                || (quantityET.getVisibility() != View.GONE &&
                quantityET.getText().toString().equals(""))) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.please_fill_all_fields, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // Чтение из полей ввода
        boolean isOk = parseInputDate(dateProducedET.getText().toString(), dateProduced);
        if (!radioDateProduced.isChecked()) {
            isOk = isOk & parseInputDate(okayBeforeOrDaysET.getText().toString(), okayBefore);
        }

        if (!isOk) {
            return;
        }



        String text_spinner = spinnerStorageLife.getSelectedItem().toString();
        boolean is_days = text_spinner.equals(getString(R.string.days_in_add_act));

        Calendar currentDate = Calendar.getInstance();
        // Проверка "Даты изготовления"
        if (dateProducedET.getVisibility() != View.GONE && compare(dateProduced, currentDate) > 0) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.wrong_date, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // Выбрано "Дата изготовления"
        // dateProduced ВСЕГДА хранит дату изготовления, дата окончания в okayBefore
        if (radioDateProduced.isChecked()) {
            final double term;
            try {
                term = Double.parseDouble(okayBeforeOrDaysET.getText().toString());
                if (term <= 0 || term % 0.5 != 0) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.wrong_best_before, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
            }
            catch (RuntimeException e) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.wrong_best_before, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            okayBefore = (Calendar) dateProduced.clone();
            if (is_days) {
                okayBefore.add(Calendar.DAY_OF_MONTH, (int) term);
            }
            else {
                okayBefore.add(Calendar.MONTH,  (int) term);
                if (term % 1 != 0 ) { // Если есть еще половинка
                    int daysInMonth = okayBefore.getActualMaximum(Calendar.DAY_OF_MONTH);
                    okayBefore.add(Calendar.DAY_OF_MONTH, daysInMonth / 2);
                }
            }

            if (compare(okayBefore, currentDate) < 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.product_is_expired, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
        else {
            if (compare(okayBefore, currentDate) < 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.wrong_date, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
        okayBefore.set(Calendar.HOUR_OF_DAY, 23);
        okayBefore.set(Calendar.MINUTE, 59);

        final int quantity = Integer.parseInt(quantityET.getText().toString());
        if (quantity <= 0) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.wrong_quantity, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }



        String name = enterName.getText().toString();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        groupName = spinnerGroups.getSelectedItem().toString();
        long groupId;
        final String mainGroupName = sPrefs.getString(Resources.MAIN_GROUP_NAME, getString(R.string.all_products));
        if (groupName.equals(mainGroupName)) {
            groupId = -1;
        }
        else {
            Group group = groupDAO.get(groupName);
            groupId = group.getId();
        }

        mProduct.setTitle(name);

        // Если есть поле "Дата производства"
        if (dateProducedET.getVisibility() != View.GONE) {
            mProduct.setProduced(dateProduced);
        }

        mProduct.setDate(okayBefore);
        mProduct.setQuantity(quantity);


        String choosenMeasure = spinnerQuantity.getSelectedItem().toString();
        int measureValue = Measures.fromString(choosenMeasure).ordinal();
        mProduct.setMeasure(measureValue);

        mProduct.setGroupId(groupId);


        Intent intent = new Intent();
        if (isChanging) {
            productDAO.updateProduct(mProduct);
            setResult(Resources.RESULT_MODIFY, intent);   // Изменениe
        }
        else {
            long id = productDAO.insertProduct(mProduct);
            mProduct.setId(id);
            setResult(Resources.RESULT_ADD, intent);   // Добавление
        }

        // Запоминаем положение радиокнопок
        SharedPreferences.Editor editor = sPrefs.edit();
        if (radioOkayBefore.isChecked()) {
            editor.putBoolean(Resources.LAST_RADIO_WAS_OKAY_BEFORE, true);
        }
        else {
            editor.putBoolean(Resources.LAST_RADIO_WAS_OKAY_BEFORE, false);
        }
        editor.apply();

        intent.putExtra(Product.class.getName(), mProduct);
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

    private void setDateText(Calendar calendar, EditText editText) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (month < 9) {
            editText.setText(day + "." + "0" + (month + 1) + "." + year);
        }
        else {
            editText.setText(day + "." + (month + 1) + "." + year);
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
                if (result.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.field_is_empty, Toast.LENGTH_SHORT);
                    toast.show();
                    showAddGroupDialog();
                }
                else {
                    result = result.substring(0, 1).toUpperCase() + result.substring(1);
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
        builder.setCancelable(false);
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

    private boolean parseInputDate(String text, Calendar calendar) {
        try {
            String[] parsed = text.split("(\\.)|(-)");
            int day = Integer.valueOf(parsed[0]);
            int month = Integer.valueOf(parsed[1]);
            int year = Integer.valueOf(parsed[2]);
            calendar.setLenient(false);
            calendar.set(year, month - 1, day);
            calendar.getTime();
            return true;
        }
        catch (Exception e) {
            Log.e(TAG, "Cannot parse input date: " + e.getMessage());
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.error_parse_date, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
    }

    public enum Measures {
        PIECE("шт"),
        KG("кг"),
        G("г"),
        L("л"),
        ML("мл");
        private String text;
        Measures(String text) {
            this.text = text;
        }
        public String getText() {
            return this.text;
        }

        public static Measures fromString(String text) {
            if (text != null) {
                for (Measures b : Measures.values()) {
                    if (text.equalsIgnoreCase(b.text)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }
}


