package com.rv150.bestbefore.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.rv150.bestbefore.Activities.MainActivity;

/**
 * Created by ivan on 08.07.2016.
 */
public class DeleteAllDialog extends DialogFragment {
        public DeleteAllDialog()
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
                            ((MainActivity) getActivity()).DeleteAll();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }

            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Вы действительно хотите удалить все продукты?").setPositiveButton("Да", dialogClickListener).setNegativeButton("Нет", dialogClickListener);
            return builder.create();
        }
    }
