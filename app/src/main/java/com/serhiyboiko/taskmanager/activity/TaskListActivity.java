package com.serhiyboiko.taskmanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.adapter.TaskListAdapter;
import com.serhiyboiko.taskmanager.dialog.MaterialDialogFragment;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsDeserializer;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class TaskListActivity extends AppCompatActivity implements MaterialDialogFragment.DialogListener {



    private ListView mTaskListView;
    private FloatingActionButton mNewTaskButton;
    private ArrayList<Task> mTaskListArray;
    private TaskListAdapter mTaskListAdapter;
    private SharedPrefsSerializer mSharedPrefsSerializer;
    private int[] mBackgroundColors;
    private boolean mBackPressed;
    private Handler mHandler;
    private Runnable mRunnable;

    private static final int CREATE_NEW_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;
    private static final int SETTINGS_REQUEST = 3;

    final static String SAVED_TASK_LIST = "task_list";
    final static String ITEM_ID_EXTRA = "item_id";
    final static String TITLE_EXTRA = "title";
    final static String COMMENTARY_EXTRA = "commentary";

    private final static String HH_MM_SS = " %02d:%02d:%02d";
    private SharedPrefsDeserializer mSharedPrefsDeserializer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list_activity);
        bindActivity();
        getSupportActionBar().setTitle(R.string.task_list_activity_title);

        mSharedPrefsDeserializer = new SharedPrefsDeserializer(this);
        mSharedPrefsSerializer = new SharedPrefsSerializer(this);

        mTaskListArray = new ArrayList<>();
        mBackgroundColors = mSharedPrefsDeserializer.getTaskBackgroundColors();
        mTaskListAdapter = new TaskListAdapter(mTaskListArray, mBackgroundColors, this);
        if (savedInstanceState != null){
            mTaskListArray.addAll((ArrayList<Task>) savedInstanceState.get(SAVED_TASK_LIST));
            mTaskListAdapter.notifyDataSetChanged();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mTaskListArray.addAll(mSharedPrefsDeserializer.getTaskList());
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

                    //Чи тут краще AsyncTask використовувати?
                    mTaskListView.post(new Runnable() {
                        @Override
                        public void run() {
                            mTaskListAdapter.notifyDataSetChanged();
                        }
                    });

                }
            }).start();
        }
        mTaskListView.setAdapter(mTaskListAdapter);

        mNewTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(TaskListActivity.this, NewTaskActivity.class), CREATE_NEW_TASK_REQUEST);
            }
        });
        mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task item = mTaskListArray.get(position);
                if (item.getTaskStart() == null) {
                    item.setTaskStart(new GregorianCalendar());
                } else {
                    if (item.getTaskEnd() == null) {
                        item.setTaskEnd(new GregorianCalendar());
                        long elapsedTimeInMills = item.getTaskEnd().getTimeInMillis() - item.getTaskStart().getTimeInMillis();
                        item.setTimeSpend(elapsedTimeInMills);
                        int hours = (int) TimeUnit.MILLISECONDS.toHours(elapsedTimeInMills);
                        int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(elapsedTimeInMills) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTimeInMills)));
                        int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(elapsedTimeInMills) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTimeInMills)));
                        Snackbar.make(findViewById(R.id.task_list_activity_container),
                                getString(R.string.task_finished_in) + String.format(HH_MM_SS, hours, minutes, seconds), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(R.id.task_list_activity_container),
                                getString(R.string.task_finished), Snackbar.LENGTH_SHORT).show();
                    }
                }
                updateData();
            }
        });

        mTaskListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mPreviousFirstVisibleItem;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mNewTaskButton.getVisibility() == View.VISIBLE && mPreviousFirstVisibleItem<firstVisibleItem){
                    mNewTaskButton.hide();

                }
                if (mNewTaskButton.getVisibility() == View.GONE && mPreviousFirstVisibleItem>firstVisibleItem){
                    mNewTaskButton.show();
                }
                mPreviousFirstVisibleItem = firstVisibleItem;
            }
        });

        registerForContextMenu(mTaskListView);
    }

    @Override
    protected void onDestroy() {
        if(mHandler != null){
            mHandler.removeCallbacks(mRunnable);
        }
        super.onDestroy();
    }

    //bind references to widgets
    private void bindActivity (){
        mTaskListView = (ListView)findViewById(R.id.task_list);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
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
                updateData();
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_az);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_za:
                Collections.sort(mTaskListArray, new Task.ComparatorZA());
                updateData();
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_za);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_new_old:
                Collections.sort(mTaskListArray, new Task.ComparatorNewerOlder());
                updateData();
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_new_old);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_old_new:
                Collections.sort(mTaskListArray, new Task.ComparatorOlderNewer());
                updateData();
                mSharedPrefsSerializer.saveSorting(R.id.menu_sort_old_new);
                item.setChecked(true);
                return true;
            case R.id.menu_settings:
                startActivityForResult(new Intent(TaskListActivity.this, SettingsActivity.class), SETTINGS_REQUEST);
                return true;
            case R.id.menu_fill:
                generateRandomTasks();
                return true;
            case R.id.menu_remove_all:
                MaterialDialogFragment.newInstance(getString(R.string.dialog_delete_all_tasks)).show(getSupportFragmentManager(), getString(R.string.dialog_delete_all_tasks));
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
        int index = 0 + mTaskListAdapter.getCount();
        int contentHeight = 0;
        int lineAmount = 0;
        String[] titles = {"Meeting", "Shopping", "Exercises", "Chores", "Work",  "Cinema", "TV show", "Some stuff", "More stuff", "Another stuff"};
        String[] commentary = {"Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                " Maecenas rutrum feugiat erat, sed vehicula purus dictum nec.",
                " Etiam blandit pulvinar maximus.", " Aliquam in ex euismod, fringilla ipsum eu, tempus nisi.",
                " Nunc sed ipsum massa. Suspendisse vitae vulputate quam."};
        //check height of existing items
        for (int i = 0; i<index; i++){
            View oldItem = mTaskListAdapter.getView(i, null, mTaskListView);
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
            Task task = new Task(title, line);
            mTaskListArray.add(task);
            updateData();
            View newItem = mTaskListAdapter.getView(index, null, mTaskListView);
            newItem.measure(View.MeasureSpec.makeMeasureSpec(mTaskListView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            Log.i("newItem.MeasuredHeight", newItem.getMeasuredHeight()+"");
            contentHeight += newItem.getMeasuredHeight();
            index++;

        }
    }

    //updates data in listview and saves it to internal storage
    private void updateData() {
        mTaskListAdapter.notifyDataSetChanged();
        mSharedPrefsSerializer.saveTaskList(mTaskListArray);
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
                    mTaskListArray.add(0, new Task(title, commentary));
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
                    selected_item.setTitle(title);
                    selected_item.setCommentary(commentary);
                    break;
                case SETTINGS_REQUEST:
                    mBackgroundColors = mSharedPrefsDeserializer.getTaskBackgroundColors();
                    mTaskListAdapter.setBackgroundColors(mBackgroundColors);
                    break;

            }
            updateData();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.task_list_context_menu, menu);
        int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
        Task selectedTask = mTaskListArray.get(position);
        if(selectedTask.getTaskStart() == null){
            menu.findItem(R.id.context_menu_restart).setEnabled(false);
        } else {
            menu.findItem(R.id.context_menu_restart).setEnabled(true);
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
        switch (id){
            case R.id.context_menu_edit:
                Intent intent = new Intent(TaskListActivity.this, EditTaskActivity.class);
                intent.putExtra(ITEM_ID_EXTRA, position);
                intent.putExtra(TITLE_EXTRA, mTaskListArray.get(position).getTitle());
                intent.putExtra(COMMENTARY_EXTRA, mTaskListArray.get(position).getCommentary());
                startActivityForResult(intent, EDIT_TASK_REQUEST);
                return true;
            case R.id.context_menu_restart:
                Task selectedTask = mTaskListArray.get(position);
                selectedTask.setTaskStart(new GregorianCalendar());
                selectedTask.setTaskEnd(null);
                updateData();
                return true;
            case R.id.context_menu_delete:
                mTaskListArray.remove(position);
                updateData();
                return true;
            default:
                return super.onContextItemSelected(item);
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
    public void onPositive() {
        mTaskListArray.clear();
        updateData();
    }
}
