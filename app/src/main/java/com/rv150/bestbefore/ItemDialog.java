package com.rv150.bestbefore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Ivan on 29.06.2016.
 */
public class ItemDialog extends DialogFragment
{
    public ItemDialog()
    {
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {


        final String[] options ={"Редактировать", "Удалить"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int option) {
                        ((MainActivity) getActivity()).OptionChoosed(option);
                    }});
        return builder.create();
    }
}
