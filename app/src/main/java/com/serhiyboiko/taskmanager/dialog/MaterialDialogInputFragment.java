package com.serhiyboiko.taskmanager.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.serhiyboiko.taskmanager.R;

/**
 * Created by Amegar on 22.06.2016.
 */
public class MaterialDialogInputFragment extends DialogFragment {

    final static String TITLE_ID = "title_id";
    final static String CONTENT = "content";
    final static String PREFILL = "prefill";

    public static MaterialDialogInputFragment newInstance(int titleId, int content, String prefill) {
        MaterialDialogInputFragment frag = new MaterialDialogInputFragment();
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
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int titleId = getArguments().getInt(TITLE_ID);
        final int content = getArguments().getInt(CONTENT);
        final String prefill = getArguments().getString(PREFILL);
        Dialog dialog = new com.afollestad.materialdialogs.MaterialDialog.Builder(getActivity())
                .title(titleId)
                .content(content)
                .inputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL)
                .input(null, prefill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        ((DialogListener)getActivity()).onInput(titleId, input);
                    }
                })
                .build();
        return dialog;
    }

    public interface DialogListener {
        void onInput(int titleId, CharSequence input);
    }
}
