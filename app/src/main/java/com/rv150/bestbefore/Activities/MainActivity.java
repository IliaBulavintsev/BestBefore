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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.DeleteOverdue;
import com.rv150.bestbefore.Dialogs.DeleteAllDialog;
import com.rv150.bestbefore.Dialogs.DeleteGroupDialog;
import com.rv150.bestbefore.Dialogs.RateAppDialog;
import com.rv150.bestbefore.ItemClickSupport;
import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Receivers.AlarmReceiver;
import com.rv150.bestbefore.RecyclerAdapter;
import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Services.DBHelper;
import com.rv150.bestbefore.Services.StatCollector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;



// открытый drawer при сворачивании - дублирование


public class MainActivity extends AppCompatActivity {
    private List<Product> wrapperList;
    private SharedPreferences sPrefs;
    private int position = -1;
    private RecyclerAdapter adapter;
    private RecyclerView rvProducts;
    private DBHelper dbHelper;
    private Product deletedProduct;
    private TextView isEmpty;
    private FloatingActionButton fab;
    private Toolbar toolbar;

    private ProductDAO productDAO;
    private GroupDAO groupDAO;

    private boolean firstLaunch;

    private Drawer drawer;
    private int drawerPosition;
    private long groupChoosen = Resources.ID_MAIN_GROUP;
    private long deletedFromThisGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        isEmpty = (TextView)findViewById(R.id.isEmptyText);
        fab = (FloatingActionButton) findViewById(R.id.fab);



        final String mainGroupName = sPrefs.getString(Resources.MAIN_GROUP_NAME, getString(R.string.all_products));
        setTitle(mainGroupName);


        // DB helper
        dbHelper = new DBHelper(getApplicationContext());
        productDAO = new ProductDAO(getApplicationContext());
        groupDAO = new GroupDAO(getApplicationContext());

        rvProducts = (RecyclerView) findViewById(R.id.rvProducts);
        setUpRecyclerView();




        // Что нового?
        firstLaunch = sPrefs.getBoolean(Resources.PREF_SHOW_WELCOME_SCREEN, true);

        SharedPreferences.Editor editor = sPrefs.edit();
        boolean needMigrate = sPrefs.getBoolean(Resources.NEED_MIGRATE, true);
        if (needMigrate) {
            if (!firstLaunch) {
                migrateToDB();
            }
            editor.putBoolean(Resources.NEED_MIGRATE, false);
            editor.apply();
        }


        wrapperList = productDAO.getFresh();


        // показ приветственного сообщения
        if (firstLaunch) {
            String whatsNewText = getResources().getString(R.string.welcomeText);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.welcomeTitle)
                    .setMessage(whatsNewText)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
            editor.putBoolean(Resources.PREF_SHOW_WELCOME_SCREEN, false);

            Calendar installedAt = Calendar.getInstance();
            int installDay = installedAt.get(Calendar.DAY_OF_MONTH);
            int installMonth = installedAt.get(Calendar.MONTH);
            int installYear = installedAt.get(Calendar.YEAR);
            editor.putInt(Resources.PREF_INSTALL_DAY, installDay);
            editor.putInt(Resources.PREF_INSTALL_MONTH, installMonth);
            editor.putInt(Resources.PREF_INSTALL_YEAR, installYear);

