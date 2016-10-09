package com.rv150.bestbefore.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.rv150.bestbefore.Activities.Overdue;

/**
 * Created by Rudnev on 07.09.2016.
 */
public class OverdueItemDialog extends DialogFragment {
    public OverdueItemDialog()
    {
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        final String[] options ={"Удалить", "Удалить все"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {
                ((Overdue) getActivity()).OptionChoosed(option);
            }});
        return builder.create();
    }
}

