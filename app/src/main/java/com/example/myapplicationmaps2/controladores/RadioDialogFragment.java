package com.example.myapplicationmaps2.controladores;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

import com.example.myapplicationmaps2.R;


public class RadioDialogFragment extends DialogFragment {

    NumberPicker numberPicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view =inflater.inflate(R.layout.dialog_radio_layout, null);
        numberPicker=view.findViewById(R.id.numberpicker_main_picker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(1);
        builder.setView(view)
                .setMessage("Seleccione el radio del obstáculo marcado")
               .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                       Log.d("DIALOG","Presionó fire");
                       int radio = numberPicker.getValue();
                       // Send the positive button event back to the host activity
                       listener.onDialogPositiveClick(RadioDialogFragment.this,radio);
                   }
               })
               .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                       Log.d("DIALOG","Presionó cancel");
                       listener.onDialogNegativeClick(RadioDialogFragment.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
            * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, int radio);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("RadioDialogFragment must implement NoticeDialogListener");
        }
    }
}