package com.rv150.bestbefore;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rv150.bestbefore.Models.Product;

/**
 * Created by Rudnev on 25.10.2016.
 */

public class RecyclerAdapter extends  RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<Product> items;
    public RecyclerAdapter(List<Product> items) {
        this.items = items;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView quantityTextView;
        TextView dateTextView;
        TextView daysLeftTextView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.item_name);
            quantityTextView = (TextView) itemView.findViewById(R.id.item_quantity);
            dateTextView = (TextView) itemView.findViewById(R.id.item_date);
            daysLeftTextView = (TextView) itemView.findViewById(R.id.item_days_left);
        }
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.recycler_view_item, parent, false);
        return new ViewHolder(contactView);
    }



    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder viewHolder, int position) {
        final Product item = items.get(position);
            viewHolder.itemView.setBackgroundColor(Color.WHITE);
            viewHolder.nameTextView.setVisibility(View.VISIBLE);
            viewHolder.quantityTextView.setVisibility(View.VISIBLE);
            viewHolder.dateTextView.setVisibility(View.VISIBLE);
            viewHolder.daysLeftTextView.setVisibility(View.VISIBLE);

            TextView name = viewHolder.nameTextView;
            name.setText(item.getTitle());

            int quantity = item.getQuantity();
            String quantityStr = "Кол-во: " + quantity;
            if (quantity < 10) {
                quantityStr += "  ";
            }
            viewHolder.quantityTextView.setText(quantityStr);

            Calendar calendar = item.getDate();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.set(year, month, day, 23, 59);

            TextView date = viewHolder.dateTextView;
            if (month < 9) {
                if (day < 10) {
                    date.setText("Годен до: 0" + day + "." + "0" + (month + 1) + "." + year);
                }
                else {
                    date.setText("Годен до: " + day + "." + "0" + (month + 1) + "." + year);
                }
            }
            else {
                if (day < 10) {
                    date.setText("Годен до: 0" + day + "." + (month + 1) + "." + year);
                }
                else {
                    date.setText("Годен до: " + day + "." + (month + 1) + "." + year);
                }
            }

            TextView daysLeft = viewHolder.daysLeftTextView;
            String daysLeftStr = getDaysLeft(calendar);
            daysLeft.setText(daysLeftStr);
            if (daysLeftStr.length() == 3) {
                daysLeft.setTextSize(21);
            }
            else if (daysLeftStr.length() > 3) {
                daysLeft.setTextSize(18);
            }
            else if (daysLeftStr.equals("!")) {
                daysLeft.setTextSize(26);
            }
            else
            {
                daysLeft.setTextSize(24);
            }
            setColor(daysLeft, calendar);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    private void setColor (TextView textView, Calendar date) {
        Calendar currentDate = new GregorianCalendar();
        long difference = date.getTimeInMillis() - currentDate.getTimeInMillis();
        int days = (int) (difference / (24 * 60 * 60 * 1000));
        if (difference < 0) {
            textView.setTextColor(0xffbdbdbd); // просрочен
        } else if (days == 0) {
            textView.setTextColor(0xffff0000); // последний день
        } else if (days > 0 && days < 3) {
            textView.setTextColor(Color.rgb(220, 180, 0));   // 1-2 дня
        } else {
            textView.setTextColor(Color.rgb(21, 153, 74));  // свежее
        }
    }


    private String getDaysLeft (Calendar sourceDate) {
        Calendar currentDate = new GregorianCalendar();
        long difference = sourceDate.getTimeInMillis() - currentDate.getTimeInMillis();
        int days = (int) (difference / (24 * 60 * 60 * 1000));
        if (days < 0) {
            days--;
        }
        if (days == 0) {
            if (difference < 0) {
                return "-1";        // В первый день просрочки
            }
            return "!";
        }
        return Integer.toString(days);
    }
}
