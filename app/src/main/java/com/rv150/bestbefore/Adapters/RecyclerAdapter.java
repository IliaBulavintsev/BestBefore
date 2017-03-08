package com.rv150.bestbefore.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Rudnev on 25.10.2016.
 */

public class RecyclerAdapter extends  RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements Filterable {

    private List<Product> filteredData, originData;
    private Context mContext;

    private ZoomAnimation mZoomAnimation;

    public RecyclerAdapter(List<Product> items, Context context, ZoomAnimation zoomAnimation) {
        this.filteredData = items;
        this.originData = items;
        mContext = context;
        mZoomAnimation = zoomAnimation;
    }

    public interface ZoomAnimation {
        void zoom(final View thumbView, long photo);
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData = (List) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint.toString().toLowerCase();
                FilterResults result = new FilterResults();

                if (constraint.toString().length() > 0) {
                    List<Product> founded = new ArrayList<>();
                    for (Product item : originData) {
                        if (item.getTitle().toLowerCase().contains(constraint)) {
                            founded.add(item);
                        }
                    }
                    result.values = founded;
                    result.count = founded.size();
                } else {
                    result.values = originData;
                    result.count = originData.size();
                }
                return result;
            }
        };
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView quantityTextView;
        TextView itemDate;
        TextView daysLeftTextView;
        ImageView photo;
        LinearLayout lowerLine;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.item_name);
            quantityTextView = (TextView) itemView.findViewById(R.id.item_quantity);
            itemDate = (TextView) itemView.findViewById(R.id.item_date);
            daysLeftTextView = (TextView) itemView.findViewById(R.id.item_days_left);
            photo = (ImageView) itemView.findViewById(R.id.item_image);
            lowerLine = (LinearLayout) itemView.findViewById(R.id.lower_line_product);
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
        final Product item = filteredData.get(position);

        String name = item.getTitle();
        TextView nameTV = viewHolder.nameTextView;

        if (name != null) {
            nameTV.setVisibility(View.VISIBLE);
            nameTV.setText(name);
        }
        else {
            nameTV.setVisibility(View.GONE);
        }

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);


        String dateFirstPart;
        boolean useDateProduced = sPrefs.getBoolean("use_date_produced", true);
        boolean defaultDate = item.getProduced().getTimeInMillis() == 0;
        if (useDateProduced && !defaultDate) {
            Calendar produced = item.getProduced();
            int year = produced.get(Calendar.YEAR);
            int month = produced.get(Calendar.MONTH);
            int day = produced.get(Calendar.DAY_OF_MONTH);
            if (month < 9) {
                if (day < 10) {
                    dateFirstPart = day + "." + "0" + (month + 1) + "." + year + " - ";
                } else {
                    dateFirstPart = day + "." + "0" + (month + 1) + "." + year + " - ";
                }
            } else {
                if (day < 10) {
                    dateFirstPart = day + "." + (month + 1) + "." + year + " - ";
                } else {
                    dateFirstPart = day + "." + (month + 1) + "." + year + " - ";
                }
            }
        }
        else {
            dateFirstPart = "Годен до: ";
        }


        final Calendar date = item.getDate();
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);
        String bestBeforeText = dateFirstPart;


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

        TextView bestBeforeTV = viewHolder.itemDate;
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



        boolean quantityEnabled = sPrefs.getBoolean("use_quantity", true);
        if (quantityEnabled) {
            viewHolder.quantityTextView.setVisibility(View.VISIBLE);
            int quantity = item.getQuantity();
            String quantityStr = quantity + " " +
                    com.rv150.bestbefore.Resources.Measures.values()[item.getMeasure()].getText();
            viewHolder.quantityTextView.setText(quantityStr);
        }
        else {
            viewHolder.quantityTextView.setVisibility(View.GONE);
        }


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final ImageView imageView = viewHolder.photo;
        boolean photoEnabled = sPrefs.getBoolean("use_photo", true);
        if (photoEnabled) {
            imageView.setVisibility(View.VISIBLE);
            final long photo = item.getPhoto();
            if (photo != 0) {
                final String fileName = mContext.getFilesDir() + "/" + photo + ".jpeg";
                File file = new File(fileName);
                if (file.exists()) {
                    Picasso.with(mContext).load(file).resize(120, 160).into(imageView);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mZoomAnimation.zoom(imageView, photo);
                        }
                    });
                }
                else {
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_image));
                        }
                    });
                    imageView.setOnClickListener(null);
                }
            }
            else {
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_image));
                    }
                });
                imageView.setOnClickListener(null);
            }

            layoutParams.setMargins(0, dpToPx(15), 0, 0);
        }
        else {
            imageView.setVisibility(View.GONE);
            layoutParams.setMargins(0, dpToPx(3), 0, dpToPx(5));
        }

        LinearLayout lowerLine = viewHolder.lowerLine;
        lowerLine.setLayoutParams(layoutParams);
    }


    private int dpToPx(int dp) {
        Resources r = mContext.getResources();
        return  (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }


    public Product getItem(int pos) {
        return filteredData.get(pos);
    }



    @Override
    public int getItemCount() {
        return filteredData.size();
    }


    private void setColor (TextView textView, Calendar date) {
        Calendar currentDate = Calendar.getInstance();
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
