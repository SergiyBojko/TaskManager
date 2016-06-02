package com.serhiyboiko.taskmanager.activity;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.adapter.TaskListAdapter;
import com.serhiyboiko.taskmanager.model.Task;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class TaskListActivity extends AppCompatActivity{

    public static final String TASK_FINISHED_IN = "Task finished in %02d:%02d:%02d";
    public static final String TASK_FINISHED = "Task finished";
    private ListView mTaskListView;
    private Button mNewTaskButton;
    private ArrayList<Task> mTaskListArray;
    private TaskListAdapter mTaskListAdapter;
    static final int CREATE_NEW_TASK = 1;
    static final int EDIT_TASK = 2;
    static final String SAVED_TASK_LIST = "task_list";
    static final String ITEM_ID_EXTRA = "item_id";
    final static String TITLE_EXTRA = "title";
    final static String COMMENTARY_EXTRA = "commentary";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            mTaskListArray = (ArrayList<Task>)savedInstanceState.get(SAVED_TASK_LIST);
        } else {
			mTaskListArray = new ArrayList<>();
		}
        setContentView(R.layout.activity_task_list);
        bindActivity();
        mNewTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(TaskListActivity.this, NewTaskActivity.class), CREATE_NEW_TASK);
            }
        });
        mTaskListAdapter = new TaskListAdapter(mTaskListArray, getLayoutInflater());
        mTaskListView.setAdapter(mTaskListAdapter);
        mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task item = mTaskListArray.get(position);
                if (item.getTaskStart() == null){
                    item.setTaskStart(new GregorianCalendar());
                    mTaskListAdapter.notifyDataSetChanged();
                } else {
                    if (item.getTaskEnd() == null){
                        item.setTaskEnd(new GregorianCalendar());
                        long elapsedTimeInMills = item.getTaskEnd().getTimeInMillis() - item.getTaskStart().getTimeInMillis();
                        item.setTimeSpend(elapsedTimeInMills);
                        int hours = (int)TimeUnit.MILLISECONDS.toHours(elapsedTimeInMills);
                        int minutes = (int)(TimeUnit.MILLISECONDS.toMinutes(elapsedTimeInMills) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTimeInMills)));
                        int seconds = (int)(TimeUnit.MILLISECONDS.toSeconds(elapsedTimeInMills) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTimeInMills)));
                        Snackbar.make(findViewById(R.id.task_list_activity_container),
                                String.format(TASK_FINISHED_IN, hours, minutes, seconds), Snackbar.LENGTH_SHORT).show();
                        mTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Snackbar.make(findViewById(R.id.task_list_activity_container),
                                TASK_FINISHED, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mTaskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(TaskListActivity.this, EditTaskActivity.class);
                intent.putExtra(ITEM_ID_EXTRA, position);
                intent.putExtra(TITLE_EXTRA, mTaskListArray.get(position).getTitle());
                intent.putExtra(COMMENTARY_EXTRA, mTaskListArray.get(position).getCommentary());
                startActivityForResult(intent, EDIT_TASK);
                return true;
            }
        });
    }

    //bind references to widgets
    private void bindActivity (){
        mTaskListView = (ListView)findViewById(R.id.task_list);
        mNewTaskButton =(Button)findViewById(R.id.new_task_button);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String title;
        String commentary;
        int item_id;
        //check result code
        if (resultCode == RESULT_OK){
            //check request code
            switch (requestCode) {
                case CREATE_NEW_TASK:
                    if (data == null) {
                        return;
                    }
                    //extract task data
                    title = data.getStringExtra(TITLE_EXTRA);
                    commentary = data.getStringExtra(COMMENTARY_EXTRA);
                    //add task to list
                    mTaskListArray.add(new Task(title, commentary));
                    //refresh listview content
                    mTaskListAdapter.notifyDataSetChanged();
                    break;
                case EDIT_TASK:

                    Task selected_item;
                    if (data == null) {
                        return;
                    }
                    //extract task data
                    title = data.getStringExtra(TITLE_EXTRA);
                    commentary = data.getStringExtra(COMMENTARY_EXTRA);
                    item_id = data.getIntExtra(ITEM_ID_EXTRA, 0);
                    //get list item and change it fields
                    selected_item = mTaskListArray.get(item_id);
                    selected_item.setTitle(title);
                    selected_item.setCommentary(commentary);
                    mTaskListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(SAVED_TASK_LIST, mTaskListArray);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTaskListArray = (ArrayList<Task>)savedInstanceState.get(SAVED_TASK_LIST);
    }

}
