package com.rv150.bestbefore.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.widget.DataBufferAdapter;

/**
 * Created by ivan on 22.01.17.
 */

public class DriveResultsAdapter extends DataBufferAdapter<Metadata> {

    public DriveResultsAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(),
                    android.R.layout.simple_list_item_1, null);
        }
        Metadata metadata = getItem(position);
        TextView titleTextView =
                (TextView) convertView.findViewById(android.R.id.text1);
        titleTextView.setText(metadata.getTitle());
        return convertView;
    }
}
