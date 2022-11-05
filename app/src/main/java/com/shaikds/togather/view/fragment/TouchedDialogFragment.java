package com.shaikds.togather.view.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TouchedDialogFragment extends DialogFragment {
    public TouchedDialogFragment newInstance() {
        TouchedDialogFragment touchedDialogFragment = new TouchedDialogFragment();
        return touchedDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("יציאה מהדף");
        builder.setMessage("אם תצאו תאבדו את כל השינוים שביצעתם. האם אתם בטוחים?");
        builder.setNegativeButton("לא", (dialog, which) -> {
            this.dismiss();
        });
        builder.setPositiveButton("כן", (dialog, which) -> {
            getActivity().onBackPressed();
        });

        return builder.create();
    }
}
