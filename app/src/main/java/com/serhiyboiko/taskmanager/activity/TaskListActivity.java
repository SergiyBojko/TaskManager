package com.serhiyboiko.taskmanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class TaskListActivity extends AppCompatActivity implements MaterialDialogFragment.DialogListener {


    private static final String TAG = "TaskListActivity";
    private RecyclerView mTaskListView;
    private FloatingActionButton mNewTaskButton;
    private RealmResults<Task> mTaskListRealmResults;
    private TaskListAdapter mTaskListAdapter;
    private SharedPrefsSerializer mSharedPrefsSerializer;
    private TaskAutoFinishManager mTaskAutoFinishManager;
    private RealmIO mRealmIO;
    private int[] mBackgroundColors;
    private boolean mBackPressed;
    private Handler mHandler;
    private Runnable mRunnable;

    private static final int CREATE_NEW_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;
    private static final int SETTINGS_REQUEST = 3;

    final static String ITEM_ID_EXTRA = "item_id";
    final static String TITLE_EXTRA = "title";
    final static String COMMENTARY_EXTRA = "commentary";
    private static final String REQUEST_CODE_EXTRA = "request_code";


    private SharedPrefsDeserializer mSharedPrefsDeserializer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list_activity);
        bindActivity();
        getSupportActionBar().setTitle(R.string.task_list_activity_title);

        mSharedPrefsDeserializer = new SharedPrefsDeserializer(this);
        mSharedPrefsSerializer = new SharedPrefsSerializer(this);

        mRealmIO = new RealmIO(this);
        mRealmIO.getRealm().addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                mTaskListAdapter.notifyDataSetChanged();
            }
        });

        mTaskAutoFinishManager = new TaskAutoFinishManager(this);

        mBackgroundColors = mSharedPrefsDeserializer.getTaskBackgroundColors();

        mTaskListRealmResults = mRealmIO.getAllTasks();
        mTaskListAdapter = new TaskListAdapter(this, mTaskListRealmResults, mBackgroundColors, mTaskAutoFinishManager, mRealmIO);
        int currentSortingId = mSharedPrefsDeserializer.getListSorting();
        switch (currentSortingId){
            case R.id.menu_sort_az:
                mTaskListRealmResults = mTaskListRealmResults.sort("mTitle", Sort.ASCENDING, "mTaskStart", Sort.ASCENDING);
                break;
            case R.id.menu_sort_za:
                mTaskListRealmResults = mTaskListRealmResults.sort("mTitle", Sort.DESCENDING, "mTaskStart", Sort.ASCENDING);
                break;
            case R.id.menu_sort_new_old:
                mTaskListRealmResults = mTaskListRealmResults.sort("mTaskStart", Sort.DESCENDING, "mTitle", Sort.ASCENDING);
                break;
            case R.id.menu_sort_old_new:
                mTaskListRealmResults = mTaskListRealmResults.sort("mTaskStart", Sort.ASCENDING, "mTitle", Sort.ASCENDING);
                break;
        }
        mTaskListAdapter.setTaskRealmResults(mTaskListRealmResults);

        mTaskListView.setLayoutManager(new LinearLayoutManager(this));
        mTaskListView.setAdapter(mTaskListAdapter);

        mNewTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskListActivity.this, EditTaskActivity.class);
                intent.putExtra(REQUEST_CODE_EXTRA, CREATE_NEW_TASK_REQUEST);
                startActivityForResult(intent, CREATE_NEW_TASK_REQUEST);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mHandler != null){
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealmIO.getRealm().removeAllChangeListeners();
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
                Intent intent = new Intent(TaskListActivity.this, EditTaskActivity.class);
                intent.putExtra(REQUEST_CODE_EXTRA, CREATE_NEW_TASK_REQUEST);
                startActivityForResult(intent, CREATE_NEW_TASK_REQUEST);
                return true;
            case R.id.menu_sort_az:
                mTaskListRealmResults = mTaskListRealmResults.sort("mTitle", Sort.ASCENDING, "mTaskStart", Sort.ASCENDING);
                mTaskListAdapter.setTaskRealmResults(mTaskListRealmResults);
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_az);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_za:
                mTaskListRealmResults = mTaskListRealmResults.sort("mTitle", Sort.DESCENDING, "mTaskStart", Sort.ASCENDING);
                mTaskListAdapter.setTaskRealmResults(mTaskListRealmResults);
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_za);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_new_old:
                mTaskListRealmResults = mTaskListRealmResults.sort("mTaskStart", Sort.DESCENDING, "mTitle", Sort.ASCENDING);
                mTaskListAdapter.setTaskRealmResults(mTaskListRealmResults);
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_new_old);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_old_new:
                mTaskListRealmResults = mTaskListRealmResults.sort("mTaskStart", Sort.ASCENDING, "mTitle", Sort.ASCENDING);
                mTaskListAdapter.setTaskRealmResults(mTaskListRealmResults);
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
        TaskListAdapter.ViewHolder viewHolder = mTaskListAdapter.onCreateViewHolder(mTaskListView, 0);
        for (int i = 0; i<listSize; i++){
            mTaskListAdapter.onBindViewHolder(viewHolder, mTaskListRealmResults.get(i));
            View oldItem = viewHolder.itemView;
            oldItem.measure(View.MeasureSpec.makeMeasureSpec(mTaskListView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            contentHeight += oldItem.getMeasuredHeight();
        }
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
            mRealmIO.putTask(task);
            mTaskListAdapter.onBindViewHolder(viewHolder, task);
            View newItem = viewHolder.itemView;
            newItem.measure(View.MeasureSpec.makeMeasureSpec(mTaskListView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            contentHeight += newItem.getMeasuredHeight();
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
                    mRealmIO.putTask(task);
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
                    selected_item = mTaskListRealmResults.get(item_id);

                    Realm.getDefaultInstance().beginTransaction();
                    selected_item.setTitle(title);
                    selected_item.setCommentary(commentary);
                    Realm.getDefaultInstance().commitTransaction();
                    break;
                case SETTINGS_REQUEST:
                    mBackgroundColors = mSharedPrefsDeserializer.getTaskBackgroundColors();
                    mTaskListAdapter.setBackgroundColors(mBackgroundColors);
                    mTaskAutoFinishManager.updateTaskAutoFinishTime(mTaskListRealmResults);
                    mTaskListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }


    /*
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(SAVED_TASK_LIST, mTaskListArray);
    }
    */

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
                mRealmIO.removeAllTasks();
                mTaskListAdapter.notifyDataSetChanged();
                break;
        }

    }

}
