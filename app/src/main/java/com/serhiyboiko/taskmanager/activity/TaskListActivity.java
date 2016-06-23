package com.serhiyboiko.taskmanager.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.adapter.TaskListAdapter;
import com.serhiyboiko.taskmanager.dialog.MaterialDialogFragment;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.alarm_manager.TaskAutoFinishManager;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsDeserializer;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

import io.realm.Realm;

public class TaskListActivity extends AppCompatActivity implements MaterialDialogFragment.DialogListener {


    private RecyclerView mTaskListView;
    private FloatingActionButton mNewTaskButton;
    private ArrayList<Task> mTaskListArray;
    private TaskListAdapter mTaskListAdapter;
    private SharedPrefsSerializer mSharedPrefsSerializer;
    private TaskAutoFinishManager mTaskAutoFinishManager;
    private RealmIO mRealmIO;
    private int[] mBackgroundColors;
    private boolean mBackPressed;
    private Handler mHandler;
    private Runnable mRunnable;
    private BroadcastReceiver mBroadcastReceiver;

    private static final int CREATE_NEW_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;
    private static final int SETTINGS_REQUEST = 3;

    final static String SAVED_TASK_LIST = "task_list";
    final static String ITEM_ID_EXTRA = "item_id";
    final static String TITLE_EXTRA = "title";
    final static String COMMENTARY_EXTRA = "commentary";

    private static final int UNREGISTERED = 0;

    private SharedPrefsDeserializer mSharedPrefsDeserializer;

