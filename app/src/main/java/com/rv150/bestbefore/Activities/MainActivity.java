package com.rv150.bestbefore.Activities;


import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.rv150.bestbefore.ItemClickSupport;
import com.rv150.bestbefore.Receivers.AlarmReceiver;
import com.rv150.bestbefore.Dialogs.DeleteAllMain;
import com.rv150.bestbefore.DeleteOverdue;
import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Dialogs.RateAppDialog;
import com.rv150.bestbefore.RecyclerAdapter;
import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Preferences.SharedPrefsManager;
import com.rv150.bestbefore.Services.DBHelper;
import com.rv150.bestbefore.Services.StatCollector;
import com.rv150.bestbefore.Product;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    private List<Product> wrapperList;
    private SharedPreferences sPrefs;
    private int position = -1;
    private RecyclerAdapter adapter;
    private RecyclerView rvProducts;
    private DBHelper dbHelper;
    private Product deletedProduct;
    TextView isEmpty;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        isEmpty = (TextView)findViewById(R.id.isEmptyText);
        Typeface font = Typeface.createFromAsset(getAssets(), "san.ttf");
        isEmpty.setTypeface(font);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        rvProducts = (RecyclerView) findViewById(R.id.rvProducts);

        wrapperList = new ArrayList<>();
        adapter = new RecyclerAdapter(wrapperList);

        setUpRecyclerView();

        // DB helper
        dbHelper = new DBHelper(getApplicationContext());


        // Что нового?

        boolean showWelcomeScreen = sPrefs.getBoolean(Resources.PREF_SHOW_WELCOME_SCREEN, true);

        // показ приветственного сообщения
        if (showWelcomeScreen) {
            String whatsNewText = getResources().getString(R.string.welcomeText);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.welcomeTitle)
                    .setMessage(whatsNewText)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putBoolean(Resources.PREF_SHOW_WELCOME_SCREEN, false);
            editor.putBoolean(Resources.WHATS_NEW, false);

            Calendar installedAt = new GregorianCalendar();
            int installDay = installedAt.get(Calendar.DAY_OF_MONTH);
            int installMonth = installedAt.get(Calendar.MONTH);
            int installYear = installedAt.get(Calendar.YEAR);
            editor.putInt(Resources.PREF_INSTALL_DAY, installDay);
            editor.putInt(Resources.PREF_INSTALL_MONTH, installMonth);
            editor.putInt(Resources.PREF_INSTALL_YEAR, installYear);
            editor.apply();
        }
        else {
            // Справка
            boolean whatsNew = sPrefs.getBoolean(Resources.WHATS_NEW, true);
            if (whatsNew) {
                new AlertDialog.Builder(this).setTitle(R.string.whats_new_title)
                        .setMessage(R.string.whats_new)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                SharedPreferences.Editor editor = sPrefs.edit();
                editor.putBoolean(Resources.WHATS_NEW, false);
                editor.apply();
            }
        }


        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0)
                {
                    fab.hide();
                }
                else {
                    fab.show();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        ItemClickSupport.addTo(rvProducts).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int pos, View v) {
                position = pos;
                runAddActivity(position);
            }
        });


        if (showWelcomeScreen) {
            StatCollector.shareStatistic(this, "First launch ");
        }
        else {
            StatCollector.shareStatistic(this, "EMPTY :( ");
        }
    }




    @Override
    protected void onResume() {
        super.onResume();

        NotificationManager notifManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();

        wrapperList = SharedPrefsManager.getFreshProducts(this); // обновляем wrapperList в соотв. с сохраненными данными


        // Удаление просроченных
        List<String> newOverdue = DeleteOverdue.delete(this, wrapperList);
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
                    Collections.sort(wrapperList, Product.getStandartComparator());
                    break;
                case Resources.SPOILED_TO_FRESH:
                    Collections.sort(wrapperList, Product.getSpoiledToFreshComparator());
                    break;
                case Resources.FRESH_TO_SPOILED:
                    Collections.sort(wrapperList, Product.getFreshToSpoiledComparator());
                    break;
                case Resources.BY_NAME:
                    Collections.sort(wrapperList, Product.getByNameComparator());
                    break;
                default:
                    break;
            }
        }



        SharedPrefsManager.saveFreshProducts(wrapperList, this); // Сохраняем возможные изменения


        adapter = new RecyclerAdapter(wrapperList);
        rvProducts.swapAdapter(adapter, false);

        // Надпись "Список пуст!"
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

        new InsertDataTask(wrapperList).execute();
    }








    public void deleteItem() {
        deletedProduct = wrapperList.get(position);
        wrapperList.remove(position);
        TextView isEmpty = (TextView)findViewById(R.id.isEmptyText);
        if (wrapperList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        }
        else {
            isEmpty.setVisibility(View.INVISIBLE);
        }
        adapter = new RecyclerAdapter(wrapperList);
        rvProducts.swapAdapter(adapter, false);
        SharedPrefsManager.saveFreshProducts(wrapperList, this);
        StatCollector.shareStatistic(this, "deleted one item");
    }

    private void restoreItem() {
        wrapperList.add(position, deletedProduct);
        deletedProduct = null;
        position = -1;
        adapter = new RecyclerAdapter(wrapperList);
        rvProducts.swapAdapter(adapter, false);
        SharedPrefsManager.saveFreshProducts(wrapperList, this);
        StatCollector.shareStatistic(this, "restored item");

        // Надпись "Список пуст!"
        isEmpty.setVisibility(View.INVISIBLE);
    }

    public void deleteAll() {
        wrapperList.clear();
        adapter = new RecyclerAdapter(wrapperList);
        rvProducts.swapAdapter(adapter, false);
        SharedPrefsManager.saveFreshProducts(wrapperList, this);
        TextView isEmpty = (TextView)findViewById(R.id.isEmptyText);
        isEmpty.setVisibility(View.VISIBLE);
        StatCollector.shareStatistic(this, "deleted all fresh products");
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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Preferences.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_overdue) {
            Intent intent = new Intent(this, Overdue.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_delete_all) {
            DialogFragment dialog_delete_all = new DeleteAllMain();
            dialog_delete_all.show(getFragmentManager(), "deleteAll");
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
            int quantity = (int) data.getExtras().get(Resources.QUANTITY);

            if (resultCode == Resources.RESULT_ADD) {                              // Добавление
                wrapperList.add(new Product(name, date, createdAt, quantity));

                // Справка
                boolean showHelp = sPrefs.getBoolean(Resources.PREF_SHOW_HELP_AFTER_FIRST_ADD, true);
                if (showHelp) {
                    new AlertDialog.Builder(this).setTitle(R.string.help)
                            .setMessage(R.string.help_add)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                    SharedPreferences.Editor editor = sPrefs.edit();
                    editor.putBoolean(Resources.PREF_SHOW_HELP_AFTER_FIRST_ADD, false);
                    editor.apply();
                }
            } else if (resultCode == Resources.RESULT_MODIFY) {                       // Изменение
                wrapperList.set(position, new Product(name, date, createdAt, quantity));
                position = -1;
            }


            adapter = new RecyclerAdapter(wrapperList);
            rvProducts.swapAdapter(adapter, false);

            SharedPrefsManager.saveFreshProducts(wrapperList, this);    // Сохраняем данные


            TextView isEmpty = (TextView) findViewById(R.id.isEmptyText);
            if (wrapperList.isEmpty()) {
                isEmpty.setVisibility(View.VISIBLE);
            } else {
                isEmpty.setVisibility(View.INVISIBLE);
            }

            StatCollector.shareStatistic(this, null);
        }
    }







    @Override
    public void onBackPressed() {
        boolean needRate = sPrefs.getBoolean(Resources.PREF_NEED_RATE, true);
        if (needRate) {
                int installDay = sPrefs.getInt(Resources.PREF_INSTALL_DAY, 11);
                int installMonth = sPrefs.getInt(Resources.PREF_INSTALL_MONTH, 8);
                int installYear = sPrefs.getInt(Resources.PREF_INSTALL_YEAR, 2016);
                Calendar installedAt = new GregorianCalendar(installYear, installMonth, installDay);
                Calendar now = new GregorianCalendar();
                final int MILLI_TO_HOUR = 1000 * 60 * 60;
                int hours = (int) ((now.getTimeInMillis() - installedAt.getTimeInMillis()) / MILLI_TO_HOUR);

                // кол-во часов с момента установки должно превысить это значение
                if (hours >= 100) {
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

    public void finishAct() {
        finish();
    }


    private void setUpRecyclerView() {
        // Attach the adapter to the recyclerview to populate items


        rvProducts.setAdapter(adapter);
        // Set layout manager to position the items
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvProducts.getContext(),
                DividerItemDecoration.VERTICAL);
        rvProducts.addItemDecoration(dividerItemDecoration);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
    }


    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                xMark = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_clear_24dp);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) MainActivity.this.getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                position = viewHolder.getAdapterPosition();
                deleteItem();
                View parentLayout = findViewById(R.id.rvProducts);
                Snackbar snackbar = Snackbar
                        .make(parentLayout, R.string.product_has_been_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                restoreItem();
                            }
                        });

                snackbar.show();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(rvProducts);
    }

    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to thier new positions
     * after an item is removed.
     */
    private void setUpAnimationDecoratorHelper() {
        rvProducts.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }


    private void runAddActivity(int clickedPosition) {
        Intent intent = new Intent(this, Add.class);
        final Product item = wrapperList.get(clickedPosition);
        intent.putExtra(Resources.NAME,item.getTitle());
        Calendar date = item.getDate();
        int myDay = date.get(Calendar.DAY_OF_MONTH);
        int myMonth = date.get(Calendar.MONTH);
        int myYear = date.get(Calendar.YEAR);
        intent.putExtra(Resources.MY_DAY, myDay);
        intent.putExtra(Resources.MY_MONTH, myMonth);
        intent.putExtra(Resources.MY_YEAR, myYear);

        Calendar createdAt = item.getCreatedAt();
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

        intent.putExtra(Resources.QUANTITY, item.getQuantity());
        startActivityForResult(intent, Resources.RC_ADD_ACTIVITY);
    }


    private class InsertDataTask extends AsyncTask<String, String, String> {

        List<Product> insertedData;
        InsertDataTask(List<Product> insertedData) {
            this.insertedData = insertedData;
        }
        @Override
        protected String doInBackground(final String... args) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            for (Product item: insertedData) {
                values.put(DBHelper.AutoCompletedProducts.COLUMN_NAME_NAME, item.getTitle());
                db.insert(DBHelper.AutoCompletedProducts.TABLE_NAME, null, values);
            }
            return null;
        }
    }
}