            // Установим название главной группы
            editor.putString(Resources.MAIN_GROUP_NAME, getString(R.string.all_products));
            editor.putString(Resources.OVERDUED_GROUP_NAME, getString(R.string.overdue_products));
            editor.apply();
        }
        else {
            editor.remove(Resources.CONGRATULATION);
            editor.apply();
        }



        if (firstLaunch) {
            StatCollector.shareStatistic(this, "First launch ");
        }
        else {
            StatCollector.shareStatistic(this, "EMPTY :( ");
        }
    }





    @Override
    protected void onResume() {
        super.onResume();
        // Стереть напоминания
        NotificationManager notifManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();


        // Update drawer with new groups
        setUpDrawer(toolbar);


        boolean needShowOverdue = sPrefs.getBoolean(Resources.SHOW_OVERDUE_DIALOG, true);
        if (needShowOverdue) {
            // Удаление просроченных и показ сообщения
            List<Product> temp = productDAO.getAll();        // берем все продукты из базы
            List<String> newOverdue = DeleteOverdue.getOverdueNamesAndRemoveFresh(temp);
            DeleteOverdue.markViewed(productDAO, temp);
            if (!newOverdue.isEmpty()) {
                CharSequence[] cs = newOverdue.toArray(new CharSequence[newOverdue.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.last_overdue);
                builder.setItems(cs, null);
                builder.show();
            }
        }

        sortMainList();

        if (firstLaunch) {
            // добавим пустой продукт как образец
            String name = getString(R.string.example_product);
            int quantity = 5;
            Calendar date = new GregorianCalendar();
            date.add(Calendar.DAY_OF_MONTH, 2);
            Product product = new Product(name, date, Calendar.getInstance(), quantity, -1);
            productDAO.insertProduct(product);
            wrapperList = productDAO.getAll();
            firstLaunch = false;
        }


        adapter = new RecyclerAdapter(wrapperList, getApplicationContext());
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
        boolean fourthNotif = sPrefs.getBoolean(Resources.PREF_FOURH_NOTIF, false);
        boolean fifthNotif = sPrefs.getBoolean(Resources.PREF_FIFTH_NOTIF, false);
        SharedPreferences.Editor editor = sPrefs.edit();
        if (wrapperList.isEmpty()) {
            AlarmReceiver am = new AlarmReceiver();
            am.cancelAlarm(this);
            editor.putBoolean(Resources.PREF_ALARM_SET, false);
        }
        else if(!alarm_set && (firstNotif || secondNotif || thirdNotif || fourthNotif || fifthNotif)){
            AlarmReceiver am = new AlarmReceiver();
            am.setAlarm(this);
            editor.putBoolean(Resources.PREF_ALARM_SET, true);
        }
        editor.apply();

        new InsertForAutocomplete(wrapperList).execute();
    }























    private void setUpDrawer(Toolbar toolbar) {
        drawerPosition = 2;
        final String mainGroupName = sPrefs.getString(Resources.MAIN_GROUP_NAME, getString(R.string.all_products));
        PrimaryDrawerItem allProducts = new PrimaryDrawerItem()
                .withIdentifier(Resources.ID_MAIN_GROUP)
                .withName(mainGroupName)
                .withIcon(GoogleMaterial.Icon.gmd_view_list);




        final String overdueGroupName = sPrefs.getString(Resources.OVERDUED_GROUP_NAME, getString(R.string.overdue_products));
        PrimaryDrawerItem overdued = new PrimaryDrawerItem()
                .withIdentifier(Resources.ID_FOR_OVERDUED)
                .withName(overdueGroupName)
                .withIcon(GoogleMaterial.Icon.gmd_history);

        PrimaryDrawerItem trash = new PrimaryDrawerItem()
                .withIdentifier(Resources.ID_FOR_TRASH)
                .withName(R.string.trash)
                .withIcon(GoogleMaterial.Icon.gmd_delete);


        PrimaryDrawerItem settings = new PrimaryDrawerItem()
                .withIdentifier(Resources.ID_FOR_SETTINGS)
                .withName(R.string.settings)
                .withSelectable(false)
                .withIcon(GoogleMaterial.Icon.gmd_settings);
        PrimaryDrawerItem feedback = new PrimaryDrawerItem()
                .withIdentifier(Resources.ID_FOR_FEEDBACK)
                .withName(R.string.feedback)
                .withSelectable(false)
                .withIcon(GoogleMaterial.Icon.gmd_email);

        boolean useGroups = sPrefs.getBoolean("use_groups", true);
        if (useGroups) {
            SecondaryDrawerItem addGroup = new SecondaryDrawerItem()
                    .withIdentifier(Resources.ID_FOR_ADD_GROUP)
                    .withName(R.string.add_group)
                    .withSelectable(false);
            drawer = new DrawerBuilder()
                    .withActivity(this)
                    .withToolbar(toolbar)
                    .withHeader(R.layout.drawer_header)
                    .addDrawerItems(
                            allProducts,
                            addGroup,
                            new DividerDrawerItem(),
                            overdued,
                            trash,
                            new DividerDrawerItem(),
                            settings,
                            feedback
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            drawerPushed(drawerItem);
                            return true;
                        }
                    })
                    .build();
        }
        else {
            drawer = new DrawerBuilder()
                    .withActivity(this)
                    .withToolbar(toolbar)
                    .withHeader(R.layout.drawer_header)
                    .addDrawerItems(
                            allProducts,
                            new DividerDrawerItem(),
                            overdued,
                            trash,
                            new DividerDrawerItem(),
                            settings,
                            feedback
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            drawerPushed(drawerItem);
                            return true;
                        }
                    })
                    .build();
        }



        List<Group> userGroups = groupDAO.getAll();
        for (Group group: userGroups) {
            PrimaryDrawerItem newItem = new PrimaryDrawerItem()
                    .withName(group.getName())
                    .withIdentifier(group.getId())
                    .withIcon(GoogleMaterial.Icon.gmd_view_list);
            drawer.addItemAtPosition(newItem, drawerPosition++);
        }
        if (groupChoosen == Resources.ID_MAIN_GROUP) {
            drawer.setSelectionAtPosition(1);
        }
        else if (groupChoosen == Resources.ID_FOR_OVERDUED) {
            drawer.setSelectionAtPosition(drawerPosition + 2); // не очень хорошо
        }
        else  {
            drawer.setSelection(groupChoosen);
        }

    }








    private void drawerPushed (IDrawerItem drawerItem) {
        drawer.closeDrawer();
        final long id = drawerItem.getIdentifier();
        if (id == Resources.ID_FOR_ADD_GROUP) {
            showAddGroupDialog();
            return;
        }
        if (id == Resources.ID_FOR_SETTINGS) {
            Intent intent = new Intent(this, Preferences.class);
            startActivityForResult(intent, Resources.RC_SETTINGS);
            return;
        }
        if (id == Resources.ID_FOR_FEEDBACK) {
            String [] addresses = {getString(R.string.developer_email)};
            String subject = getString(R.string.email_subject);
            composeEmail(addresses, subject);
            return;
        }
        // Смена группы
        groupChoosen = id;
        changeGroup();
    }


    private void changeGroup() {
        if (groupChoosen == Resources.ID_MAIN_GROUP) {
            wrapperList = productDAO.getFresh();
            final String mainGroupName = sPrefs.getString(Resources.MAIN_GROUP_NAME, getString(R.string.all_products));
            setTitle(mainGroupName);
            fab.show();
        }
        else if (groupChoosen == Resources.ID_FOR_OVERDUED) {
            wrapperList = productDAO.getOverdued();
            final String overdueGroupName = sPrefs.getString(Resources.OVERDUED_GROUP_NAME, getString(R.string.overdue_products));
            setTitle(overdueGroupName);
            fab.hide();
        }
        else if (groupChoosen == Resources.ID_FOR_TRASH) {
            wrapperList = productDAO.getRemoved();
            setTitle(R.string.trash);
            fab.hide();
        }
        else {  // Какая-то определенная категория
            Group group = groupDAO.get(groupChoosen);
            wrapperList = productDAO.getFreshFromGroup(group.getId());
            setTitle(group.getName());
            fab.show();
        }
        sortMainList();
        adapter = new RecyclerAdapter(wrapperList, getApplicationContext());
        rvProducts.swapAdapter(adapter, false);
        if (wrapperList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        }
        else {
            isEmpty.setVisibility(View.INVISIBLE);
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
            }
        });
        builder.show();
    }

    private void createGroup(String name) {
        Group newGroup = new Group(name);
        long id;
        try {
            id = groupDAO.insertGroup(newGroup);
        }
        catch (RuntimeException e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.group_with_this_name_already_exists, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        PrimaryDrawerItem newItem = new PrimaryDrawerItem()
                .withName(name)
                .withIdentifier(id)
                .withIcon(GoogleMaterial.Icon.gmd_view_list);
        drawer.addItemAtPosition(newItem, drawerPosition);
        drawer.setSelectionAtPosition(drawerPosition);
        drawerPosition++;
        groupChoosen = id;
        changeGroup();
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.group_was_created, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void clearGroup() {
        if (groupChoosen == Resources.ID_MAIN_GROUP) {
            productDAO.deleteFresh();
        }
        else if (groupChoosen == Resources.ID_FOR_OVERDUED) {
            productDAO.deleteOverdued();
        }
        else if (groupChoosen == Resources.ID_FOR_TRASH) {
            productDAO.clearTrash();
        }
        else {
            Group group = groupDAO.get(groupChoosen);
            productDAO.deleteFreshFromGroup(group.getId());
        }

        wrapperList.clear();
        adapter = new RecyclerAdapter(wrapperList, getApplicationContext());
        rvProducts.swapAdapter(adapter, false);
        isEmpty.setVisibility(View.VISIBLE);
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.group_was_cleared, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void deleteGroup() {
        groupDAO.deleteGroup(groupChoosen);
        drawer.removeItem(groupChoosen);
        drawerPosition--;
        drawer.setSelectionAtPosition(1);
        groupChoosen = Resources.ID_MAIN_GROUP;
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.group_was_deleted, Toast.LENGTH_SHORT);
        toast.show();
        changeGroup();
    }











    private void deleteItem() {
        deletedProduct = wrapperList.get(position);
        wrapperList.remove(position);
        if (wrapperList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        }
        adapter = new RecyclerAdapter(wrapperList, getApplicationContext());
        rvProducts.swapAdapter(adapter, false);

        if (groupChoosen == Resources.ID_FOR_TRASH ||
                deletedProduct.getTitle().equals(getString(R.string.example_product))) {
            productDAO.removeProductFromTrash(deletedProduct.getId());
        }
        else
        {
            productDAO.deleteProduct(deletedProduct.getId());
        }
        deletedFromThisGroup = groupChoosen;
    }

    private void undoRemove() {
        if (deletedFromThisGroup == Resources.ID_FOR_TRASH) {
            long id = productDAO.insertProduct(deletedProduct);
            deletedProduct.setId(id);
        }
        else {
            productDAO.markRestored(deletedProduct.getId());
        }

        if (deletedFromThisGroup == groupChoosen) {
            wrapperList.add(position, deletedProduct);
            isEmpty.setVisibility(View.INVISIBLE);
        }
        position = -1;
        adapter = new RecyclerAdapter(wrapperList, getApplicationContext());
        rvProducts.swapAdapter(adapter, false);
        deletedProduct = null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (groupChoosen == Resources.ID_FOR_TRASH) {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(true);
            menu.getItem(2).setVisible(false);
            menu.getItem(3).setVisible(false);
        } else if (groupChoosen == Resources.ID_MAIN_GROUP || groupChoosen == Resources.ID_FOR_OVERDUED) {
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(true);
            menu.getItem(2).setVisible(true);
            menu.getItem(3).setVisible(false);
        }
        else {
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(true);
            menu.getItem(2).setVisible(true);
            menu.getItem(3).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.sort)
                    .setItems(R.array.sortOptions, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = sPrefs.edit();
                            switch (which) {
                                case 0:
                                    editor.putString(Resources.PREF_HOW_TO_SORT, Resources.SPOILED_TO_FRESH);
                                    break;
                                case 1:
                                    editor.putString(Resources.PREF_HOW_TO_SORT, Resources.FRESH_TO_SPOILED);
                                    break;
                                case 2:
                                    editor.putString(Resources.PREF_HOW_TO_SORT, Resources.STANDART);
                                    break;
                                case 3:
                                    editor.putString(Resources.PREF_HOW_TO_SORT, Resources.BY_NAME);
                                    break;
                            }
                            editor.apply();
                            sortMainList();
                            adapter = new RecyclerAdapter(wrapperList, getApplicationContext());
                            rvProducts.swapAdapter(adapter, false);
                        }
                    })
                    .show();
            return true;
        }

        if (id == R.id.action_clear_list) {
            DialogFragment dialog = new DeleteAllDialog();
            dialog.show(getFragmentManager(), "deleteAll");
            return true;
        }

        if (id == R.id.action_rename_group) {
            showRenameGroupDialog();
            return true;
        }

        if (id == R.id.action_delete_group) {
            if (groupChoosen == Resources.ID_MAIN_GROUP || groupChoosen == Resources.ID_FOR_OVERDUED) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.you_can_not_delete_group, Toast.LENGTH_SHORT);
                toast.show();
                return true;
            }
            DialogFragment dialogDeleteGroup = new DeleteGroupDialog();
            dialogDeleteGroup.show(getFragmentManager(), "deleteGroup");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void rateApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.rv150.bestbefore"));
        startActivity(intent);
    }

    public void delayRateApp() {
        int hoursNeeded = sPrefs.getInt(Resources.PREF_HOURS_NEEDED, 72);
        hoursNeeded += 24;
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putInt(Resources.PREF_HOURS_NEEDED, hoursNeeded);
        editor.apply();
    }


    public void onFabClick(View view) {
        Intent intent = new Intent(MainActivity.this, Add.class);
        intent.putExtra(Resources.GROUP_ID, groupChoosen);
        startActivityForResult(intent, Resources.RC_ADD_ACTIVITY);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Resources.RC_ADD_ACTIVITY && resultCode != RESULT_CANCELED) {
            Product product = data.getExtras().getParcelable(Product.class.getName());
            if (product == null) {
                throw new RuntimeException("No extra data");
            }

            if (resultCode == Resources.RESULT_ADD) {                              // Добавление
                // Справка
                boolean showHelp = sPrefs.getBoolean(Resources.PREF_SHOW_HELP_AFTER_FIRST_ADD, true);
                if (showHelp) {
                    Calendar currentDate = Calendar.getInstance();
                    long difference = product.getDate().getTimeInMillis() - currentDate.getTimeInMillis();
                    int days = (int) (difference / (24 * 60 * 60 * 1000));
                    String msg;
                    if (days == 0 && difference >= 0) {
                        msg = getString(R.string.help_add_with_last_day);
                    }
                    else {
                        msg = getString(R.string.help_add);
                    }
                    new AlertDialog.Builder(this).setTitle(R.string.help)
                            .setMessage(msg)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                    SharedPreferences.Editor editor = sPrefs.edit();
                    editor.putBoolean(Resources.PREF_SHOW_HELP_AFTER_FIRST_ADD, false);
                    editor.apply();
                }

            } else if (resultCode == Resources.RESULT_MODIFY) {               // Изменение
                position = -1;
            }


            if (product.getGroupId() != -1) {
                groupChoosen = product.getGroupId();
            }
            else {
                groupChoosen = Resources.ID_MAIN_GROUP;
            }
            changeGroup();
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.product_has_been_saved, Toast.LENGTH_SHORT);
            toast.show();
        }
        if (requestCode == Resources.RC_SETTINGS) {
            adapter = new RecyclerAdapter(wrapperList, getApplicationContext());
            rvProducts.swapAdapter(adapter, false);
        }
    }







    @Override
    public void onBackPressed() {
        boolean needRate = sPrefs.getBoolean(Resources.PREF_NEED_RATE, true);
        if (needRate) {
                int installDay = sPrefs.getInt(Resources.PREF_INSTALL_DAY, 11);
                int installMonth = sPrefs.getInt(Resources.PREF_INSTALL_MONTH, 8);
                int installYear = sPrefs.getInt(Resources.PREF_INSTALL_YEAR, 2016);
                int hoursNeeded = sPrefs.getInt(Resources.PREF_HOURS_NEEDED, 72);
                Calendar installedAt = new GregorianCalendar(installYear, installMonth, installDay);
                Calendar now = new GregorianCalendar();
                final int MILLI_TO_HOUR = 1000 * 60 * 60;
                int hours = (int) ((now.getTimeInMillis() - installedAt.getTimeInMillis()) / MILLI_TO_HOUR);

                // кол-во часов с момента установки должно превысить это значение
                if (hours >= hoursNeeded) {
                    SharedPreferences.Editor editor = sPrefs.edit();
                    editor.putBoolean(Resources.PREF_NEED_RATE, false);
                    editor.remove(Resources.PREF_INSTALL_YEAR);
                    editor.remove(Resources.PREF_INSTALL_MONTH);
                    editor.remove(Resources.PREF_INSTALL_DAY);
                    editor.remove(Resources.PREF_HOURS_NEEDED);
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

        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvProducts.getContext(),
                DividerItemDecoration.VERTICAL);
        rvProducts.addItemDecoration(dividerItemDecoration);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0)
                {
                    fab.hide();
                }
                else {
                    if (groupChoosen != Resources.ID_FOR_OVERDUED &&
                            groupChoosen != Resources.ID_FOR_TRASH) {
                        fab.show();
                    }
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
            public void onItemClicked(RecyclerView recyclerView, final int pos, View v) {
                if (groupChoosen != Resources.ID_FOR_TRASH) {
                    position = pos;
                    runAddActivity(position);
                    return;
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.want_restore_this_product)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Product product = wrapperList.get(pos);
                                productDAO.markRestored(product.getId());
                                product.setRemoved(0);
                                if (!product.isFresh()) {
                                    product.setViewed(1);
                                    productDAO.updateProduct(product);
                                }
                                wrapperList.remove(pos);
                                adapter = new RecyclerAdapter(wrapperList, getApplicationContext());
                                rvProducts.swapAdapter(adapter, false);
                                if (wrapperList.isEmpty()) {
                                    isEmpty.setVisibility(View.VISIBLE);
                                }
                                else {
                                    isEmpty.setVisibility(View.INVISIBLE);
                                }
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        R.string.product_has_been_restored, Toast.LENGTH_SHORT);
                                toast.show();

                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();

            }
        });
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
                                undoRemove();
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
        final Product item = wrapperList.get(clickedPosition);
        Intent intent = new Intent(this, Add.class);
        intent.putExtra(Product.class.getName(), item);
        startActivityForResult(intent, Resources.RC_ADD_ACTIVITY);
    }


    private class InsertForAutocomplete extends AsyncTask<String, String, String> {

        List<Product> insertedData;
        InsertForAutocomplete(List<Product> insertedData) {
            this.insertedData = insertedData;
        }
        @Override
        protected String doInBackground(final String... args) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            for (Product item: insertedData) {
                final String name = item.getTitle();
                if (!name.equals(getString(R.string.example_product))) {
                    values.put(DBHelper.AutoCompletedProducts.COLUMN_NAME_NAME, name);
                    db.insert(DBHelper.AutoCompletedProducts.TABLE_NAME, null, values);
                }
            }
            return null;
        }
    }

    private void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void sortMainList() {
        if (groupChoosen == Resources.ID_FOR_OVERDUED) {
            Collections.sort(wrapperList, Product.getFreshToSpoiledComparator());
            return;
        }

        if (groupChoosen == Resources.ID_FOR_TRASH) {
            return;
        }

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
    }


    private void migrateToDB() {
        List<Product> fresh = ProductDAO.getFreshProducts(this);
        List<Product> overdue = ProductDAO.getOverdueProducts(this);
        for (Product product: overdue) {
            product.setViewed(1);
        }
        List<Product> all = new ArrayList<>();
        all.addAll(fresh);
        all.addAll(overdue);
        for (Product product: all) {
            Calendar date = product.getDate();
            date.set(Calendar.HOUR_OF_DAY, 23);
            date.set(Calendar.MINUTE, 59);
        }
        if (!all.isEmpty()) {
            productDAO.insertProducts(all);
        }
    }


    private void showRenameGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_new_name);

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
                    showRenameGroupDialog();
                }
                else {
                    result = result.substring(0, 1).toUpperCase() + result.substring(1);
                    renameGroup(result);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void renameGroup(String result) {
        Group group = groupDAO.get(result);
        if (group != null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.group_with_this_name_already_exists, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        int groupPosition = drawer.getPosition(groupChoosen);
        drawer.removeItemByPosition(groupPosition);
        PrimaryDrawerItem newItem;

        if (groupChoosen == Resources.ID_MAIN_GROUP) {
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putString(Resources.MAIN_GROUP_NAME, result);
            editor.apply();
            newItem = new PrimaryDrawerItem()
                    .withName(result)
                    .withIdentifier(Resources.ID_MAIN_GROUP)
                    .withIcon(GoogleMaterial.Icon.gmd_view_list);
        }
        else if (groupChoosen == Resources.ID_FOR_OVERDUED) {
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putString(Resources.OVERDUED_GROUP_NAME, result);
            editor.apply();
            newItem = new PrimaryDrawerItem()
                    .withName(result)
                    .withIdentifier(Resources.ID_FOR_OVERDUED)
                    .withIcon(GoogleMaterial.Icon.gmd_history);
        }
        else {
            group = groupDAO.get(groupChoosen);
            group.setName(result);
            groupDAO.updateGroup(group);
            newItem = new PrimaryDrawerItem()
                    .withName(result)
                    .withIdentifier(group.getId())
                    .withIcon(GoogleMaterial.Icon.gmd_view_list);
        }


        drawer.addItemAtPosition(newItem, groupPosition);
        drawer.setSelectionAtPosition(groupPosition);

        setTitle(result);
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.group_was_renamed, Toast.LENGTH_SHORT);
        toast.show();
    }
}