    public String test = "test string";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list_activity);
        bindActivity();
        getSupportActionBar().setTitle(R.string.task_list_activity_title);

        mSharedPrefsDeserializer = new SharedPrefsDeserializer(this);
        mSharedPrefsSerializer = new SharedPrefsSerializer(this);

        mRealmIO = new RealmIO(this);

        mTaskAutoFinishManager = new TaskAutoFinishManager(this);

        mTaskListArray = new ArrayList<>();
        mBackgroundColors = mSharedPrefsDeserializer.getTaskBackgroundColors();
        mTaskListAdapter = new TaskListAdapter(this, mTaskListArray, mBackgroundColors,
                mSharedPrefsSerializer, mTaskAutoFinishManager, mRealmIO);
        if (savedInstanceState != null){
            mTaskListArray.addAll((ArrayList<Task>) savedInstanceState.get(SAVED_TASK_LIST));
            mTaskListAdapter.notifyDataSetChanged();
        } else {
            mTaskListArray.addAll(mRealmIO.getAllTasks());
            int currentSortingId = mSharedPrefsDeserializer.getListSorting();
            switch (currentSortingId){
                case R.id.menu_sort_az:
                    Collections.sort(mTaskListArray, new Task.ComparatorAZ());
                    break;
                case R.id.menu_sort_za:
                    Collections.sort(mTaskListArray, new Task.ComparatorZA());
                    break;
                case R.id.menu_sort_new_old:
                    Collections.sort(mTaskListArray, new Task.ComparatorNewerOlder());
                    break;
                case R.id.menu_sort_old_new:
                    Collections.sort(mTaskListArray, new Task.ComparatorOlderNewer());
                    break;
            }
            mTaskListAdapter.notifyDataSetChanged();
        }
        mTaskListView.setLayoutManager(new LinearLayoutManager(this));
        mTaskListView.setAdapter(mTaskListAdapter);

        mNewTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(TaskListActivity.this, NewTaskActivity.class), CREATE_NEW_TASK_REQUEST);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTaskListAdapter.notifyDataSetChanged();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long taskId = intent.getLongExtra("primary_key", 0);
                for (Task task:mTaskListArray){
                    if (taskId == task.getId()){
                        Realm.getDefaultInstance().beginTransaction();
                        task.setTaskEnd(new GregorianCalendar());
                        task.setAlertRequestCode(UNREGISTERED);
                        Realm.getDefaultInstance().commitTransaction();
                        break;
                    }
                }
                mTaskListAdapter.notifyDataSetChanged();
                Log.i("in inner reciever", "notifyDataSetChanged");
            }
        };

        IntentFilter intentFilter = new IntentFilter("com.serhiyboiko.taskmanager.UPDATE_DATA");
        intentFilter.addCategory("android.intent.category.DEFAULT");
        registerReceiver(mBroadcastReceiver, intentFilter);
        Log.i("in inner reciever", "registered");


    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBroadcastReceiver);
        if(mHandler != null){
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealmIO.close();
    }

    //bind references to widgets
    private void bindActivity (){
        mTaskListView = (RecyclerView) findViewById(R.id.task_list);
        mNewTaskButton =(FloatingActionButton)findViewById(R.id.new_task_button);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lask_list_menu, menu);
        int sortingId = mSharedPrefsDeserializer.getListSorting();
        switch (sortingId) {
            case R.id.menu_sort_az:
            case R.id.menu_sort_za:
            case R.id.menu_sort_new_old:
            case R.id.menu_sort_old_new:
                menu.findItem(sortingId).setChecked(true);
                return true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.menu_new_task:
                startActivityForResult(new Intent(TaskListActivity.this, NewTaskActivity.class), CREATE_NEW_TASK_REQUEST);
                return true;
            case R.id.menu_sort_az:
                Collections.sort(mTaskListArray, new Task.ComparatorAZ());
                mTaskListAdapter.notifyDataSetChanged();
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_az);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_za:
                Collections.sort(mTaskListArray, new Task.ComparatorZA());
                mTaskListAdapter.notifyDataSetChanged();
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_za);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_new_old:
                Collections.sort(mTaskListArray, new Task.ComparatorNewerOlder());
                mTaskListAdapter.notifyDataSetChanged();
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_new_old);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_old_new:
                Collections.sort(mTaskListArray, new Task.ComparatorOlderNewer());
                mTaskListAdapter.notifyDataSetChanged();
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_old_new);
                item.setChecked(true);
                return true;
            case R.id.menu_settings:
                startActivityForResult(new Intent(TaskListActivity.this, SettingsActivity.class), SETTINGS_REQUEST);
                return true;
            case R.id.menu_fill:
                generateRandomTasks();
                mTaskListAdapter.notifyDataSetChanged();
                return true;
            case R.id.menu_remove_all:
                MaterialDialogFragment.newInstance(R.string.dialog_delete_all_tasks).show(getSupportFragmentManager(), getString(R.string.dialog_delete_all_tasks));
                return true;
            case R.id.menu_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void generateRandomTasks() {
        int listViewHeight = mTaskListView.getHeight();
        int listSize =  mTaskListAdapter.getItemCount();
        int contentHeight = 0;
        int lineAmount = 0;
        String[] titles = {"Meeting", "Shopping", "Exercises", "Chores", "Work",  "Cinema", "TV show", "Some stuff", "More stuff", "Another stuff"};
        String[] commentary = {"Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                " Maecenas rutrum feugiat erat, sed vehicula purus dictum nec.",
                " Etiam blandit pulvinar maximus.", " Aliquam in ex euismod, fringilla ipsum eu, tempus nisi.",
                " Nunc sed ipsum massa. Suspendisse vitae vulputate quam."};

        //check height of existing items
        Log.i("index", listSize+"");
        TaskListAdapter.ViewHolder viewHolder = mTaskListAdapter.onCreateViewHolder(mTaskListView, 0);
        for (int i = 0; i<listSize; i++){
            mTaskListAdapter.onBindViewHolder(viewHolder, i);
            View oldItem = viewHolder.itemView;
            oldItem.measure(View.MeasureSpec.makeMeasureSpec(mTaskListView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            Log.i("oldItem Height", oldItem.getMeasuredHeight()+"");
            contentHeight += oldItem.getMeasuredHeight();
        }

        int index = listSize;
        //generate new items
        while (contentHeight < listViewHeight*3) {
            String line = "";
            String title = "";
            lineAmount = (int) (Math.random() * 4) + 1;
            title = titles[(int) (Math.random() * titles.length)];
            for (int i = 0; i < lineAmount; i++) {
                line += commentary[i];
            }
            Task task = new Task(this, line, title);
            mTaskListArray.add(task);
            mRealmIO.putTask(task);
            mTaskListAdapter.onBindViewHolder(viewHolder, index);
            View newItem = viewHolder.itemView;
            newItem.measure(View.MeasureSpec.makeMeasureSpec(mTaskListView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            Log.i("mTaskListArray.size", mTaskListArray.size()+"");
            Log.i("index", index+"");
            Log.i("newItem Height", newItem.getMeasuredHeight()+"");
            contentHeight += newItem.getMeasuredHeight();
            index++;
        }
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
                case CREATE_NEW_TASK_REQUEST:
                    if (data == null) {
                        return;
                    }
                    //extract task data
                    title = data.getStringExtra(TITLE_EXTRA);
                    commentary = data.getStringExtra(COMMENTARY_EXTRA);
                    //add task to list
                    Task task = new Task(this, commentary, title);
                    mTaskListArray.add(0, task);
                    mRealmIO.putTask(task);
                    mTaskListAdapter.notifyDataSetChanged();
                    Log.i("array", mTaskListArray.toString());
                    break;
                case EDIT_TASK_REQUEST:


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

                    Realm.getDefaultInstance().beginTransaction();
                    selected_item.setTitle(title);
                    selected_item.setCommentary(commentary);
                    Realm.getDefaultInstance().commitTransaction();

                    mTaskListAdapter.notifyDataSetChanged();
                    break;
                case SETTINGS_REQUEST:
                    mBackgroundColors = mSharedPrefsDeserializer.getTaskBackgroundColors();
                    mTaskListAdapter.setBackgroundColors(mBackgroundColors);
                    //mTaskAutoFinishManager.unregisterAllTasksAutoFinish(mTaskListArray);
                    mTaskAutoFinishManager.updateTaskAutoFinishTime(mTaskListArray);
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
    public void onBackPressed() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mBackPressed = false;
            }
        };
        if(mBackPressed){
            super.onBackPressed();
        } else {
            mBackPressed = true;
            mHandler.postDelayed(mRunnable, 2000);
            Snackbar.make(findViewById(R.id.task_list_activity_container),
                    getString(R.string.press_back_again_to_exit), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPositive(int titleId) {
        switch (titleId){
            case R.string.dialog_delete_all_tasks:
                mTaskListArray.clear();
                mRealmIO.removeAllTasks();
                mTaskListAdapter.notifyDataSetChanged();
                break;
        }

    }

}
