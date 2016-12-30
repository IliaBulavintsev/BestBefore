package com.rv150.bestbefore;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rv150.bestbefore.Models.Product;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Rudnev on 25.10.2016.
 */

public class RecyclerAdapter extends  RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<Product> items;
    private Context mContext;
    public RecyclerAdapter(List<Product> items, Context context) {
        this.items = items;
        mContext = context;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView quantityTextView;
        TextView bestBeforeTV;
        TextView dateCreatedTV;
        TextView daysLeftTextView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.item_name);
            quantityTextView = (TextView) itemView.findViewById(R.id.item_quantity);
            bestBeforeTV = (TextView) itemView.findViewById(R.id.item_best_before);
            dateCreatedTV = (TextView) itemView.findViewById(R.id.item_date_created);
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

        TextView name = viewHolder.nameTextView;
        name.setText(item.getTitle());

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean quantityEnabled = sPrefs.getBoolean("use_quantity", true);
        if (quantityEnabled) {
            viewHolder.quantityTextView.setVisibility(View.VISIBLE);
            int quantity = item.getQuantity();
            String quantityStr = "Кол-во:  " + quantity;
            if (quantity < 10) {
                quantityStr += "  ";
            }
            viewHolder.quantityTextView.setText(quantityStr);
        }
        else {
            viewHolder.quantityTextView.setVisibility(View.GONE);
        }



        TextView dateCreatedTV = viewHolder.dateCreatedTV;
        boolean useDateProduced = sPrefs.getBoolean("use_date_produced", true);
        if (useDateProduced) {
            dateCreatedTV.setVisibility(View.VISIBLE);
            Calendar produced = item.getProduced();
            int year = produced.get(Calendar.YEAR);
            int month = produced.get(Calendar.MONTH);
            int day = produced.get(Calendar.DAY_OF_MONTH);
            String dateCreated;
            if (month < 9) {
                if (day < 10) {
                    dateCreated = day + "." + "0" + (month + 1) + "." + year + "  -  ";
                } else {
                    dateCreated = day + "." + "0" + (month + 1) + "." + year + "  -  ";
                }
            } else {
                if (day < 10) {
                    dateCreated = day + "." + (month + 1) + "." + year + "  -  ";
                } else {
                    dateCreated = day + "." + (month + 1) + "." + year + "  -  ";
                }
            }
            dateCreatedTV.setText(dateCreated);
        }
        else {
            dateCreatedTV.setVisibility(View.GONE);
        }


        final Calendar date = item.getDate();
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);
        String bestBeforeText = "";
        if (!useDateProduced) {
            bestBeforeText += "Годен до:  ";
        }

        if (month < 9) {
            if (day < 10) {
                bestBeforeText += day + "." + "0" + (month + 1) + "." + year;
            }
            else {
                bestBeforeText += day + "." + "0" + (month + 1) + "." + year;
            }
        }
        else {
            if (day < 10) {
                bestBeforeText += day + "." + (month + 1) + "." + year;
            }
            else {
                bestBeforeText += day + "." + (month + 1) + "." + year;
            }
        }

        TextView bestBeforeTV = viewHolder.bestBeforeTV;
        bestBeforeTV.setText(bestBeforeText);




        TextView daysLeft = viewHolder.daysLeftTextView;
        String daysLeftStr = getDaysLeft(date);
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
        setColor(daysLeft, date);
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
        Calendar currentDate = Calendar.getInstance();
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
