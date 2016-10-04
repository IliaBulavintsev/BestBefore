package com.rv150.bestbefore;


import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;


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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;



// Все настройки чистятся в UpdatePreferences() и в классе DeleteOverdue

// Перевести дни в месяцы

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private ListView listView;
    private CustomAdapter customAdapter;
    private List<StringWrapper> wrapperList;
    private SharedPreferences sPrefs;
    private int position = -1;

    private GoogleApiClient mGoogleApiClient;


    
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


        SharedPreferences.Editor editor = sPrefs.edit();

        // Что нового?
        boolean showWhatsNewIn11 = sPrefs.getBoolean(Resources.PREF_WHATSNEW_11, true);
        boolean showWelcomeScreen = sPrefs.getBoolean(Resources.PREF_SHOW_WELCOME_SCREEN, true);
        if (showWhatsNewIn11 && !showWelcomeScreen) {
            new AlertDialog.Builder(this).setTitle(R.string.whats_new).setMessage("В настройках теперь можно выбрать отображение даты окончания срока годности продукта вместо количества оставшихся дней.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
        editor.putBoolean(Resources.PREF_WHATSNEW_11, false);
        editor.apply();


        // показ приветственного сообщения
        if (showWelcomeScreen) {
            String whatsNewText = getResources().getString(R.string.welcomeText);
            new AlertDialog.Builder(this).setTitle(R.string.welcomeTitle).setMessage(whatsNewText).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            editor.putBoolean(Resources.PREF_SHOW_WELCOME_SCREEN, false);

            Calendar installedAt = new GregorianCalendar();
            int installDay = installedAt.get(Calendar.DAY_OF_MONTH);
            int installMonth = installedAt.get(Calendar.MONTH);
            int installYear = installedAt.get(Calendar.YEAR);
            editor.putInt(Resources.PREF_INSTALL_DAY, installDay);
            editor.putInt(Resources.PREF_INSTALL_MONTH, installMonth);
            editor.putInt(Resources.PREF_INSTALL_YEAR, installYear);

            editor.apply();
        }


        // Инкремент счетчика открываний приложения
        boolean needRate = sPrefs.getBoolean(Resources.PREF_NEED_RATE, true);
        if (needRate) {
            int timesOpened = sPrefs.getInt(Resources.PREF_TIMES_OPENED, 0);
            timesOpened++;
            editor.putInt(Resources.PREF_TIMES_OPENED, timesOpened);
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






        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.CLIENT_ID))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, Resources.RC_SIGN_IN);
            }
        });
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("Google API", "connectionResult.getErrorMessage()");

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
        boolean needShowOverdue = sPrefs.getBoolean(Resources.SHOW_OVERDUE_DIALOG, true);

        if (needShowOverdue  && !newOverdue.isEmpty()) {
            CharSequence[] cs = newOverdue.toArray(new CharSequence[newOverdue.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.last_overdue);
            builder.setItems(cs, null);
            builder.show();
        }





        // Сортировка
        if (wrapperList.size() >= 2) {
            String howToSort = sPrefs.getString(Resources.PREF_HOW_TO_SORT, Resources.STANDART);
            switch (howToSort) {
                case Resources.STANDART:
                    Collections.sort(wrapperList, StringWrapper.getStandartComparator());
                    break;
                case Resources.SPOILED_TO_FRESH:
                    Collections.sort(wrapperList, StringWrapper.getSpoiledToFreshComparator());
                    break;
                case Resources.FRESH_TO_SPOILED:
                    Collections.sort(wrapperList, StringWrapper.getFreshToSpoiledComparator());
                    break;
                default:
                    break;
            }
        }



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

        boolean alarm_set = sPrefs.getBoolean(Resources.PREF_ALARM_SET, false);
        boolean firstNotif = sPrefs.getBoolean(Resources.PREF_FIRST_NOTIF, true);
        boolean secondNotif = sPrefs.getBoolean(Resources.PREF_SECOND_NOTIF, false);
        boolean thirdNotif = sPrefs.getBoolean(Resources.PREF_THIRD_NOTIF, false);
        SharedPreferences.Editor editor = sPrefs.edit();
        if (wrapperList.isEmpty()) {
            AlarmReceiver am = new AlarmReceiver();
            am.cancelAlarm(this);
            editor.putBoolean(Resources.PREF_ALARM_SET, false);
        }
        else if(!alarm_set && (firstNotif || secondNotif || thirdNotif)){
            AlarmReceiver am = new AlarmReceiver();
            am.setAlarm(this);
            editor.putBoolean(Resources.PREF_ALARM_SET, true);
        }
        editor.apply();
    }







    public void OptionChoosed(int option) {
        final int MODIFY = 0;
        final int DELETE = 1;
        switch (option) {
            case MODIFY:
                Intent intent = new Intent(MainActivity.this, Add.class);
                intent.putExtra(Resources.NAME, wrapperList.get(position).getTitle());
                Calendar date = wrapperList.get(position).getDate();
                int myDay = date.get(Calendar.DAY_OF_MONTH);
                int myMonth = date.get(Calendar.MONTH);
                int myYear = date.get(Calendar.YEAR);
                intent.putExtra(Resources.MY_DAY, myDay);
                intent.putExtra(Resources.MY_MONTH, myMonth);
                intent.putExtra(Resources.MY_YEAR, myYear);

                Calendar createdAt = wrapperList.get(position).createdAt();
                int DayCreated = createdAt.get(Calendar.DAY_OF_MONTH);
                int MonthCreated = createdAt.get(Calendar.MONTH);
                int YearCreated = createdAt.get(Calendar.YEAR);
                int HourCreated = createdAt.get(Calendar.HOUR_OF_DAY);
                int MinuteCreated = createdAt.get(Calendar.MINUTE);
                int SecondCreated = createdAt.get(Calendar.SECOND);
                intent.putExtra(Resources.DAY_CREATED, DayCreated);
                intent.putExtra(Resources.MONTH_CREATED, MonthCreated);
                intent.putExtra(Resources.YEAR_CREATED, YearCreated);
                intent.putExtra(Resources.HOUR_CREATED, HourCreated);
                intent.putExtra(Resources.MINUTE_CREATED, MinuteCreated);
                intent.putExtra(Resources.SECOND_CREATED, SecondCreated);

                startActivityForResult(intent, Resources.RC_ADD_ACTIVITY);
                break;

            case DELETE:
                DialogFragment dialog = new YesNoDialog();
                dialog.show(getFragmentManager(), "YesNo");
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
            return true;
        }
        if (id == R.id.action_overdue) {
            Intent intent = new Intent(this, Overdue.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_delete_all) {
            DialogFragment dialog_delete_all = new DeleteAllDialog();
            dialog_delete_all.show(getFragmentManager(), "DeleteAll");
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
        startActivityForResult(intent, Resources.RC_ADD_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Resources.RC_ADD_ACTIVITY && resultCode != Resources.RESULT_EXIT) {
            String name = data.getExtras().getString(Resources.NAME);
            Calendar date = (Calendar) data.getExtras().get(Resources.DATE);
            Calendar createdAt = (Calendar) data.getExtras().get(Resources.CREATED_AT);

            if (resultCode == Resources.RESULT_ADD) {                              // Добавление
                wrapperList.add(new StringWrapper(name, date, createdAt));
            } else if (resultCode == Resources.RESULT_MODIFY) {                       // Изменение
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


        if (requestCode == Resources.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            try {
                Log.i("SIGNED IN", acct.getDisplayName());
                Log.i("SIGNED IN", "TOKEN: " + acct.getIdToken());
                final String idToken = acct.getIdToken();
                new AsyncHttpPost().execute(idToken);
            }
            catch (Exception e) {return;}

        } else {
            // Signed out, show unauthenticated UI.
            Log.i("SIGNED IN", "result is not success");
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
        boolean needRate = sPrefs.getBoolean(Resources.PREF_NEED_RATE, true);
        int timesOpened = sPrefs.getInt(Resources.PREF_TIMES_OPENED, 0);
        if (needRate && timesOpened >= 10) {

                int installDay = sPrefs.getInt(Resources.PREF_INSTALL_DAY, 11);
                int installMonth = sPrefs.getInt(Resources.PREF_INSTALL_MONTH, 8);
                int installYear = sPrefs.getInt(Resources.PREF_INSTALL_YEAR, 2016);
                Calendar installedAt = new GregorianCalendar(installYear, installMonth, installDay);
                Calendar now = new GregorianCalendar();
                final int MILLI_TO_HOUR = 1000 * 60 * 60;
                int hours = (int) ((now.getTimeInMillis() - installedAt.getTimeInMillis()) / MILLI_TO_HOUR);

                // кол-во часов с момента установки должно превысить 48
                if (hours >= 48) {
                    SharedPreferences.Editor editor = sPrefs.edit();
                    editor.putBoolean(Resources.PREF_NEED_RATE, false);
                    editor.remove(Resources.PREF_INSTALL_YEAR);
                    editor.remove(Resources.PREF_INSTALL_MONTH);
                    editor.remove(Resources.PREF_INSTALL_DAY);
                    editor.apply();

                    // Вызов окна с предложением оценить приложение
                    DialogFragment dialog = new RateAppDialog();
                    dialog.show(getFragmentManager(), "RateApp");
                }
                else {
                    finish();
                }
        }
        else {
            finish();
        }
    }

    public void closeApp() {
        finish();
    }
}
