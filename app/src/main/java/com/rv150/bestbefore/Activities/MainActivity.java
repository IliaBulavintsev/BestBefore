package com.rv150.bestbefore.Activities;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.rv150.bestbefore.Adapters.RecyclerAdapter;
import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.DeleteOverdue;
import com.rv150.bestbefore.Dialogs.DeleteAllDialog;
import com.rv150.bestbefore.Dialogs.DeleteGroupDialog;
import com.rv150.bestbefore.Dialogs.RateAppDialog;
import com.rv150.bestbefore.Exceptions.DuplicateEntryException;
import com.rv150.bestbefore.ItemClickSupport;
import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Receivers.AlarmReceiver;
import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Services.DBHelper;
import com.rv150.bestbefore.Services.FileService;
import com.rv150.bestbefore.Services.IAmHere;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, RecyclerAdapter.ZoomAnimation {
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

    private Drawer drawer;
    private int drawerPosition;
    private long groupChoosen = Resources.ID_MAIN_GROUP;
    private long lastChoosenGroup = Resources.ID_MAIN_GROUP;
    private long deletedFromThisGroup;

    private boolean doubleBackToExitPressedOnce = false;

    private int mShortAnimationDuration;
    private Animator mCurrentAnimator;
    private ImageView imageView;
    private View thumbView;
    private boolean zoomIn = false;
    private float startScaleFinal;
    private Rect startBounds;

    private SearchView mSearchView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        isEmpty = (TextView)findViewById(R.id.isEmptyText);
        isEmpty.bringToFront();
        fab = (FloatingActionButton) findViewById(R.id.fab);



        final String mainGroupName = sPrefs.getString(Resources.MAIN_GROUP_NAME, getString(R.string.all_products));
        setTitle(mainGroupName);


        // DB helper
        dbHelper = DBHelper.getInstance(getApplicationContext());
        productDAO = ProductDAO.getInstance(getApplicationContext());
        groupDAO = GroupDAO.getInstance(getApplicationContext());

        rvProducts = (RecyclerView) findViewById(R.id.rvProducts);
        setUpRecyclerView();



        boolean firstLaunch = sPrefs.getBoolean(Resources.PREF_SHOW_WELCOME_SCREEN, true);

        SharedPreferences.Editor editor = sPrefs.edit();

        wrapperList = productDAO.getFresh();


        // показ приветственного сообщения
        if (firstLaunch) {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.hi)
                    .setMessage(R.string.welcome_text)
                    .setPositiveButton(R.string.ok, null)
                    .setCancelable(false)
                    .show();

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
            boolean whatsNew = sPrefs.getBoolean(Resources.WHATS_NEW_38, true);
            if (whatsNew) {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(R.string.whats_new_title)
                        .setMessage(R.string.whats_new_excel)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }

        editor.remove(Resources.NEED_MIGRATE);
        editor.remove(Resources.CONGRATULATION);
        editor.remove(Resources.PREF_SHOW_SYNC_WARNING);
        editor.remove(Resources.WHATS_NEW_34);
        editor.putBoolean(Resources.WHATS_NEW_38, false);
        editor.apply();

        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }







    @Override
    protected void onResume() {
        super.onResume();
        // Стереть напоминания
        NotificationManager notifManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();


        boolean needShowOverdue = sPrefs.getBoolean(Resources.SHOW_OVERDUE_DIALOG, true);
        if (needShowOverdue) {
            // Удаление просроченных и показ сообщения
            List<Product> temp = productDAO.getAllNotRemoved();        // берем все продукты из базы
            List<String> newOverdue = DeleteOverdue.getOverdueNamesAndRemoveFresh(getApplicationContext(), temp);
            DeleteOverdue.markViewed(productDAO, temp);
            if (!newOverdue.isEmpty()) {
                CharSequence[] cs = newOverdue.toArray(new CharSequence[newOverdue.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.last_overdue);
                builder.setItems(cs, null);
                builder.show();
            }
        }

        // Update drawer with new groups and other changes
        setUpDrawer(toolbar);

        sortMainList();
        adapter = new RecyclerAdapter(wrapperList, getApplicationContext(), this);
        rvProducts.swapAdapter(adapter, false);

        if (mSearchView != null) {
            mSearchView.setIconified(true);
            rvProducts.requestFocus();
        }

        // Надпись "Список пуст!"
        if (wrapperList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        }
        else {
            isEmpty.setVisibility(View.INVISIBLE);
        }

        boolean alarm_set = sPrefs.getBoolean(Resources.PREF_ALARM_SET, false);
        SharedPreferences.Editor editor = sPrefs.edit();
        if (wrapperList.isEmpty()) {
            AlarmReceiver am = new AlarmReceiver();
            am.cancelAlarm(this);
            editor.putBoolean(Resources.PREF_ALARM_SET, false);
        }
        else if(!alarm_set) {
            boolean firstNotif = sPrefs.getBoolean(Resources.PREF_FIRST_NOTIF, true);
            boolean secondNotif = sPrefs.getBoolean(Resources.PREF_SECOND_NOTIF, false);
            boolean thirdNotif = sPrefs.getBoolean(Resources.PREF_THIRD_NOTIF, false);
            boolean fourthNotif = sPrefs.getBoolean(Resources.PREF_FOURH_NOTIF, false);
            boolean fifthNotif = sPrefs.getBoolean(Resources.PREF_FIFTH_NOTIF, false);
            if (firstNotif || secondNotif || thirdNotif || fourthNotif || fifthNotif) {
                AlarmReceiver am = new AlarmReceiver();
                am.setAlarm(this);
                editor.putBoolean(Resources.PREF_ALARM_SET, true);
            }
        }
        editor.apply();
        new InsertForAutocomplete(wrapperList).execute();


        new Thread(new IAmHere(this)).start();
    }










    private void setUpDrawer(Toolbar toolbar) {
        drawerPosition = 2;
        final String mainGroupName = sPrefs.getString(Resources.MAIN_GROUP_NAME, getString(R.string.all_products));
        int mainGroupCount = productDAO.getFreshCount();

        PrimaryDrawerItem allProducts = new PrimaryDrawerItem()
                .withIdentifier(Resources.ID_MAIN_GROUP)
                .withName(mainGroupName + " (" + mainGroupCount + ')')
                .withIcon(GoogleMaterial.Icon.gmd_view_list);

        int overduedCount = productDAO.getOverduedCount();
        final String overdueGroupName = sPrefs.getString(Resources.OVERDUED_GROUP_NAME, getString(R.string.overdue_products));
        PrimaryDrawerItem overdued = new PrimaryDrawerItem()
                .withIdentifier(Resources.ID_FOR_OVERDUED)
                .withName(overdueGroupName + " (" + overduedCount + ')')
                .withIcon(GoogleMaterial.Icon.gmd_history);

        final String trashName = getString(R.string.trash);
        int trashCount = productDAO.getRemovedCount();
        PrimaryDrawerItem trash = new PrimaryDrawerItem()
                .withIdentifier(Resources.ID_FOR_TRASH)
                .withName(trashName + " (" + trashCount + ')')
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
        PrimaryDrawerItem about = new PrimaryDrawerItem()
                .withIdentifier(Resources.ID_FOR_BILLING)
                .withName(R.string.give_thanks)
                .withSelectable(false)
                .withIcon(GoogleMaterial.Icon.gmd_attach_money);

        boolean useGroups = sPrefs.getBoolean(Resources.PREF_USE_GROUPS, true);
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
                            feedback,
                            about
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
                            feedback,
                            about
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
            int productsInGroup = productDAO.getCountForGroup(group.getId());
            PrimaryDrawerItem newItem = new PrimaryDrawerItem()
                    .withName(group.getName() + " (" + productsInGroup + ')')
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
        if (id == Resources.ID_FOR_BILLING) {
            Intent intent = new Intent(this, BillingActivity.class);
            startActivity(intent);
            return;
        }

        // сохраняем старое значение
        if (groupChoosen == Resources.ID_FOR_TRASH || groupChoosen == Resources.ID_FOR_OVERDUED) {
            lastChoosenGroup = Resources.ID_MAIN_GROUP;
        }
        else {
            lastChoosenGroup = groupChoosen;
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
            lastChoosenGroup = Resources.ID_MAIN_GROUP;
        }
        sortMainList();
        adapter = new RecyclerAdapter(wrapperList, getApplicationContext(), this);
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
        catch (DuplicateEntryException e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.group_with_this_name_already_exists, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        PrimaryDrawerItem newItem = new PrimaryDrawerItem()
                .withName(name + "(0)")
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
        adapter = new RecyclerAdapter(wrapperList, getApplicationContext(), this);
        rvProducts.swapAdapter(adapter, false);
        isEmpty.setVisibility(View.VISIBLE);
        setUpDrawer(toolbar);
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
        deletedProduct = adapter.getItem(position);
        wrapperList.remove(deletedProduct);
        if (wrapperList.isEmpty()) {
            isEmpty.setVisibility(View.VISIBLE);
        }
        adapter = new RecyclerAdapter(wrapperList, getApplicationContext(), this);
        rvProducts.swapAdapter(adapter, false);

        if (groupChoosen == Resources.ID_FOR_TRASH) {
            productDAO.removeProductFromTrash(deletedProduct.getId());
        }
        else
        {
            productDAO.deleteProduct(deletedProduct.getId());
        }
        deletedFromThisGroup = groupChoosen;
        // Пробуем пересоздать drawer для обновления счетчиков
        setUpDrawer(toolbar);
        if (mSearchView != null) {
            mSearchView.setIconified(true);
            rvProducts.requestFocus();
        }
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
        adapter = new RecyclerAdapter(wrapperList, getApplicationContext(), this);
        rvProducts.swapAdapter(adapter, false);
        deletedProduct = null;
        // Пробуем пересоздать drawer для обновления счетчиков
        setUpDrawer(toolbar);
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            mSearchView = (SearchView) searchItem.getActionView();
        }
        if (mSearchView != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
            mSearchView.setOnQueryTextListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }









    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {    // idx 0 - это поиск!!!

        if (groupChoosen == Resources.ID_FOR_TRASH) {
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(true);
            menu.getItem(3).setVisible(false);
            menu.getItem(4).setVisible(false);
        } else if (groupChoosen == Resources.ID_MAIN_GROUP || groupChoosen == Resources.ID_FOR_OVERDUED) {
            menu.getItem(1).setVisible(true);
            menu.getItem(2).setVisible(true);
            menu.getItem(3).setVisible(true);
            menu.getItem(4).setVisible(false);
        }
        else {
            menu.getItem(1).setVisible(true);
            menu.getItem(2).setVisible(true);
            menu.getItem(3).setVisible(true);
            menu.getItem(4).setVisible(true);
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
                            adapter = new RecyclerAdapter(wrapperList, getApplicationContext(), MainActivity.this);
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
        finishAct();
    }


    public void onFabClick(View view) {
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        intent.putExtra(Resources.GROUP_ID, groupChoosen);
        intent.putExtra(Resources.STATUS, Resources.STATUS_ADD);
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
                            })
                            .setCancelable(false)
                            .show();
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
            adapter = new RecyclerAdapter(wrapperList, getApplicationContext(), this);
            rvProducts.swapAdapter(adapter, false);
        }
    }







    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
            return;
        }

        if (zoomIn) {
            zoomOutImage();
            return;
        }

        if (groupChoosen != Resources.ID_MAIN_GROUP) {
            groupChoosen = lastChoosenGroup;
            changeGroup();
            drawer.setSelection(groupChoosen);
            lastChoosenGroup = Resources.ID_MAIN_GROUP;
            return;
        }


        boolean needRate = sPrefs.getBoolean(Resources.PREF_NEED_RATE, true);
        if (needRate) {
            int installDay = sPrefs.getInt(Resources.PREF_INSTALL_DAY, 11);
            int installMonth = sPrefs.getInt(Resources.PREF_INSTALL_MONTH, 8);
            int installYear = sPrefs.getInt(Resources.PREF_INSTALL_YEAR, 2016);
            int hoursNeeded = sPrefs.getInt(Resources.PREF_HOURS_NEEDED, 72);
            Calendar installedAt = new GregorianCalendar(installYear, installMonth, installDay);
            Calendar now = Calendar.getInstance();
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
                dialog.setCancelable(false);
                dialog.show(getFragmentManager(), "RateApp");
            }
            else {
                checkDoubleClick();
            }
        }
        else {
            checkDoubleClick();
        }
    }

    private void checkDoubleClick() {
        boolean noDoubleClickNeeded = !sPrefs.getBoolean("double_click_to_exit", true);
        if (noDoubleClickNeeded || doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_twice_to_exit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void finishAct() {
        finish();
    }


    private void setUpRecyclerView() {

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setHasFixedSize(true);
        rvProducts.setItemViewCacheSize(20);
        rvProducts.setDrawingCacheEnabled(true);
        rvProducts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

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
                                adapter = new RecyclerAdapter(wrapperList, getApplicationContext(), MainActivity.this);
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

                                // Обновляем счетчики
                                setUpDrawer(toolbar);
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
                        xMark.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                                R.color.md_light_background), PorterDuff.Mode.SRC_ATOP);
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
                super.onDraw(c, parent, state);
            }

        });
    }


    private void runAddActivity(int clickedPosition) {
        final Product item = adapter.getItem(clickedPosition);
        Intent intent = new Intent(this, AddActivity.class);
        intent.putExtra(Resources.STATUS, Resources.STATUS_EDIT);
        intent.putExtra(Product.class.getName(), (Parcelable) item);
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
                if (name != null && !name.isEmpty()) {
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

    @Override
    protected void onStop() {
        super.onStop();
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        }
    }

    public void zoom(final View thumbView, long photo) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        this.thumbView = thumbView;

        // Load the high-resolution "zoomed-in" image.
        this.imageView = (ImageView) findViewById(
                R.id.expanded_image);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        imageView.setMinimumHeight(dm.widthPixels / 3 * 4);
        imageView.setMinimumWidth(dm.widthPixels);

        final Bitmap bitmap = FileService.getBitmapFromFileId(getApplicationContext(), photo);
        try {
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, imageView.getWidth(),
                            imageView.getHeight(), false));
                }
            });
        }
        catch (Exception ex) {
            Log.e(MainActivity.class.getSimpleName(), ex.getLocalizedMessage());
            return;
        }



        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        imageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        imageView.setPivotX(0f);
        imageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(imageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(imageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(imageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(imageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        this.startScaleFinal = startScale;
        zoomIn = true;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomOutImage();
            }
        });
    }

    public void zoomOutImage() {
            if (mCurrentAnimator != null) {
                mCurrentAnimator.cancel();
            }

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator
                    .ofFloat(imageView, View.X, startBounds.left))
                    .with(ObjectAnimator
                            .ofFloat(imageView,
                                    View.Y,startBounds.top))
                    .with(ObjectAnimator
                            .ofFloat(imageView,
                                    View.SCALE_X, startScaleFinal))
                    .with(ObjectAnimator
                            .ofFloat(imageView,
                                    View.SCALE_Y, startScaleFinal));
            set.setDuration(mShortAnimationDuration);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    thumbView.setAlpha(1f);
                    imageView.setVisibility(View.GONE);
                    mCurrentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    thumbView.setAlpha(1f);
                    imageView.setVisibility(View.GONE);
                    mCurrentAnimator = null;
                }
            });
            set.start();
            mCurrentAnimator = set;
            zoomIn = false;
        }
}

