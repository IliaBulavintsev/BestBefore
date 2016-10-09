package com.rv150.bestbefore.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.rv150.bestbefore.Activities.Overdue;

/**
 * Created by Rudnev on 06.09.2016.
 */
// Используется в просроченных продуктах
public class ClearListDialog  extends DialogFragment {
        public ClearListDialog()
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
                            ((Overdue) getActivity()).clearList();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }

            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Вы действительно хотите очистить список?").setPositiveButton("Да", dialogClickListener).setNegativeButton("Нет", dialogClickListener);
            return builder.create();
        }
    }

