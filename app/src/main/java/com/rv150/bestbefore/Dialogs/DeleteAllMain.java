package com.rv150.bestbefore.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.rv150.bestbefore.Activities.MainActivity;
import com.rv150.bestbefore.R;

/**
 * Created by ivan on 08.07.2016.
 */
public class DeleteAllMain extends DialogFragment {
        public DeleteAllMain()
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
                            ((MainActivity) getActivity()).deleteAll();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }

            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.sure_you_want_delete_all_products).
                    setPositiveButton(R.string.yes, dialogClickListener).
                    setNegativeButton(R.string.no, dialogClickListener);
            return builder.create();
        }
    }
