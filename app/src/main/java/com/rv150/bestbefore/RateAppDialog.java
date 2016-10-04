package com.rv150.bestbefore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Rudnev on 07.09.2016.
 */
public class RateAppDialog extends DialogFragment {
    public RateAppDialog()
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
                        ((MainActivity) getActivity()).rateApp();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        ((MainActivity) getActivity()).finishAct();
                        break;
                }
            }

        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.rate_app_msg).setPositiveButton(R.string.rate, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener);
        return builder.create();
    }
}

