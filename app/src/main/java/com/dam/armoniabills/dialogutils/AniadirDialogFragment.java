package com.dam.armoniabills.dialogutils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.dam.armoniabills.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;

public class AniadirDialogFragment extends DialogFragment {

    EditText etPersona;

    OnDatosDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_aniadir, null);

        etPersona = dialogView.findViewById(R.id.etPersonaDialog);

        materialAlertDialogBuilder
                .setTitle("Usuarios")
                .setView(dialogView)
                .setPositiveButton("Añadir personas", null)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog ad = materialAlertDialogBuilder.create();

        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btn = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = etPersona.getText().toString();

                        if (email.isEmpty()) {
                            Toast.makeText(getContext(), R.string.email_incorrecto, Toast.LENGTH_LONG).show();
                        } else {
                            listener.onAceptarDatos(email);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        return ad;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDatosDialogListener) {
            listener = (OnDatosDialogListener) context;
        } else {
            Log.e("ERROR DialogFragment", "El Activity asociado a dicho fragmento debe implementar OnDatosDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (listener != null) {
            listener = null;
        }
    }

}
