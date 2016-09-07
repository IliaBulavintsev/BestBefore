package com.rv150.bestbefore;


import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;


// Все настройки чистятся в UpdatePreferences() и в классе DeleteOverdue

// Добавить напоминание оценить приложение, выводить после выхода из приложения...
// Перевести дни в месяцы

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    static final private int RequestCode = 0;
    private CustomAdapter customAdapter;
    private List<StringWrapper> wrapperList;
    private SharedPreferences sPrefs;
    private int position = -1;

    private boolean resumeWasNotCalled;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        customAdapter = new CustomAdapter(this, width);
        wrapperList = new ArrayList<>();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        resumeWasNotCalled = true;

        // показ приветственного сообщения
        Boolean showWelcomeScreen = sPrefs.getBoolean("showWelcomeScreen", true);
        if (showWelcomeScreen) {
            String whatsNewText = getResources().getString(R.string.welcomeText);
            new AlertDialog.Builder(this).setTitle(R.string.welcomeTitle).setMessage(whatsNewText).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putBoolean("showWelcomeScreen", false);
            editor.apply(); // Very important to save the preference
        }
        else {  // либо вывод информации о просроченных сегодня продуктах

            //Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Просрочено сегодня: " + overdueToday, Snackbar.LENGTH_LONG);
            // View sbView = snackbar.getView();
            // sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
            //snackbar.show();

        }



        // Инкремент счетчика открываний приложения
        boolean needRate = sPrefs.getBoolean("needRate", true);
        if (needRate) {
            int timesOpened = sPrefs.getInt("timesOpened", 0);
            timesOpened++;
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putInt("timesOpened", timesOpened);
            editor.apply();
        }


        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int pos, long id) {
                position = pos;
                DialogFragment dialog = new ItemDialog();
                dialog.show(getFragmentManager(), "ItemDialog");
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener(){
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            int mLastFirstVisibleItem = 0;
            boolean show = false;
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                fab.show();
            }
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (view.getId() == listView.getId()) {
                    final int currentFirstVisibleItem = listView.getFirstVisiblePosition();
                    int displayed = listView.getHeight() / listView.getChildAt(1).getHeight();
                    if ((wrapperList.size() > 10) && (wrapperList.size() > displayed - 2)) {
                        if (wrapperList.size() > displayed + 1) {
                            if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                                fab.hide();
                                Log.i("a", "scrolling down...");
                            } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                                fab.show();
                                Log.i("a", "scrolling up...");
                            }
                            mLastFirstVisibleItem = currentFirstVisibleItem;
                        } else {
                            if (show) {
                                fab.show();
                            } else {
                                fab.hide();
                            }
                            show = !show;
                        }
                    }
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        NotificationManager notifManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();

        loadFromPreferences(); // обновляем wrapperList в соотв. с сохраненными данными


        // Удаление просроченных
        DeleteOverdue deleteOverdue = new DeleteOverdue(wrapperList, this);
        List<String> newOverdue = deleteOverdue.delete();
        boolean needShowOverdue = sPrefs.getBoolean("needShowOverdue", true);

        if (needShowOverdue && resumeWasNotCalled && !newOverdue.isEmpty()) {
            resumeWasNotCalled = false;
            StringBuilder array = new StringBuilder();
            for (String item : newOverdue) {
                array.append(item);
                array.append('\n');
            }
            new AlertDialog.Builder(this).setTitle("Новые просроки").setMessage(array.toString()).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                }
            }).show();
        }


        final String STANDART = "STANDART";
        final String SPOILED_TO_FRESH = "SPOILED_TO_FRESH";
        final String FRESH_TO_SPOILED = "FRESH_TO_SPOILED";


        // Сортировка
        if (wrapperList.size() >= 2) {
            String howToSort = sPrefs.getString("how_to_sort", STANDART);
            switch (howToSort) {
                case STANDART:
                    Collections.sort(wrapperList, StringWrapper.getStandartComparator());
                    break;
                case SPOILED_TO_FRESH:
                    Collections.sort(wrapperList, StringWrapper.getSpoiledToFreshComparator());
                    break;
                case FRESH_TO_SPOILED:
                    Collections.sort(wrapperList, StringWrapper.getFreshToSpoiledComparator());
                    break;
                default:
                    break;
            }
        }


