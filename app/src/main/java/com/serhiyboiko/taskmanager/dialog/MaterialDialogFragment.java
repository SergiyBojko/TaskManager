package com.serhiyboiko.taskmanager.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.DialogAction;
import com.serhiyboiko.taskmanager.R;

public class MaterialDialogFragment extends DialogFragment {

    public static MaterialDialogFragment newInstance(String title) {
        MaterialDialogFragment frag = new MaterialDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof DialogListener)) {
            throw new ClassCastException(activity.toString() + " must implement DialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        Dialog dialog = new com.afollestad.materialdialogs.MaterialDialog.Builder(getActivity())
                .title(title)
                .positiveText(R.string.ok_label)
                .negativeText(R.string.md_cancel_label)
                .onPositive(new com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull com.afollestad.materialdialogs.MaterialDialog dialog, @NonNull DialogAction which) {
                        ((DialogListener)getActivity()).onPositive();
                    }
                }).build();
        return dialog;
    }

    public interface DialogListener {
        void onPositive();
    }
}
