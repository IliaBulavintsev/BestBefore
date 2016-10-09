package com.rv150.bestbefore.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.rv150.bestbefore.Activities.MainActivity;

/**
 * Created by Ivan on 29.06.2016.
 */
public class YesNoDialog extends DialogFragment
{
    public YesNoDialog()
    {
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        ((MainActivity) getActivity()).DeleteItem();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }

        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Удалить этот продукт?").setPositiveButton("Да", dialogClickListener).setNegativeButton("Нет", dialogClickListener);
        return builder.create();
    }
}
