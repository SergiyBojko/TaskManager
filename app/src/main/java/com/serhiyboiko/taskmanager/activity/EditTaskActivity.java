package com.serhiyboiko.taskmanager.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.dialog.ImageSourcePickerDialog;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.file_io.FileIO;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsDeserializer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


public class EditTaskActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "EditTaskActivity";
    private int mTaskId;
    private TextInputEditText mTitleEditText;
    private TextInputEditText mCommentaryEditText;
    private TextInputEditText mMaxTaskDurationEditText;
    private ImageButton mTitleVoiceButton;
    private ImageButton mCommentaryVoiceButton;
    private AppCompatSpinner mTaskFrequencySpinner;
    private ImageView mTaskAvatar;
    private int buttonId;
    private int mItemPosition;
    private int mRequestCode;

    private static final int CREATE_NEW_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;
    private final static int VOICE_RECOGNITION_REQUEST = 3;
    private final static int PICK_AVATAR_FROM_GALLERY_REQUEST = 4;
    private static final int PICK_AVATAR_FOM_CAMERA_REQUEST = 5;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_editor_activity);
        bindActivity();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.task_frequency_variants));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTaskFrequencySpinner.setAdapter(spinnerAdapter);

        mTaskAvatar.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Intent intent = getIntent();
        mRequestCode = intent.getIntExtra(TaskListActivity.REQUEST_CODE_EXTRA, -1);


        switch (mRequestCode){
            case EDIT_TASK_REQUEST:
                getSupportActionBar().setTitle(R.string.edit_task_activity_title);

                mTaskId = intent.getIntExtra(TaskListActivity.TASK_ID_EXTRA, -1);
                mItemPosition = intent.getIntExtra(TaskListActivity.ITEM_POSITION_EXTRA, -1);
                mTitleEditText.setText(intent.getStringExtra(TaskListActivity.TITLE_EXTRA));
                mCommentaryEditText.setText(intent.getStringExtra(TaskListActivity.COMMENTARY_EXTRA));
                mMaxTaskDurationEditText.setText(String.valueOf(intent.getIntExtra(TaskListActivity.MAX_DURATION_EXTRA, 0)));
                mTaskFrequencySpinner.setSelection(intent.getIntExtra(TaskListActivity.TASK_FREQUENCY_EXTRA, 0));
                String avatarPath = intent.getStringExtra(TaskListActivity.AVATAR_PATH_EXTRA);

                if(avatarPath == null){
                    avatarPath = "";
                }

                if (!avatarPath.equals("")){
                    Bitmap avatar = BitmapFactory.decodeFile(avatarPath);
                    mTaskAvatar.setImageBitmap(avatar);
                }

                break;
            case CREATE_NEW_TASK_REQUEST:
                SharedPrefsDeserializer spd = new SharedPrefsDeserializer(this);
                int defaultMaxTaskDuration = spd.getMaxTaskDuration();
                mMaxTaskDurationEditText.setText(String.valueOf(defaultMaxTaskDuration));
                mTaskFrequencySpinner.setSelection(0);
                break;
        }

        // Disable button if no recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
            mTitleVoiceButton.setVisibility(View.GONE);
            mCommentaryVoiceButton.setVisibility(View.GONE);
        } else {
            mTitleVoiceButton.setOnClickListener(this);
            mCommentaryVoiceButton.setOnClickListener(this);
        }

        mTaskAvatar.setOnClickListener(this);
    }

    private void startVoiceRecognitionActivity(int promptId)    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(promptId));
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_editor_menu, menu);
        menu.findItem(R.id.menu_save_task).getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent taskData = new Intent();
                String imageName = "";
                String avatarPath;


                String title = mTitleEditText.getText().toString();
                if (title.isEmpty()) {
                    mTitleEditText.setError(getString(R.string.enter_title));
                    return;
                }
                String commentary = mCommentaryEditText.getText().toString();
                int maxDuration = Integer.parseInt(mMaxTaskDurationEditText.getText().toString());
                int taskFrequency = mTaskFrequencySpinner.getSelectedItemPosition();
                int taskPeriodInMills = calculateTaskPeriod(taskFrequency);
                if(taskPeriodInMills != 0 && maxDuration*1000 > taskPeriodInMills){
                    mMaxTaskDurationEditText.setError(getString(R.string.duration_must_be_lower_than_frequency));
                    return;
                }
                BitmapDrawable avatarDrawable = (BitmapDrawable)mTaskAvatar.getDrawable();

                if (avatarDrawable != null){
                    Bitmap avatarBitmap = avatarDrawable.getBitmap();

                    switch (mRequestCode){
                        case CREATE_NEW_TASK_REQUEST:
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(EditTaskActivity.this);
                            int nextTaskId = sp.getInt(Task.NEXT_TASK_ID, 0);
                            imageName = nextTaskId + ".png";
                            break;
                        case EDIT_TASK_REQUEST:
                            imageName = mTaskId + ".png";
                            break;
                    }
                    avatarPath = FileIO.saveImage(avatarBitmap, imageName, EditTaskActivity.this);
                } else {
                    avatarPath = "";
                }

                if (mRequestCode == EDIT_TASK_REQUEST){
                    taskData.putExtra(TaskListActivity.ITEM_POSITION_EXTRA, mItemPosition);
                }
                taskData.putExtra(TaskListActivity.TITLE_EXTRA, title);
                taskData.putExtra(TaskListActivity.COMMENTARY_EXTRA, commentary);
                taskData.putExtra(TaskListActivity.MAX_DURATION_EXTRA, maxDuration);
                taskData.putExtra(TaskListActivity.TASK_FREQUENCY_EXTRA, taskFrequency);
                taskData.putExtra(TaskListActivity.AVATAR_PATH_EXTRA, avatarPath);

                setResult(RESULT_OK, taskData);
                finish();
            }
        });
        menu.findItem(R.id.menu_cancel).getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, null);
                finish();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private int calculateTaskPeriod(int taskFrequency) {

        GregorianCalendar calendar;
        long taskStartTime = System.currentTimeMillis();
        long taskNextStartTime;
        int period;

        switch (taskFrequency){
            case Task.ONE_TIME:
                return 0;
            case Task.EVERY_HOUR:
                //return 1000*60*60;
                return 1000*10;
            case Task.EVERY_DAY:
                return 1000*60*60*24;
            case Task.EVERY_WEEK:
                return 1000*60*60*24*7;
            case Task.EVERY_MONTH:
                calendar = new GregorianCalendar()     ;
                calendar.setTimeInMillis(taskStartTime);
                calendar.add(Calendar.MONTH, 1);
                taskNextStartTime = calendar.getTimeInMillis();
                period = (int)(taskNextStartTime - taskStartTime);
                return period;
            case Task.EVERY_YEAR:
                calendar = new GregorianCalendar();
                calendar.setTimeInMillis(taskStartTime);
                calendar.add(Calendar.YEAR, 1);
                taskNextStartTime = calendar.getTimeInMillis();
                period = (int)(taskNextStartTime - taskStartTime);
                return period;
            default:
                return 0;
        }

    }


    private void bindActivity(){
        mTitleEditText = (TextInputEditText)findViewById(R.id.edit_text_title);
        mCommentaryEditText = (TextInputEditText)findViewById(R.id.edit_text_commentary);
        mMaxTaskDurationEditText = (TextInputEditText)findViewById(R.id.edit_text_max_task_duration);
        mTitleVoiceButton = (ImageButton) findViewById(R.id.edit_text_title_voice_recognition);
        mCommentaryVoiceButton = (ImageButton) findViewById(R.id.edit_text_commentary_voice_recognition);
        mTaskFrequencySpinner = (AppCompatSpinner) findViewById(R.id.spinner_task_exec_frequency);
        mTaskAvatar = (ImageView) findViewById(R.id.task_avatar);

    }

    @Override
    public void onClick(View v) {
        buttonId = v.getId();
        switch (buttonId){
            case R.id.edit_text_title_voice_recognition:
                startVoiceRecognitionActivity(R.string.enter_title_voice_rec_prompt);
                break;
            case R.id.edit_text_commentary_voice_recognition:
                startVoiceRecognitionActivity(R.string.enter_commentary_voice_rec_prompt);
                break;
            case R.id.task_avatar:
                ImageSourcePickerDialog.newInstance(R.string.image_source_picker_dialog_title).show(getSupportFragmentManager(), getString(R.string.image_source_picker_dialog_title));
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == VOICE_RECOGNITION_REQUEST && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            switch (buttonId){
                case R.id.edit_text_title_voice_recognition:
                    StringBuilder title = new StringBuilder(mTitleEditText.getText());
                    if (!title.toString().equals("")){
                        title.append(" ");
                    }
                    title.append(matches.get(0));
                    mTitleEditText.setText(title);
                    break;
                case R.id.edit_text_commentary_voice_recognition:
                    StringBuilder commentary = new StringBuilder(mTitleEditText.getText());
                    if (!commentary.toString().equals("")){
                        commentary.append(" ");
                    }
                    commentary.append(matches.get(0));
                    mCommentaryEditText.setText(commentary);
                    break;
            }
        }

        if (requestCode == PICK_AVATAR_FROM_GALLERY_REQUEST && resultCode == RESULT_OK){

                Uri imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                        .setActivityTitle(getString(R.string.image_crop_activity_title))
                        .setFixAspectRatio(true)
                        .start(this);
        }

        if (requestCode == PICK_AVATAR_FOM_CAMERA_REQUEST && resultCode == RESULT_OK){
            File tempPhotoPath = new File(getExternalFilesDir(Environment.DIRECTORY_DCIM), "temp_photo.jpg");
            Uri imageUri = Uri.fromFile(tempPhotoPath);

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.OFF)
                    .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                    .setActivityTitle(getString(R.string.image_crop_activity_title))
                    .setFixAspectRatio(true)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();
                Log.i(TAG, imageUri.toString());
                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap croppedImage = BitmapFactory.decodeStream(imageStream);
                int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());

                //resizing image here because resizing in CropImage works inconsistently
                Bitmap resizedImage = Bitmap.createScaledBitmap(croppedImage, imageSize, imageSize, false);

                mTaskAvatar.setImageBitmap(resizedImage);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