//        StringWrapper s = new StringWrapper("unwwwewtil 4", new GregorianCalendar(2016, 8, 3));
//        StringWrapper ss = new StringWrapper("untewewewil 5", new GregorianCalendar(2016,8,5));
//        wrapperList.add(s);
//        wrapperList.add(ss);




        UpdatePreferences(); // Сохраняем возможные изменения
        customAdapter.setData(wrapperList);
        customAdapter.notifyDataSetChanged();

        // Надпись "Список пуст!"
        TextView isEmpty = (TextView)findViewById(R.id.isEmptyText);
        Typeface font = Typeface.createFromAsset(getAssets(), "san.ttf");
        isEmpty.setTypeface(font);
        if (wrapperList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        }
        else {
            isEmpty.setVisibility(View.INVISIBLE);
        }

        boolean alarm_set = sPrefs.getBoolean("alarm_set_flag", false);
        boolean last_day = sPrefs.getBoolean("last_day", true);
        boolean day_before = sPrefs.getBoolean("day_before", false);
        boolean three_days = sPrefs.getBoolean("three_days", false);
        SharedPreferences.Editor editor = sPrefs.edit();
        if (wrapperList.isEmpty()) {
            AlarmReceiver am = new AlarmReceiver();
            am.cancelAlarm(this);
            editor.putBoolean("alarm_set_flag", false);
        }
        else if(!alarm_set && (last_day || day_before || three_days)){
            AlarmReceiver am = new AlarmReceiver();
            am.setAlarm(this);
            editor.putBoolean("alarm_set_flag", true); // value to store
        }
        editor.apply();
    }







    public void OptionChoosed(int option) {
        final int MODIFY = 0;
        final int DELETE = 1;
        final int DELETE_ALL = 2;
        switch (option) {
            case MODIFY:
                Intent intent = new Intent(MainActivity.this, Add.class);
                intent.putExtra("name", wrapperList.get(position).getTitle());
                Calendar date = wrapperList.get(position).getDate();
                int myDay = date.get(Calendar.DAY_OF_MONTH);
                int myMonty = date.get(Calendar.MONTH);
                int myYear = date.get(Calendar.YEAR);
                intent.putExtra("myDay", myDay);
                intent.putExtra("myMonth", myMonty);
                intent.putExtra("myYear", myYear);

                Calendar createdAt = wrapperList.get(position).createdAt();
                int DayCreated = createdAt.get(Calendar.DAY_OF_MONTH);
                int MonthCreated = createdAt.get(Calendar.MONTH);
                int YearCreated = createdAt.get(Calendar.YEAR);
                int HourCreated = createdAt.get(Calendar.HOUR_OF_DAY);
                int MinuteCreated = createdAt.get(Calendar.MINUTE);
                int SecondCreated = createdAt.get(Calendar.SECOND);
                intent.putExtra("DayCreated", DayCreated);
                intent.putExtra("MonthCreated", MonthCreated);
                intent.putExtra("YearCreated", YearCreated);
                intent.putExtra("HourCreated", HourCreated);
                intent.putExtra("MinuteCreated", MinuteCreated);
                intent.putExtra("SecondCreated", SecondCreated);

                startActivityForResult(intent, RequestCode);
                break;

            case DELETE:
                DialogFragment dialog = new YesNoDialog();
                dialog.show(getFragmentManager(), "YesNo");
                break;

            case DELETE_ALL:
                DialogFragment dialog_delete_all = new DeleteAllDialog();
                dialog_delete_all.show(getFragmentManager(), "DeleteAll");
                break;

            default:
                break;
        }
    }

    public void DeleteItem() {
        wrapperList.remove(position);
        position = -1;
        TextView isEmpty = (TextView)findViewById(R.id.isEmptyText);
        if (wrapperList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        }
        else {
            isEmpty.setVisibility(View.INVISIBLE);
        }
        customAdapter.setData(wrapperList);
        customAdapter.notifyDataSetChanged();
        UpdatePreferences();
    }

    public void DeleteAll() {
        wrapperList.clear();
        customAdapter.setData(wrapperList);
        customAdapter.notifyDataSetChanged();
        UpdatePreferences();
        TextView isEmpty = (TextView)findViewById(R.id.isEmptyText);
        isEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Preferences.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_rate) {
            rateApp();
        }
        if (id == R.id.action_overdue) {
            Intent intent = new Intent(this, Overdue.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void rateApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.rv150.bestbefore"));
        startActivity(intent);
    }

    public void onFabClick(View view) {
        Intent intent = new Intent(MainActivity.this, Add.class);
        startActivityForResult(intent, RequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String name;
        Calendar date;

        if (requestCode == RequestCode) {
            if (resultCode == 1) {                              // Добавление
                name = data.getExtras().getString("name");
                date = (Calendar) data.getExtras().get("date");
                Calendar createdAt = (Calendar) data.getExtras().get("createdAt");
                wrapperList.add(new StringWrapper(name, date, createdAt));
            } else if (resultCode == 2) {                       // Изменение
                name = data.getExtras().getString("name");
                date = (Calendar) data.getExtras().get("date");
                Calendar createdAt = (Calendar) data.getExtras().get("createdAt");
                wrapperList.set(position, new StringWrapper(name, date, createdAt));
                position = -1;
            }
            customAdapter.setData(wrapperList);
            customAdapter.notifyDataSetChanged();
            UpdatePreferences();
            TextView isEmpty = (TextView)findViewById(R.id.isEmptyText);
            if (wrapperList.isEmpty()) {
                isEmpty.setVisibility(View.VISIBLE);
            }
            else {
                isEmpty.setVisibility(View.INVISIBLE);
            }
        }
    }









    private void UpdatePreferences() {
        SharedPreferences.Editor editor = sPrefs.edit();
        for (int i = 0; i < wrapperList.size(); ++i) {
            Calendar temp = wrapperList.get(i).getDate();
            int myYear = temp.get(Calendar.YEAR);
            int myMonth = temp.get(Calendar.MONTH);
            int myDay = temp.get(Calendar.DAY_OF_MONTH);
            String str;
            if (myMonth < 9) {
                str = myDay + "." + "0" + myMonth + "." + myYear;
            } else {
                str = myDay + "." + myMonth + "." + myYear;
            }
            editor.putString(String.valueOf(i), wrapperList.get(i).getTitle());
            editor.putString(String.valueOf(i + 500), str);

            Calendar createdAt = wrapperList.get(i).createdAt();
            int DayCreated =  createdAt.get(Calendar.DAY_OF_MONTH);
            int MonthCreated = createdAt.get(Calendar.MONTH);
            int YearCreated = createdAt.get(Calendar.YEAR);
            int HourCreated = createdAt.get(Calendar.HOUR_OF_DAY);
            int MinuteCreated = createdAt.get(Calendar.MINUTE);
            int SecondCreated = createdAt.get(Calendar.SECOND);
            String createdAtStr = YearCreated + "." + MonthCreated + "." + DayCreated  + "." + HourCreated + "." + MinuteCreated + "." + SecondCreated;
            editor.putString(String.valueOf(i + 1000), createdAtStr);
        }
        editor.putString(String.valueOf(wrapperList.size()), ""); // признак конца списка
        editor.apply();
    }

    void loadFromPreferences() {
        wrapperList.clear();
        for (int i = 0; sPrefs.contains(String.valueOf(i)) ; ++i) {
            if (sPrefs.getString(String.valueOf(i), "").equals("") || i >= 500) {
                break;
            }
            final String title = sPrefs.getString(String.valueOf(i), "");
            final String date = sPrefs.getString(String.valueOf(i + 500), "0.0.0");
            String[] array = date.split("\\.");
            int myDay = Integer.parseInt(array[0]);
            int myMonth = Integer.parseInt(array[1]);
            int myYear = Integer.parseInt(array[2]);

            final String createdAtStr = sPrefs.getString(String.valueOf(i + 1000), "0.0.0.0.0.0");
            String[] createdAtSplit = createdAtStr.split("\\.");
            int YearCreated = Integer.parseInt(createdAtSplit[0]);
            int MonthCreated = Integer.parseInt(createdAtSplit[1]);
            int DayCreated = Integer.parseInt(createdAtSplit[2]);
            int HourCreated = Integer.parseInt(createdAtSplit[3]);
            int MinuteCreated = Integer.parseInt(createdAtSplit[4]);
            int SecondCreated = Integer.parseInt(createdAtSplit[5]);

            StringWrapper temp = new StringWrapper(title, new GregorianCalendar(myYear, myMonth, myDay), new GregorianCalendar(YearCreated, MonthCreated, DayCreated, HourCreated, MinuteCreated, SecondCreated));
            wrapperList.add(temp);
        }
    }

    @Override
    public void onBackPressed() {
        boolean needRate = sPrefs.getBoolean("needRate", true);
        int timesOpened = sPrefs.getInt("timesOpened", 0);
        if (needRate && timesOpened >= 10) {
                SharedPreferences.Editor editor = sPrefs.edit();
                editor.putBoolean("needRate", false);
                editor.apply();

                // Вызов окна с предложением оценить приложение
                DialogFragment dialog = new RateAppDialog();
                dialog.show(getFragmentManager(), "RateApp");
        }
        else {
            finish();
        }
    }

    public void closeApp() {
        finish();
    }
}
