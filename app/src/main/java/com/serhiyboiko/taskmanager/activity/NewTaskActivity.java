package com.serhiyboiko.taskmanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.serhiyboiko.taskmanager.R;

/**
 * Created by Amegar on 26.05.2016.
 */
public class NewTaskActivity extends AppCompatActivity{

    private RelativeLayout mContainer;
    private EditText mTitleEditText;
    private EditText mCommentaryEditText;
    private Button mSaveTaskButton;
    private Button mCancelButton;
    private final static String TOOLBAR_TITLE = "New Task";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);
        getSupportActionBar().setTitle(TOOLBAR_TITLE);
        bindActivity();

        mSaveTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitleEditText.getText().toString();
                String commentary = mCommentaryEditText.getText().toString();
                if (title.isEmpty()) {
                    Snackbar.make(mContainer, getString(R.string.enter_title), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Intent taskData = new Intent();
                taskData.putExtra(TaskListActivity.TITLE_EXTRA, title);
                taskData.putExtra(TaskListActivity.COMMENTARY_EXTRA, commentary);
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
        mContainer = (RelativeLayout)findViewById(R.id.task_editor_container);
    }
}
