package com.serhiyboiko.taskmanager.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.serhiyboiko.taskmanager.R;


public class EditTaskActivity extends AppCompatActivity{

    private RelativeLayout mContainer;
    private EditText mTitleEditText;
    private EditText mCommentaryEditText;
    private int mItemId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_editor_activity);
        getSupportActionBar().setTitle(R.string.edit_task_activity_title);
        bindActivity();

        mItemId = getIntent().getIntExtra(TaskListActivity.ITEM_ID_EXTRA, -1);
        mTitleEditText.setText(getIntent().getStringExtra(TaskListActivity.TITLE_EXTRA));
        mCommentaryEditText.setText(getIntent().getStringExtra(TaskListActivity.COMMENTARY_EXTRA));
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
                taskData.putExtra(TaskListActivity.ITEM_ID_EXTRA, mItemId);
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


    private void bindActivity(){
        mTitleEditText = (EditText)findViewById(R.id.edit_text_title);
        mCommentaryEditText = (EditText)findViewById(R.id.edit_text_commentary);
        mContainer = (RelativeLayout)findViewById(R.id.task_editor_container);

    }
}
