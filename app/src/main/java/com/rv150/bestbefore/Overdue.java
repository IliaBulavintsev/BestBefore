package com.rv150.bestbefore;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Rudnev on 06.09.2016.
 */

// Активити "просроченные продукты"
public class Overdue extends AppCompatActivity {
    private CustomAdapter customAdapter;
    private List<StringWrapper> overdueList;
    private SharedPreferences sPrefs;
    private TextView isEmpty;
    private int position = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overdue);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        customAdapter = new CustomAdapter(this, width, true);
        overdueList = new ArrayList<>();

        isEmpty = (TextView) findViewById(R.id.isEmptyOverdue);
        ListView listView = (ListView) findViewById(R.id.overdue_list);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int pos, long id) {
                position = pos;
                DialogFragment dialog = new OverdueItemDialog();
                dialog.show(getFragmentManager(), "OverdueItemDialog");
            }
        });


        sPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        Boolean needHelp = sPrefs.getBoolean(Resources.SHOW_HELP, true);
        if (needHelp) {
            showHelp();
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putBoolean(Resources.SHOW_HELP, false);
            editor.apply();
        }
    }

    private void showHelp() {
        String whatsNewText = getResources().getString(R.string.help_text);
        new AlertDialog.Builder(this).setTitle(R.string.help).setMessage(whatsNewText).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }


    @Override
    protected void onResume() {
        super.onResume();

        overdueList = getOverdueProducts(this); // обновляем overdueList в соотв. с сохраненными данными

        customAdapter.setData(overdueList);
        customAdapter.notifyDataSetChanged();

        Typeface font = Typeface.createFromAsset(getAssets(), "san.ttf");
        isEmpty.setTypeface(font);
        if (overdueList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        } else {
            isEmpty.setVisibility(View.INVISIBLE);
        }
    }


    public static List<StringWrapper> getOverdueProducts(Context context) {
        List<StringWrapper> list = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (int i = 0; prefs.contains("del" + String.valueOf(i)); ++i) {
            if (prefs.getString("del" + String.valueOf(i), "").equals("") || i >= 1000) {
                break;
            }
            final String title = prefs.getString("del" + String.valueOf(i), "");
            final String date = prefs.getString("del" + String.valueOf(i + 1000), "0.0.0");
            String[] array = date.split("\\.");
            int myDay = Integer.parseInt(array[0]);
            int myMonth = Integer.parseInt(array[1]);
            int myYear = Integer.parseInt(array[2]);

            final String createdAtStr = prefs.getString("del" + String.valueOf(i + 2000), "0.0.0.0.0.0");
            String[] createdAtSplit = createdAtStr.split("\\.");
            int YearCreated = Integer.parseInt(createdAtSplit[0]);
            int MonthCreated = Integer.parseInt(createdAtSplit[1]);
            int DayCreated = Integer.parseInt(createdAtSplit[2]);
            int HourCreated = Integer.parseInt(createdAtSplit[3]);
            int MinuteCreated = Integer.parseInt(createdAtSplit[4]);
            int SecondCreated = Integer.parseInt(createdAtSplit[5]);

            StringWrapper temp = new StringWrapper(title, new GregorianCalendar(myYear, myMonth, myDay), new GregorianCalendar(YearCreated, MonthCreated, DayCreated, HourCreated, MinuteCreated, SecondCreated));
            list.add(temp);
        }
        return list;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_overdue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_list && !overdueList.isEmpty()) {
            DialogFragment dialog_delete_all = new ClearListDialog();
            dialog_delete_all.show(getFragmentManager(), "ClearList");
            return true;
        }
        if (id == R.id.action_show_help) {
            showHelp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void OptionChoosed(int option) {
        switch (option) {
            case 0:
                DialogFragment dialog = new OverdueYesNoDialog();
                dialog.show(getFragmentManager(), "OverdueYesNo");
                break;
            case 1:
                DialogFragment dialog_delete_all = new ClearListDialog();
                dialog_delete_all.show(getFragmentManager(), "ClearList");
                break;
            default:
                break;
        }
    }


    public void deleteItem() {
        overdueList.remove(position);
        position = -1;

        if (overdueList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        }

        customAdapter.setData(overdueList);
        customAdapter.notifyDataSetChanged();
        savePreferences();
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sPrefs.edit();
        for (int i = 0; i < overdueList.size(); ++i) {
            Calendar temp = overdueList.get(i).getDate();
            int myYear = temp.get(Calendar.YEAR);
            int myMonth = temp.get(Calendar.MONTH);
            int myDay = temp.get(Calendar.DAY_OF_MONTH);
            String str;
            if (myMonth < 9) {
                str = myDay + "." + "0" + myMonth + "." + myYear;
            } else {
                str = myDay + "." + myMonth + "." + myYear;
            }
            editor.putString("del" + String.valueOf(i), overdueList.get(i).getTitle());
            editor.putString("del" + String.valueOf(i + 1000), str);
        }
        editor.putString("del" + String.valueOf(overdueList.size()), ""); // признак конца списка
        editor.apply();
    }


    public void clearList() {
        overdueList.clear();
        savePreferences();
        customAdapter.setData(overdueList);
        customAdapter.notifyDataSetChanged();
        isEmpty.setVisibility(View.VISIBLE);
    }

}

