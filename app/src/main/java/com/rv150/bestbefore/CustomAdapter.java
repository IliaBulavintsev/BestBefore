package com.rv150.bestbefore;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

        TextView date_text = (TextView) view.findViewById(R.id.date);
        // Если нужно всегда показывать дату окончания срока (в Overdue)
        if (always_show_date) {
            if (month < 9) {
                date_text.setText(day + "." + "0" + (month + 1) + "." + year);
            }
            else {
                date_text.setText(day + "." + (month + 1) + "." + year);
            }
            date_text.setTextColor(0xffbdbdbd);
        }

        else {
            Calendar currentDate = new GregorianCalendar();
            long difference = date.getTimeInMillis() - currentDate.getTimeInMillis();
            Integer days = (int) (difference / (24 * 60 * 60 * 1000));

            if (difference < 0) {
                date_text.setText("Просрочен!");
            } else if (days == 0)
                date_text.setText(R.string.last_day);

            else {
                days++;
//                String text = days.toString();
//                if ((days >= 10) && (days <= 20))
//                    text += " дней";
//                else if ((days % 10 >= 2) && (days % 10 <= 4))
//                    text += " дня";
//                else if (days % 10 == 1)
//                    text += " день";
//                else
//                    text += " дней";

               // date_text.setText(text);

                date_text.setText(context.getResources().getQuantityString(R.plurals.numberOfDaysLeft, days, days));
            }

            if (difference < 0) {
                date_text.setTextColor(0xffbdbdbd); // просрочен
            } else if (days == 0) {
                date_text.setTextColor(0xffff0000); // последний день
            } else if (days > 0 && days <= 3) {
                date_text.setTextColor(Color.rgb(220, 180, 0));   // 1-3 дня
            } else {
                date_text.setTextColor(Color.rgb(21, 153, 74));
            }
        }

        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(currentItem.getTitle());

        name.setMaxWidth (40 * width / 100 );
        date_text.setMaxWidth( 50 * width / 100 );

        Typeface font = Typeface.createFromAsset(context.getAssets(), "san.ttf");
        name.setTypeface(font);
        date_text.setTypeface(font);

        return view;
    }
}
