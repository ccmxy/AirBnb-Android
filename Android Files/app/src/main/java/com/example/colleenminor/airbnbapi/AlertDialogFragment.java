package com.example.colleenminor.airbnbapi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by colleenminor on 7/28/16.
 */
public class AlertDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Title: This is an errror!!!!!!")
                .setMessage("Message: Panic!!!!")
                .setPositiveButton("Ahhhhhh!", null);

        AlertDialog dialog = builder.create();
        return dialog;
    }

}
