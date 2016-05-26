package com.serhiyboiko.taskmanager.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.adapters.TaskListAdapter;
import com.serhiyboiko.taskmanager.models.Task;

import java.util.ArrayList;

public class TaskListActivity extends AppCompatActivity{

    private ListView mTaskListView;
    private Button mNewTaskButton;
    private ArrayList<Task> mTaskListArray;
    private TaskListAdapter mTaskListAdapter;
    private static final int CREATE_NEW_TASK = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            mTaskListArray = (ArrayList<Task>)savedInstanceState.get("task_list");
        }
        setContentView(R.layout.activity_task_list);
        bindActivity();
        mNewTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(TaskListActivity.this, NewTaskActivity.class), CREATE_NEW_TASK);
            }
        });
        //create new arrayList if it doesn't exist
        if (mTaskListArray == null) {
            mTaskListArray = new ArrayList<>();
        }
        mTaskListAdapter = new TaskListAdapter(mTaskListArray, getLayoutInflater());
        mTaskListView.setAdapter(mTaskListAdapter);
    }

    //bind references to widgets
    private void bindActivity (){
        mTaskListView = (ListView)findViewById(R.id.task_list);
        mNewTaskButton =(Button)findViewById(R.id.new_task_button);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check result code
        if (resultCode == RESULT_OK){
            //check request code
            if (requestCode == CREATE_NEW_TASK){
                String title;
                String commentary;
                if (data == null) {
                    return;
                }

                //extract task data
                title = data.getStringExtra("title");
                commentary = data.getStringExtra("commentary");
                //add task to list
                mTaskListArray.add(new Task(title, commentary));
                //refresh listview content
                mTaskListAdapter.notifyDataSetChanged();
            }
        }
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList("task_list", mTaskListArray);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTaskListArray = (ArrayList<Task>)savedInstanceState.get("task_list");
    }

}
