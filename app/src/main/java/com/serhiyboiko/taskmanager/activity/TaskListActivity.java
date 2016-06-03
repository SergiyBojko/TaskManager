package com.serhiyboiko.taskmanager.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.adapter.TaskListAdapter;
import com.serhiyboiko.taskmanager.utils.json.JsonDeserializer;
import com.serhiyboiko.taskmanager.utils.json.JsonSerializer;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsDeserializer;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsSerializer;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class TaskListActivity extends AppCompatActivity{


    private ListView mTaskListView;
    private Button mNewTaskButton;
    private ArrayList<Task> mTaskListArray;
    private TaskListAdapter mTaskListAdapter;
    private JsonSerializer mJsonSerializer;
    private SharedPrefsSerializer mSharedPrefsSerializer;

    static final int CREATE_NEW_TASK = 1;
    static final int EDIT_TASK = 2;

    static final String SAVED_TASK_LIST = "task_list";
    static final String ITEM_ID_EXTRA = "item_id";
    final static String TITLE_EXTRA = "title";
    final static String COMMENTARY_EXTRA = "commentary";

    public static final String TASK_FINISHED_IN = "Task finished in %02d:%02d:%02d";
    public static final String TASK_FINISHED = "Task finished";
    public static final String JSON_TASK_LIST_FILE_NAME = "task_list.json";
    public static final String SHARED_PREFS_TASK_LIST_FILE_NAME = "task_list.sharedprefs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        bindActivity();
        mTaskListArray = new ArrayList<>();
        mTaskListAdapter = new TaskListAdapter(mTaskListArray, getLayoutInflater());
        if (savedInstanceState != null){
            mTaskListArray.addAll((ArrayList<Task>)savedInstanceState.get(SAVED_TASK_LIST));
            mTaskListAdapter.notifyDataSetChanged();
            Log.i("mTaskListArray", mTaskListArray.toString());
        } else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    // load data from JSON file
                    //mTaskListArray.addAll(new JsonDeserializer(TaskListActivity.this, JSON_TASK_LIST_FILE_NAME).getTaskListFromJson());


                    mTaskListArray.addAll(new SharedPrefsDeserializer(TaskListActivity.this, SHARED_PREFS_TASK_LIST_FILE_NAME).getTaskListFromSharedPrefs());
                    mTaskListAdapter.notifyDataSetChanged();
                    Log.i("mTaskListArray", mTaskListArray.toString());
            }
            });
       }

        mTaskListView.setAdapter(mTaskListAdapter);
        mJsonSerializer = new JsonSerializer(this, JSON_TASK_LIST_FILE_NAME);
        mSharedPrefsSerializer = new SharedPrefsSerializer(this, SHARED_PREFS_TASK_LIST_FILE_NAME);

        mNewTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(TaskListActivity.this, NewTaskActivity.class), CREATE_NEW_TASK);
            }
        });
        mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task item = mTaskListArray.get(position);
                if (item.getTaskStart() == null){
                    item.setTaskStart(new GregorianCalendar());
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
                    } else {
                        Snackbar.make(findViewById(R.id.task_list_activity_container),
                                TASK_FINISHED, Snackbar.LENGTH_SHORT).show();
                    }
                }
                updateData();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.menu_fill:
                int listViewHeight = mTaskListView.getHeight();
                int index = 0 + mTaskListAdapter.getCount();
                int contentHeight = 0;
                int lineAmount = 0;
                for (int i = 0; i<index; i++){
                    View oldItem = mTaskListAdapter.getView(i, null, mTaskListView);
                    oldItem.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    contentHeight += oldItem.getMeasuredHeight();
                    Log.i("old item height", oldItem.getMeasuredHeight() + "");
                    Log.i("old item width", oldItem.getMeasuredWidth() + "");
                    Log.i("listViewHeight", listViewHeight + "");
                    Log.i("contentHeight", contentHeight + "");
                }

                while (contentHeight < listViewHeight*3) {
                    String line = "";
                    String title = "";
                    lineAmount = (int) (Math.random() * 4);
                    title = "Title" + " " + index;
                    for (int i = 0; i < lineAmount; i++) {
                        if (i != lineAmount-1){
                            line += "line " + i + "\n";
                        } else {
                            line += "line " + i;
                        }
                    }
                    Task task = new Task(title, line);
                    mTaskListArray.add(task);
                    updateData();
                    View newItem = mTaskListAdapter.getView(index, null, mTaskListView);
                    newItem.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    contentHeight += newItem.getMeasuredHeight();
                    Log.i("new item height", newItem.getMeasuredHeight() + "");
                    Log.i("new item width", newItem.getMeasuredWidth() + "");
                    Log.i("listViewHeight", listViewHeight + "");
                    Log.i("contentHeight", contentHeight + "");
                    Log.i("lineAmount", lineAmount + "");
                    index++;

                }
                break;
            case R.id.menu_remove_all:
                mTaskListArray.clear();
                updateData();
                break;
        }
        return true;
    }

    //updates data in listview and saves it to internal storage
    private void updateData() {
        mTaskListAdapter.notifyDataSetChanged();
        Log.i("in updateData", mTaskListArray.toString());
        mJsonSerializer.saveTaskListToJson(mTaskListArray);
        mSharedPrefsSerializer.saveTaskListToSharedPrefs(mTaskListArray);
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
                break;
        }
            Log.i("in activity result", mTaskListArray.toString());
            updateData();
        }
    }




    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(SAVED_TASK_LIST, mTaskListArray);
    }


}
