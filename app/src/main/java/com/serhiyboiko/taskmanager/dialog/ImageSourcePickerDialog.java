package com.serhiyboiko.taskmanager.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.serhiyboiko.taskmanager.R;

import java.io.File;

/**
 * Created on 03.07.2016.
 */
public class ImageSourcePickerDialog extends DialogFragment {
    final static String TITLE_ID = "title_id";

    private final static int PICK_AVATAR_FROM_GALLERY_REQUEST = 4;
    private static final int PICK_AVATAR_FOM_CAMERA_REQUEST = 5;

    public static ImageSourcePickerDialog newInstance(int titleId) {
        ImageSourcePickerDialog frag = new ImageSourcePickerDialog();
        Bundle args = new Bundle();
        args.putInt(TITLE_ID, titleId);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int titleId = getArguments().getInt(TITLE_ID);
        Dialog dialog = new MaterialDialog.Builder(getActivity())
                .title(titleId)
                .items(getResources().getStringArray(R.array.image_source_picker_dialog_choices))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        Intent intent;
                        switch (which){
                            case 0:
                                intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                getActivity().startActivityForResult(intent, PICK_AVATAR_FROM_GALLERY_REQUEST);
                                break;
                            case 1:
                                File tempPhotoPath = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DCIM), "temp_photo.jpg");
                                Uri tempPhotoUri = Uri.fromFile(tempPhotoPath);

                                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri);
                                getActivity().startActivityForResult(intent, PICK_AVATAR_FOM_CAMERA_REQUEST);
                                break;
                        }

                    }
                })
                .negativeText(R.string.md_cancel_label)
                .build();
        return dialog;
    }
}
