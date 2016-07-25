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

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.dialog.ImageSourcePickerDialog;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.file_io.FileIO;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsDeserializer;
import com.serhiyboiko.taskmanager.utils.tutorial.Tutorial;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


public class EditTaskActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private static final String TAG = "EditTaskActivity";
    public static final String MAP_FRAGMENT_TAG = "map_fragment";
    private static final String AVATAR_CHANGED_KEY = "avatar_changed";
    private static final String AVATAR_KEY = "avatar";
    private int mTaskId;
    private TextInputEditText mTitleEditText;
    private TextInputEditText mCommentaryEditText;
    private TextInputEditText mMaxTaskDurationEditText;
    private ImageButton mTitleVoiceButton;
    private ImageButton mCommentaryVoiceButton;
    private ImageButton mEditLocationButton;
    private ImageButton mDeleteLocationButton;
    private AppCompatSpinner mTaskFrequencySpinner;
    private ImageView mTaskAvatar;
    private int buttonId;
    private int mItemPosition;
    private int mRequestCode;
    private String mAvatarPath;
    private long mAvatarEditTime;
    private boolean mAvatarChanged;
    private double mLatitude;
    private double mLongitude;
    private boolean mIsAssignedToLocation;
    private GoogleMap mMap;
    private ImageView mMapPlaceholder;


    private static final int CREATE_NEW_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;
    private final static int VOICE_RECOGNITION_REQUEST = 3;
    private final static int PICK_AVATAR_FROM_GALLERY_REQUEST = 4;
    private static final int PICK_AVATAR_FOM_CAMERA_REQUEST = 5;
    private static final int SELECT_TASK_LOCATION_REQUEST = 6;


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
                mAvatarPath = intent.getStringExtra(TaskListActivity.AVATAR_PATH_EXTRA);
                mAvatarEditTime = intent.getLongExtra(TaskListActivity.AVATAR_EDIT_TIME_EXTRA, 0);
                mIsAssignedToLocation = intent.getBooleanExtra(TaskListActivity.IS_ASSIGNED_TO_LOCATION_EXTRA, false);

                if (mAvatarPath != null && !mAvatarPath.isEmpty()){
                    mTaskAvatar.setBackgroundResource(R.mipmap.avatar_placeholder);
                    Glide.with(this)
                            .load(mAvatarPath)
                            .signature(new StringSignature(Long.toString(mAvatarEditTime)))
                            .placeholder(R.mipmap.avatar_placeholder)
                            .crossFade()
                            .into(mTaskAvatar);
                }

                break;
            case CREATE_NEW_TASK_REQUEST:
                SharedPrefsDeserializer spd = new SharedPrefsDeserializer(this);
                int defaultMaxTaskDuration = spd.getMaxTaskDuration();
                mMaxTaskDurationEditText.setText(String.valueOf(defaultMaxTaskDuration));
                mTaskFrequencySpinner.setSelection(0);
                mEditLocationButton.setVisibility(View.GONE);
                mDeleteLocationButton.setVisibility(View.GONE);
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

        if (savedInstanceState == null){
            if (mIsAssignedToLocation){
                mLatitude = intent.getDoubleExtra(TaskListActivity.LATITUDE_EXTRA, 0);
                mLongitude = intent.getDoubleExtra(TaskListActivity.LONGITUDE_EXTRA, 0);
                mMapPlaceholder.setVisibility(View.GONE);
                loadMap();
            } else {
                mEditLocationButton.setVisibility(View.GONE);
                mDeleteLocationButton.setVisibility(View.GONE);
            }
        } else {
            mIsAssignedToLocation = savedInstanceState.getBoolean(TaskListActivity.IS_ASSIGNED_TO_LOCATION_EXTRA, false);
            if (mIsAssignedToLocation){
                mLatitude = savedInstanceState.getDouble(TaskListActivity.LATITUDE_EXTRA, 0);
                mLongitude = savedInstanceState.getDouble(TaskListActivity.LONGITUDE_EXTRA, 0);
                mMapPlaceholder.setVisibility(View.GONE);
                loadMap();
            }

            mAvatarChanged = savedInstanceState.getBoolean(AVATAR_CHANGED_KEY, false);
            if (mAvatarChanged){
                Bitmap avatar = savedInstanceState.getParcelable(AVATAR_KEY);
                mTaskAvatar.setImageBitmap(avatar);
                mAvatarEditTime = savedInstanceState.getLong(TaskListActivity.AVATAR_EDIT_TIME_EXTRA, 0);
            }
        }

        mTaskAvatar.setOnClickListener(this);
        mMapPlaceholder.setOnClickListener(this);
        mEditLocationButton.setOnClickListener(this);
        mDeleteLocationButton.setOnClickListener(this);

        Tutorial.taskEditActivityTutorial(this);
    }

    private void loadMap() {
        SupportMapFragment map = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.map_fragment_container, map, MAP_FRAGMENT_TAG).commitAllowingStateLoss();
        map.getMapAsync(this);
        mEditLocationButton.setVisibility(View.VISIBLE);
        mDeleteLocationButton.setVisibility(View.VISIBLE);
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

                if (mAvatarChanged){
                    BitmapDrawable avatarDrawable = (BitmapDrawable)mTaskAvatar.getDrawable();
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
                    mAvatarPath = FileIO.saveImage(avatarBitmap, imageName, EditTaskActivity.this);
                }

                if (mRequestCode == EDIT_TASK_REQUEST){
                    taskData.putExtra(TaskListActivity.ITEM_POSITION_EXTRA, mItemPosition);
                }
                taskData.putExtra(TaskListActivity.TITLE_EXTRA, title);
                taskData.putExtra(TaskListActivity.COMMENTARY_EXTRA, commentary);
                taskData.putExtra(TaskListActivity.MAX_DURATION_EXTRA, maxDuration);
                taskData.putExtra(TaskListActivity.TASK_FREQUENCY_EXTRA, taskFrequency);
                taskData.putExtra(TaskListActivity.AVATAR_PATH_EXTRA, mAvatarPath);
                taskData.putExtra(TaskListActivity.AVATAR_EDIT_TIME_EXTRA, mAvatarEditTime);
                taskData.putExtra(TaskListActivity.LATITUDE_EXTRA, mLatitude);
                taskData.putExtra(TaskListActivity.LONGITUDE_EXTRA, mLongitude);
                taskData.putExtra(TaskListActivity.IS_ASSIGNED_TO_LOCATION_EXTRA, mIsAssignedToLocation);

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
        mEditLocationButton = (ImageButton) findViewById(R.id.edit_location);
        mDeleteLocationButton = (ImageButton) findViewById(R.id.delete_location);
        mTaskFrequencySpinner = (AppCompatSpinner) findViewById(R.id.spinner_task_exec_frequency);
        mTaskAvatar = (ImageView) findViewById(R.id.task_avatar);
        mMapPlaceholder = (ImageView) findViewById(R.id.map_placeholder);
    }

    @Override
    public void onClick(View v) {
        buttonId = v.getId();
        Intent intent;
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
            case R.id.map_placeholder:
                intent = new Intent(EditTaskActivity.this, TaskLocationSelectActivity.class);
                intent.putExtra(TaskListActivity.IS_ASSIGNED_TO_LOCATION_EXTRA, false);
                startActivityForResult(intent, SELECT_TASK_LOCATION_REQUEST);
                break;
            case R.id.edit_location:
                intent = new Intent(EditTaskActivity.this, TaskLocationSelectActivity.class);
                intent.putExtra(TaskListActivity.IS_ASSIGNED_TO_LOCATION_EXTRA, mIsAssignedToLocation);
                intent.putExtra(TaskListActivity.LATITUDE_EXTRA, mLatitude);
                intent.putExtra(TaskListActivity.LONGITUDE_EXTRA, mLongitude);
                startActivityForResult(intent, SELECT_TASK_LOCATION_REQUEST);
                break;
            case R.id.delete_location:
                mIsAssignedToLocation = false;
                mMap = null;
                mLatitude = 0;
                mLongitude = 0;
                SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
                getSupportFragmentManager().beginTransaction().remove(map).commit();
                mEditLocationButton.setVisibility(View.GONE);
                mDeleteLocationButton.setVisibility(View.GONE);
                mMapPlaceholder.setVisibility(View.VISIBLE);
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
                mAvatarEditTime = System.currentTimeMillis();
                mAvatarChanged = true;
            }
        }

        if (requestCode == SELECT_TASK_LOCATION_REQUEST && resultCode == RESULT_OK){
            mLatitude = data.getDoubleExtra(TaskListActivity.LATITUDE_EXTRA, 0);
            mLongitude = data.getDoubleExtra(TaskListActivity.LONGITUDE_EXTRA, 0);
            mIsAssignedToLocation = true;
            LatLng taskLocation = new LatLng(mLatitude, mLongitude);
            if (mMap == null){
                loadMap();
            } else {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(taskLocation));
                mMap.addCircle(new CircleOptions().center(taskLocation).radius(100).strokeWidth(5).strokeColor(0xa0ff3030));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(taskLocation, 15));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng taskLocation = new LatLng(mLatitude, mLongitude);
        mMap.addMarker(new MarkerOptions().position(taskLocation));
        mMap.addCircle(new CircleOptions().center(taskLocation).radius(100).strokeWidth(5).strokeColor(0xa0ff3030));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(taskLocation, 15));
        mMap.getUiSettings().setAllGesturesEnabled(false);
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(AVATAR_CHANGED_KEY, mAvatarChanged);
        if (mAvatarChanged){
            Bitmap avatar = ((BitmapDrawable) mTaskAvatar.getDrawable()).getBitmap();
            outState.putParcelable(AVATAR_KEY, avatar);
            outState.putLong(TaskListActivity.AVATAR_EDIT_TIME_EXTRA, mAvatarEditTime);
        }

        outState.putBoolean(TaskListActivity.IS_ASSIGNED_TO_LOCATION_EXTRA, mIsAssignedToLocation);
        if (mIsAssignedToLocation){
            outState.putDouble(TaskListActivity.LATITUDE_EXTRA, mLatitude);
            outState.putDouble(TaskListActivity.LONGITUDE_EXTRA, mLongitude);
        }


    }



}
