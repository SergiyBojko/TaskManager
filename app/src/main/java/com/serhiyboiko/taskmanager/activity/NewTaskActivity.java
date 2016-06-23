package com.serhiyboiko.taskmanager.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.serhiyboiko.taskmanager.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amegar on 26.05.2016.
 */
public class NewTaskActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mTitleEditText;
    private EditText mCommentaryEditText;
    private ImageButton mTitleVoiceButton;
    private ImageButton mCommentaryVoiceButton;
    private int mSelectedVoiceInputId;
    private int mItemId;

    private final static int VOICE_RECOGNITION_REQUEST = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_editor_activity);
        getSupportActionBar().setTitle(R.string.new_task_activity_title);
        bindActivity();

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_editor_menu, menu);
        menu.findItem(R.id.menu_save_task).getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitleEditText.getText().toString();
                String commentary = mCommentaryEditText.getText().toString();
                if (title.isEmpty()) {
                    mTitleEditText.setError(getString(R.string.enter_title));
                    return;
                }
                Intent taskData = new Intent();
                taskData.putExtra(TaskListActivity.TITLE_EXTRA, title);
                taskData.putExtra(TaskListActivity.COMMENTARY_EXTRA, commentary);
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

    private void startVoiceRecognitionActivity(int promptId)    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(promptId));
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST);
    }

    private void bindActivity(){
        mTitleEditText = (EditText)findViewById(R.id.edit_text_title);
        mCommentaryEditText = (EditText)findViewById(R.id.edit_text_commentary);
        mTitleVoiceButton = (ImageButton) findViewById(R.id.edit_text_title_voice_recognition);
        mCommentaryVoiceButton = (ImageButton) findViewById(R.id.edit_text_commentary_voice_recognition);
    }

    @Override
    public void onClick(View v) {
        mSelectedVoiceInputId = v.getId();
        switch (mSelectedVoiceInputId){
            case R.id.edit_text_title_voice_recognition:
                startVoiceRecognitionActivity(R.string.enter_title_voice_rec_prompt);
                break;
            case R.id.edit_text_commentary_voice_recognition:
                startVoiceRecognitionActivity(R.string.enter_commentary_voice_rec_prompt);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == VOICE_RECOGNITION_REQUEST && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            switch (mSelectedVoiceInputId){
                case R.id.edit_text_title_voice_recognition:
                    mTitleEditText.setText(mTitleEditText.getText().append(" ").append(matches.get(0)));
                    break;
                case R.id.edit_text_commentary_voice_recognition:
                    mCommentaryEditText.setText(mCommentaryEditText.getText().append(" ").append(matches.get(0)));
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
