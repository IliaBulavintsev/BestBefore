package com.rv150.bestbefore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Rudnev on 07.09.2016.
 */
public class OverdueYesNoDialog extends DialogFragment
    {
        public OverdueYesNoDialog()
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
                            ((Overdue) getActivity()).deleteItem();
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
