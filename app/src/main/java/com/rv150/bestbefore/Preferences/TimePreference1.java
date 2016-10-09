package com.rv150.bestbefore.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Resources;


public class TimePreference1 extends DialogPreference {

    /** The widget for picking a time */
    private TimePicker timePicker;

    /** Default hour */
    private static final int DEFAULT_HOUR = 17;

    /** Default minute */
    private static final int DEFAULT_MINUTE = 0;

    /**
     * Creates a preference for choosing a time based on its XML declaration.
     *
     * @param context
     * @param attributes
     */
    public TimePreference1(Context context,
                           AttributeSet attributes) {
        super(context, attributes);
        setPersistent(false);
    }

    /**
     * Initialize time picker to currently stored time preferences.
     *
     * @param view
     * The dialog preference's host view
     */
    @Override
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        timePicker = (TimePicker) view.findViewById(R.id.prefTimePicker);
        timePicker.setCurrentHour(getSharedPreferences().getInt(Resources.PREF_FIRST_HOUR, DEFAULT_HOUR));
        timePicker.setCurrentMinute(getSharedPreferences().getInt(Resources.PREF_FIRST_MINUTE, DEFAULT_MINUTE));
        timePicker.setIs24HourView(DateFormat.is24HourFormat(timePicker.getContext()));
    }

    /**
     * Handles closing of dialog. If user intended to save the settings, selected
     * hour and minute are stored in the preferences with keys KEY.hour and
     * KEY.minute, where KEY is the preference's KEY.
     *
     * @param okToSave
     * True if user wanted to save settings, false otherwise
     */
    @Override
    protected void onDialogClosed(boolean okToSave) {
        super.onDialogClosed(okToSave);
        if (okToSave) {
            timePicker.clearFocus();
            int hour = timePicker.getCurrentHour();
            int minute = timePicker.getCurrentMinute();
            SharedPreferences.Editor editor = getEditor();
            editor.putInt(Resources.PREF_FIRST_HOUR, hour);
            editor.putInt(Resources.PREF_FIRST_MINUTE, minute);
            editor.apply();
            if (callChangeListener(get_summary())) {
                persistString(get_summary());
            }
        }
    }


    private String get_summary() {
        if (timePicker == null) {
            return null;
        }
        String result = timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute();
        return timePicker.getCurrentMinute() == 0 ? result + '0' :  result;
    }
}