package com.serhiyboiko.taskmanager.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

public class MaterialNumberInputDialogFragment extends DialogFragment {

    final static String TITLE_ID = "title_id";
    final static String CONTENT = "content";
    final static String PREFILL = "prefill";

    private DialogListener mDialogListener;



    public static MaterialNumberInputDialogFragment newInstance(int titleId, int content, String prefill) {
        MaterialNumberInputDialogFragment frag = new MaterialNumberInputDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE_ID, titleId);
        args.putInt(CONTENT, content);
        args.putString(PREFILL, prefill);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof DialogListener)) {
            throw new ClassCastException(activity.toString() + " must implement DialogListener");
        }
        mDialogListener = (DialogListener) activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int titleId = getArguments().getInt(TITLE_ID);
        final int content = getArguments().getInt(CONTENT);
        final String prefill = getArguments().getString(PREFILL);
        Dialog dialog = new MaterialDialog.Builder(getActivity())
                .title(titleId)
                .content(content)
                .inputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL)
                .input(null, prefill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        mDialogListener.onInput(titleId, input);
                    }
                })
                .build();
        return dialog;
    }

    public interface DialogListener {
        void onInput(int titleId, CharSequence input);
    }
}
