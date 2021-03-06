package com.serhiyboiko.taskmanager.activity;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TabHost;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.adapter.StatisticsAdapter;
import com.serhiyboiko.taskmanager.adapter.TaskListAdapter;
import com.serhiyboiko.taskmanager.dialog.ConfirmationDialog;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.model.TaskExecInfo;
import com.serhiyboiko.taskmanager.service.TaskManager;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmLoader;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsDeserializer;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsSerializer;
import com.serhiyboiko.taskmanager.utils.tutorial.Tutorial;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class TaskListActivity
        extends AppCompatActivity
        implements ConfirmationDialog.DialogListener, TabHost.OnTabChangeListener, LoaderManager.LoaderCallbacks<RealmResults<Task>> {


    private static final String TAG = "TaskListActivity";
    private RecyclerView mTaskListView;
    private TabHost mTabHost;
    private ExpandableListView mStatisticsExpandableList;
    private FloatingActionButton mNewTaskButton;

    private RealmResults<Task> mTaskListRealmResults;
    private ArrayList<Integer> mMonthsList = new ArrayList<>();
    private Map<Integer, Map<String, Object>> mMonthsMap = new TreeMap<>();
    private TaskListAdapter mTaskListAdapter;
    private StatisticsAdapter mStatisticsAdapter;
    private SharedPrefsSerializer mSharedPrefsSerializer;
    private RealmIO mRealmIO;
    private int[] mBackgroundColors;
    private boolean mBackPressed;
    private Handler mHandler;
    private Runnable mCancelExit;
    private Runnable mRemoveRefreshActionView;
    private Menu mMenu;
    private MenuItem mRefreshStatisticsItem;
    private View mCurrentTabView;
    private int mCurrentTabId;

    private static final int CREATE_NEW_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;
    private static final int SETTINGS_REQUEST = 3;

    private final static int REALM_LOADER = 1;

    public final static String ITEM_POSITION_EXTRA = "item_position";
    public final static String TASK_ID_EXTRA = "task_id";
    public final static String TITLE_EXTRA = "title";
    public final static String COMMENTARY_EXTRA = "commentary";
    public final static String MAX_DURATION_EXTRA = "max_duration";
    public final static String TASK_FREQUENCY_EXTRA = "task_frequency";
    public final static String AVATAR_PATH_EXTRA = "avatar_path";
    public final static String REQUEST_CODE_EXTRA = "request_code";
    public final static String AVATAR_EDIT_TIME_EXTRA = "avatar_edit_tome";
    public static final String IS_ASSIGNED_TO_LOCATION_EXTRA = "is_assigned_to_location";
    public static final String LATITUDE_EXTRA = "latitude";
    public static final String LONGITUDE_EXTRA = "longitude";

    public static final String TASKS_DURATION_KEY = "tasks_duration";
    public static final String TASKS_ID_LIST_KEY = "tasks_id_list";
    public static final String MONTH_KEY = "month";
    public static final String TASK_TITLE_KEY = "task_title";
    public static final String TASK_TOTAL_DURATION = "total_duration";
    public static final String DATA_LIST_KEY = "task_data_list";
    public static final String TASKS_INFO_LIST = "tasks_info_list";

    public static final Object sDataLock = new Object();




    private SharedPrefsDeserializer mSharedPrefsDeserializer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list_activity);
        bindActivity();
        getSupportActionBar().setTitle(R.string.task_list_activity_title);

        initTabHost();

        mHandler = new Handler();

        mRemoveRefreshActionView = new Runnable() {
            @Override
            public void run() {
                mRefreshStatisticsItem.setActionView(null);
            }
        };

        mCancelExit = new Runnable() {
            @Override
            public void run() {
                mBackPressed = false;
            }
        };

        mSharedPrefsDeserializer = new SharedPrefsDeserializer(this);
        mSharedPrefsSerializer = new SharedPrefsSerializer(this);

        getSupportLoaderManager().initLoader(REALM_LOADER, null, this);

        mBackgroundColors = mSharedPrefsDeserializer.getTaskBackgroundColors();

        mRealmIO = new RealmIO(this);

        mStatisticsAdapter = new StatisticsAdapter(mMonthsList, mMonthsMap, mRealmIO, this);
        mStatisticsExpandableList.setAdapter(mStatisticsAdapter);

        calculateStatistics();

        mNewTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskListActivity.this, EditTaskActivity.class);
                intent.putExtra(REQUEST_CODE_EXTRA, CREATE_NEW_TASK_REQUEST);
                startActivityForResult(intent, CREATE_NEW_TASK_REQUEST);
            }
        });

        if(!TaskManager.isRunning()){
            startService(new Intent(this, TaskManager.class));
        }

    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        BackupManager backupManager = new BackupManager(this);
        backupManager.dataChanged();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mCancelExit);
        mHandler.removeCallbacks(mRemoveRefreshActionView);
        mRealmIO.getRealm().removeAllChangeListeners();
        mRealmIO.close();
        Log.i(TAG, "onDestroy");
    }

    //bind references to widgets
    private void bindActivity() {
        mTaskListView = (RecyclerView) findViewById(R.id.task_list);
        mTabHost = (TabHost) findViewById(R.id.task_list_tab_host);
        mStatisticsExpandableList = (ExpandableListView) findViewById(R.id.statistics_list);
        mNewTaskButton = (FloatingActionButton) findViewById(R.id.new_task_button);
    }

    private void initTabHost() {
        mTabHost.setup();
        TabHost.TabSpec tabSpec;

        CharSequence[] tabTitles = getResources().getTextArray(R.array.task_list_tabs);
        String taskListTabTitle = tabTitles[0].toString();
        String statisticsTabTitle = tabTitles[1].toString();

        tabSpec = mTabHost.newTabSpec(taskListTabTitle);
        tabSpec.setIndicator(taskListTabTitle);
        tabSpec.setContent(R.id.task_list);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(statisticsTabTitle);
        tabSpec.setIndicator(statisticsTabTitle);
        tabSpec.setContent(R.id.statistics_list);
        mTabHost.addTab(tabSpec);

        mTabHost.setCurrentTabByTag(taskListTabTitle);
        mTabHost.setOnTabChangedListener(this);

        mCurrentTabView = mTabHost.getCurrentView();
        mCurrentTabId = mTabHost.getCurrentTab();
    }


    private void calculateStatistics() {
        RealmResults<Task> tasksWithStatistics = mRealmIO.getRealm()
                .where(Task.class)
                .isNotEmpty("mTaskExecInfoList")
                .findAll();
        RealmList<TaskExecInfo> taskExecInfoList;
        //list that contains months that will be displayed in statistics
        ArrayList<Integer> monthsList = new ArrayList<>();
        //map that contains statistics of each month, key = month + year*12
        Map<Integer, Map<String, Object>> monthsMap = new TreeMap<>();
        Map<String, Object> monthMap;
        Map<Integer, Map<String, Object>> tasksMap;
        ArrayList<Integer> taskIdList = new ArrayList<>();
        Map<String, Object> taskData;
        long taskStart;
        long taskDuration;
        GregorianCalendar taskStartCalendar = new GregorianCalendar();
        int month;


        for (Task task : tasksWithStatistics) {
            taskExecInfoList = task.getTaskExecInfoList();
            for (TaskExecInfo taskExecInfo : taskExecInfoList) {
                taskStart = taskExecInfo.getTaskStart();
                taskDuration = taskExecInfo.getDuration();

                taskStartCalendar.setTimeInMillis(taskStart);
                month = taskStartCalendar.get(Calendar.YEAR) * 12 + taskStartCalendar.get(Calendar.MONTH);
                if (monthsMap.containsKey(month)) {
                    monthMap = monthsMap.get(month);

                    long oldTasksDuration = (Long) monthMap.get(TASKS_DURATION_KEY);
                    long newTasksDuration = oldTasksDuration + taskDuration;
                    monthMap.put(TASKS_DURATION_KEY, newTasksDuration);

                    taskIdList = (ArrayList<Integer>) monthMap.get(TASKS_ID_LIST_KEY);
                    tasksMap = (Map<Integer, Map<String, Object>>) monthMap.get(DATA_LIST_KEY);
                    if (!taskIdList.contains(task.getId())) {
                        taskIdList.add(task.getId());

                        taskData = new TreeMap<>();
                        taskData.put(TASK_TITLE_KEY, task.getTitle());
                        taskData.put(TASK_TOTAL_DURATION, taskDuration);
                        tasksMap.put(task.getId(), taskData);
                    } else {
                        taskData = tasksMap.get(task.getId());
                        long oldTaskDuration = (long) taskData.get(TASK_TOTAL_DURATION);
                        long newTaskDuration = oldTaskDuration + taskDuration;
                        taskData.put(TASK_TOTAL_DURATION, newTaskDuration);
                    }

                } else {
                    monthMap = new TreeMap<>();
                    tasksMap = new TreeMap<>();
                    taskIdList = new ArrayList<>();
                    taskData = new TreeMap<>();

                    monthsList.add(month);

                    long newTasksDuration = taskDuration;

                    taskIdList.add(task.getId());

                    taskData.put(TASK_TITLE_KEY, task.getTitle());
                    taskData.put(TASK_TOTAL_DURATION, newTasksDuration);

                    tasksMap.put(task.getId(), taskData);
                    monthMap.put(MONTH_KEY, month);
                    monthMap.put(TASKS_DURATION_KEY, newTasksDuration);
                    monthMap.put(TASKS_ID_LIST_KEY, taskIdList);
                    monthMap.put(DATA_LIST_KEY, tasksMap);

                    monthsMap.put(month, monthMap);
                }

            }
        }

        ArrayList<Map<String, Object>> tasksInfoList;


        for (int m : monthsList) {
            monthMap = monthsMap.get(m);
            tasksMap = (Map<Integer, Map<String, Object>>) monthMap.get(DATA_LIST_KEY);
            tasksInfoList = new ArrayList<>();
            for (int id : taskIdList) {
                tasksInfoList.add(tasksMap.get(id));
            }

            monthMap.put(TASKS_INFO_LIST, tasksInfoList);

            Collections.sort(tasksInfoList, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                    return (int) ( (long) t1.get(TASK_TOTAL_DURATION) - (long)stringObjectMap.get(TASK_TOTAL_DURATION));
                }
            });
        }

        mMonthsList.clear();
        mMonthsMap.clear();
        mMonthsList.addAll(monthsList);
        mMonthsMap.putAll(monthsMap);
        mStatisticsAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        mMenu = menu;
        getMenuInflater().inflate(R.menu.lask_list_menu, menu);
        mRefreshStatisticsItem = mMenu.findItem(R.id.menu_refresh);
        int sortingId = mSharedPrefsDeserializer.getListSorting();
        switch (sortingId) {
            case R.id.menu_sort_az:
            case R.id.menu_sort_za:
            case R.id.menu_sort_new_old:
            case R.id.menu_sort_old_new:
                menu.findItem(sortingId).setChecked(true);
        }
        initTaskListTab();
        Tutorial.taskListActivityTutorial(this, mMenu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i(TAG, "onPrepareOptionsMenu");
        return super.onPrepareOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_new_task:
                Intent intent = new Intent(TaskListActivity.this, EditTaskActivity.class);
                intent.putExtra(REQUEST_CODE_EXTRA, CREATE_NEW_TASK_REQUEST);
                startActivityForResult(intent, CREATE_NEW_TASK_REQUEST);
                return true;
            case R.id.menu_refresh:
                animateRefresh();
                calculateStatistics();
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
                ConfirmationDialog.newInstance(R.string.dialog_delete_all_tasks).show(getSupportFragmentManager(), getString(R.string.dialog_delete_all_tasks));
                return true;
            case R.id.menu_remove_all_forever:
                ConfirmationDialog.newInstance(R.string.dialog_delete_all_tasks_forever).show(getSupportFragmentManager(), getString(R.string.dialog_delete_all_tasks_forever));
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
        int listSize = mTaskListAdapter.getItemCount();
        int contentHeight = 0;
        int lineAmount = 0;
        int defaultTaskDuration = mSharedPrefsDeserializer.getMaxTaskDuration();
        String[] titles = {"Meeting", "Shopping", "Exercises", "Chores", "Work", "Cinema", "TV show", "Some stuff", "More stuff", "Another stuff"};
        String[] commentary = {"Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                " Maecenas rutrum feugiat erat, sed vehicula purus dictum nec.",
                " Etiam blandit pulvinar maximus.", " Aliquam in ex euismod, fringilla ipsum eu, tempus nisi.",
                " Nunc sed ipsum massa. Suspendisse vitae vulputate quam."};

        //check height of existing items
        TaskListAdapter.ViewHolder viewHolder = mTaskListAdapter.onCreateViewHolder(mTaskListView, 0);
        for (int i = 0; i < listSize; i++) {
            mTaskListAdapter.onBindViewHolder(viewHolder, mTaskListRealmResults.get(i));
            View oldItem = viewHolder.itemView;
            oldItem.measure(View.MeasureSpec.makeMeasureSpec(mTaskListView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            contentHeight += oldItem.getMeasuredHeight();
        }
        //generate new items
        while (contentHeight < listViewHeight * 3) {
            String line = "";
            String title = "";
            lineAmount = (int) (Math.random() * 4) + 1;
            title = titles[(int) (Math.random() * titles.length)];
            for (int i = 0; i < lineAmount; i++) {
                line += commentary[i];
            }
            Task task = new Task(this, line, title, defaultTaskDuration, 0, null, 0, 0, 0, false);
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
        final String title;
        final String commentary;
        final int maxDuration;
        final int taskFrequency;
        final String avatarPath;
        final long avatarEditTime;
        final double latitude;
        final double longitude;
        final boolean isAssignedToLocation;

        int position;
        //check result code
        if (resultCode == RESULT_OK) {
            //check request code
            switch (requestCode) {
                case CREATE_NEW_TASK_REQUEST:
                    if (data == null) {
                        return;
                    }
                    //extract task data
                    title = data.getStringExtra(TITLE_EXTRA);
                    commentary = data.getStringExtra(COMMENTARY_EXTRA);
                    maxDuration = data.getIntExtra(MAX_DURATION_EXTRA, 0);
                    taskFrequency = data.getIntExtra(TASK_FREQUENCY_EXTRA, 0);
                    avatarPath = data.getStringExtra(AVATAR_PATH_EXTRA);
                    avatarEditTime = data.getLongExtra(AVATAR_EDIT_TIME_EXTRA, 0);
                    latitude = data.getDoubleExtra(LATITUDE_EXTRA, 0);
                    longitude = data.getDoubleExtra(LONGITUDE_EXTRA, 0);
                    isAssignedToLocation = data.getBooleanExtra(IS_ASSIGNED_TO_LOCATION_EXTRA, false);

                    //add task to list
                    Task task = new Task(this, commentary, title, maxDuration, taskFrequency,
                            avatarPath, avatarEditTime, latitude, longitude, isAssignedToLocation);
                    mRealmIO.putTask(task);
                    break;
                case EDIT_TASK_REQUEST:
                    final Task selectedItem;
                    if (data == null) {
                        return;
                    }
                    //extract task data
                    title = data.getStringExtra(TITLE_EXTRA);
                    commentary = data.getStringExtra(COMMENTARY_EXTRA);
                    maxDuration = data.getIntExtra(MAX_DURATION_EXTRA, 0);
                    taskFrequency = data.getIntExtra(TASK_FREQUENCY_EXTRA, 0);
                    position = data.getIntExtra(ITEM_POSITION_EXTRA, 0);
                    avatarPath = data.getStringExtra(AVATAR_PATH_EXTRA);
                    avatarEditTime = data.getLongExtra(AVATAR_EDIT_TIME_EXTRA, 0);
                    latitude = data.getDoubleExtra(LATITUDE_EXTRA, 0);
                    longitude = data.getDoubleExtra(LONGITUDE_EXTRA, 0);
                    isAssignedToLocation = data.getBooleanExtra(IS_ASSIGNED_TO_LOCATION_EXTRA, false);

                    selectedItem = mTaskListRealmResults.get(position);

                    //get list item and change it fields
                    mRealmIO.getRealm().beginTransaction();
                    selectedItem.setPeriod(taskFrequency);
                    selectedItem.setTaskMaxDuration(maxDuration);
                    selectedItem.setTitle(title);
                    selectedItem.setCommentary(commentary);
                    selectedItem.setAvatarLocation(avatarPath);
                    selectedItem.setLastAvatarEditTime(avatarEditTime);
                    selectedItem.setLatitude(latitude);
                    selectedItem.setLongitude(longitude);
                    selectedItem.setAssignedToLocation(isAssignedToLocation);
                    mRealmIO.getRealm().commitTransaction();

                    break;
                case SETTINGS_REQUEST:
                    mBackgroundColors = mSharedPrefsDeserializer.getTaskBackgroundColors();

                    mTaskListAdapter.setBackgroundColors(mBackgroundColors);
                    mTaskListAdapter.notifyDataSetChanged();
                    break;
            }
        }
        Tutorial.taskListActivityTutorial(this, mMenu);
    }


    /*
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(SAVED_TASK_LIST, mTaskListArray);
    }
    */

    @Override
    public void onBackPressed() {

        if (mBackPressed) {
            super.onBackPressed();
        } else {
            mBackPressed = true;
            mHandler.postDelayed(mCancelExit, 2000);
            Snackbar.make(findViewById(R.id.task_list_activity_container),
                    getString(R.string.press_back_again_to_exit), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPositive(int titleId) {
        switch (titleId) {
            case R.string.dialog_delete_all_tasks:
                mRealmIO.hideAllTasks();
                mTaskListAdapter.notifyDataSetChanged();
                break;
            case R.string.dialog_delete_all_tasks_forever:
                mRealmIO.deleteAllHiddenTasks();
                calculateStatistics();
                break;
        }
    }

    @Override
    public void onTabChanged(String s) {
        int newTabId = mTabHost.getCurrentTab();
        View newTabView = mTabHost.getCurrentView();


        switch (s) {
            case "Task list":
                initTaskListTab();
                break;
            case "Statistics":
                initStatisticsTab();
                break;
        }

        if (newTabId > mCurrentTabId){
            Animation inFromRight = AnimationUtils.loadAnimation(this, R.anim.tab_in_from_right);
            Animation outToLeft = AnimationUtils.loadAnimation(this, R.anim.tab_out_to_left);

            newTabView.setAnimation(inFromRight);
            mCurrentTabView.setAnimation(outToLeft);
        } else {
            Animation inFromLeft = AnimationUtils.loadAnimation(this, R.anim.tab_in_from_left);
            Animation outToRight = AnimationUtils.loadAnimation(this, R.anim.tab_out_to_right);

            newTabView.setAnimation(inFromLeft);
            mCurrentTabView.setAnimation(outToRight);
        }

        mCurrentTabId = mTabHost.getCurrentTab();
        mCurrentTabView = mTabHost.getCurrentView();
    }

    private void initTaskListTab(){
        mNewTaskButton.setVisibility(View.VISIBLE);
        hideStatisticsOptions();
        showListOptions();
    }

    private void initStatisticsTab(){
        mNewTaskButton.setVisibility(View.GONE);
        showStatisticsOptions();
        hideListOptions();
    }

    private void hideListOptions() {
        mMenu.getItem(0).setVisible(false);
        mMenu.getItem(2).setVisible(false);
        mMenu.getItem(3).setVisible(false);
        mMenu.getItem(4).setVisible(false);
        mMenu.getItem(6).setVisible(false);
    }

    private void showStatisticsOptions() {
        mMenu.getItem(1).setVisible(true);
        mMenu.getItem(5).setVisible(true);

    }

    private void showListOptions() {
        mMenu.getItem(0).setVisible(true);
        mMenu.getItem(2).setVisible(true);
        mMenu.getItem(3).setVisible(true);
        mMenu.getItem(4).setVisible(true);
        mMenu.getItem(6).setVisible(true);
    }


    private void hideStatisticsOptions() {
        mMenu.getItem(1).setVisible(false);
        mMenu.getItem(5).setVisible(false);
    }

    public void animateRefresh() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_statistics_action_view, null);

        mRefreshStatisticsItem.setActionView(iv);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh_icon_spinning);
        iv.startAnimation(rotation);

        mHandler.postDelayed(mRemoveRefreshActionView, 1500);
    }

    @Override
    public Loader<RealmResults<Task>> onCreateLoader(int id, Bundle args) {
        switch (id){
            case REALM_LOADER:
                return new RealmLoader(this);

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<RealmResults<Task>> loader, RealmResults<Task> data) {
        mTaskListRealmResults = data;

        int currentSortingId = mSharedPrefsDeserializer.getListSorting();
        switch (currentSortingId) {
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

        if (mTaskListAdapter == null){
            mTaskListAdapter = new TaskListAdapter(this, mTaskListRealmResults, mBackgroundColors, mRealmIO);
            mTaskListView.setLayoutManager(new LinearLayoutManager(this));
            mTaskListView.setAdapter(mTaskListAdapter);
        } else {
            mTaskListAdapter.setTaskRealmResults(mTaskListRealmResults);
        }

        mTaskListRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Task>>() {
            @Override
            public void onChange(RealmResults<Task> element) {
                mTaskListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<RealmResults<Task>> loader) {

    }
}


