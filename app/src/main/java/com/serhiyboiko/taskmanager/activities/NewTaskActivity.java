package com.serhiyboiko.taskmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.serhiyboiko.taskmanager.R;

/**
 * Created by Amegar on 26.05.2016.
 */
public class NewTaskActivity extends AppCompatActivity{
    private EditText mTitleEditText;
    private EditText mCommentaryEditText;
    private Button mSaveTaskButton;
    private Button mCancelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        bindActivity();

        mSaveTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitleEditText.getText().toString();
                String commentary = mCommentaryEditText.getText().toString();
                if (title.isEmpty() || commentary.isEmpty()) {
                    Toast.makeText(NewTaskActivity.this, getString(R.string.enter_title_and_commentary), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent taskData = new Intent();
                taskData.putExtra("title", mTitleEditText.getText().toString());
                taskData.putExtra("commentary", mCommentaryEditText.getText().toString());
                setResult(RESULT_OK, taskData);
                finish();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, null);
                finish();
            }
        });
    }

    private void bindActivity(){
        mTitleEditText = (EditText)findViewById(R.id.edit_text_title);
        mCommentaryEditText = (EditText)findViewById(R.id.edit_text_commentary);
        mSaveTaskButton = (Button)findViewById(R.id.save_button);
        mCancelButton = (Button)findViewById(R.id.cancel_button);
    }
}
