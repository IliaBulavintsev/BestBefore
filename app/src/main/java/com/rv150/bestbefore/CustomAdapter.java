package com.rv150.bestbefore;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Created by rv150 on 06.01.2016.
 */
public class CustomAdapter extends BaseAdapter {
    private List<StringWrapper> data = new ArrayList<StringWrapper>();  //Наша коллекция
    private LayoutInflater inflater;
    private int width;
    private Context context;
    private boolean always_show_date;

    public CustomAdapter(Context mContext, int width) {
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //Инфлейтер чтобы получить View из XML
        this.width = width;
        this.context = mContext;
        this.always_show_date = false;
    }

    public CustomAdapter(Context mContext, int width, boolean always_show_date) {
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //Инфлейтер чтобы получить View из XML
        this.width = width;
        this.context = mContext;
        this.always_show_date = always_show_date;
    }

    public void setData(List<StringWrapper> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public StringWrapper getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.simplt_list_item, null);
        StringWrapper currentItem = getItem(i);
        Calendar date = currentItem.getDate();
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);
        date.set(year, month, day, 23, 59);

        TextView dateText = (TextView) view.findViewById(R.id.date);
        // Если нужно всегда показывать дату окончания срока (в Overdue)

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showDateExceptDaysLeft = sPrefs.getBoolean(Resources.showDateExceptDaysLeft, false);

        Calendar currentDate = new GregorianCalendar();
        long difference = date.getTimeInMillis() - currentDate.getTimeInMillis();
        int days = (int) (difference / (24 * 60 * 60 * 1000));


        if (always_show_date || showDateExceptDaysLeft) {
            if (month < 9) {
                dateText.setText(day + "." + "0" + (month + 1) + "." + year);
            }
            else {
                dateText.setText(day + "." + (month + 1) + "." + year);
            }
            setColor(dateText, difference, days);
        }

        else {
            if (difference < 0) {
                dateText.setText("Просрочен!");
            } else if (days == 0)
                dateText.setText(R.string.last_day);

            else {
                days++;
                dateText.setText(context.getResources().getQuantityString(R.plurals.numberOfDaysLeft, days, days));
            }


            setColor(dateText, difference, days);
        }

        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(currentItem.getTitle());

        name.setMaxWidth (40 * width / 100 );
        dateText.setMaxWidth( 50 * width / 100 );

        Typeface font = Typeface.createFromAsset(context.getAssets(), "san.ttf");
        name.setTypeface(font);
        dateText.setTypeface(font);

        return view;
    }

    private void setColor (TextView textView, long difference, int days) {
        if (difference < 0) {
            textView.setTextColor(0xffbdbdbd); // просрочен
        } else if (days == 0) {
            textView.setTextColor(0xffff0000); // последний день
        } else if (days > 0 && days <= 3) {
            textView.setTextColor(Color.rgb(220, 180, 0));   // 1-3 дня
        } else {
            textView.setTextColor(Color.rgb(21, 153, 74));  // свежее
        }
    }

   
}
