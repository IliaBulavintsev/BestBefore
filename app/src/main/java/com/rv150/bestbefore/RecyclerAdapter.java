package com.rv150.bestbefore;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Rudnev on 21.10.2016.
 */

public class RecyclerAdapter  extends
            RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        // Provide a direct reference to each of the views within a data item
        // Used to cache the views within the item layout for fast access
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public TextView nameTextView;
            public TextView quantityTextView;
            public TextView dateTextView;
            public TextView daysLeftTextView;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);

                nameTextView = (TextView) itemView.findViewById(R.id.item_name);
                quantityTextView = (TextView) itemView.findViewById(R.id.item_quantity);
                dateTextView = (TextView) itemView.findViewById(R.id.item_date);
                daysLeftTextView = (TextView) itemView.findViewById(R.id.item_days_left);
            }
        }

    private List<Contact> mContacts;
    // Store the context for easy access
    private Context mContext;

    // Pass in the contact array into the constructor
    public RecyclerAdapter(Context context, List<Contact> contacts) {
        mContacts = contacts;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.recycler_view_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Contact contact = mContacts.get(position);

        // Set item views based on your views and data model
        TextView name = viewHolder.nameTextView;
        name.setText("фруктовый сад персик и яблоко");

        TextView quantity = viewHolder.quantityTextView;
        quantity.setText("Кол-во: 1");
        TextView date = viewHolder.dateTextView;
        date.setText("13.11.2016");
        TextView daysLeft = viewHolder.daysLeftTextView;
        daysLeft.setText("17 дней");
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mContacts.size();
    }
}
