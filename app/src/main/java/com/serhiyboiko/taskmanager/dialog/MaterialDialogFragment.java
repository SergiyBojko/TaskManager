package com.serhiyboiko.taskmanager.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.serhiyboiko.taskmanager.R;

public class MaterialDialogFragment extends DialogFragment {

    final static String TITLE_ID = "title_id";

    public static MaterialDialogFragment newInstance(int titleId) {
        MaterialDialogFragment frag = new MaterialDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE_ID, titleId);
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
        final int titleId = getArguments().getInt(TITLE_ID);
        Dialog dialog = new MaterialDialog.Builder(getActivity())
                .title(getContext().getString(titleId))
                .positiveText(R.string.ok_label)
                .negativeText(R.string.md_cancel_label)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ((DialogListener)getActivity()).onPositive(titleId);
                    }
                }).build();
        return dialog;
    }

    public interface DialogListener {
        void onPositive(int titleId);
    }
}
