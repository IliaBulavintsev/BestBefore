package com.rv150.bestbefore.Activities;

import android.app.DialogFragment;
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

import com.rv150.bestbefore.Dialogs.ClearListDialog;
import com.rv150.bestbefore.CustomAdapter;
import com.rv150.bestbefore.Dialogs.OverdueItemDialog;
import com.rv150.bestbefore.Dialogs.OverdueYesNoDialog;
import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Preferences.SharedPrefsManager;
import com.rv150.bestbefore.StringWrapper;

import java.util.List;

/**
 * Created by Rudnev on 06.09.2016.
 */

// Активити "просроченные продукты"
public class Overdue extends AppCompatActivity {
    private CustomAdapter customAdapter;
    private List<StringWrapper> overdueList;
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


        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        Boolean needHelp = sPrefs.getBoolean(Resources.SHOW_HELP, true);
        if (needHelp) {
            showHelp();
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putBoolean(Resources.SHOW_HELP, false);
            editor.apply();
        }
    }

    private void showHelp() {
        String whatsNewText = getResources().getString(R.string.help_overdue);
        new AlertDialog.Builder(this).setTitle(R.string.help).setMessage(whatsNewText).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }


    @Override
    protected void onResume() {
        super.onResume();

        overdueList = SharedPrefsManager.getOverdueProducts(this); // обновляем overdueList в соотв. с сохраненными данными

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
        SharedPrefsManager.saveOverdueProducts(overdueList, this);
    }



    public void clearList() {
        overdueList.clear();
        SharedPrefsManager.saveOverdueProducts(overdueList, this);
        customAdapter.setData(overdueList);
        customAdapter.notifyDataSetChanged();
        isEmpty.setVisibility(View.VISIBLE);
    }
}

