package com.serhiyboiko.taskmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.activity.TaskListActivity;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created on 04.07.2016.
 */
public class StatisticsAdapter extends BaseExpandableListAdapter{

    private ArrayList<Integer> mMonthsList;
    private Map<Integer, Map<String, Object>> mMonthsMap;
    private Context mContext;
    private RealmIO mRealmIO;

    public StatisticsAdapter(ArrayList<Integer> monthsList, Map<Integer, Map<String, Object>> monthsMap, RealmIO realmIO, Context context) {
        mMonthsList = monthsList;
        mMonthsMap = monthsMap;
        mContext = context;
        mRealmIO = realmIO;
    }

    @Override
    public int getGroupCount() {
        return mMonthsList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        int month = mMonthsList.get(i);
        Map<String, Object> monthMap = mMonthsMap.get(month);
        ArrayList<Integer> taskIdList = (ArrayList<Integer>)monthMap.get(TaskListActivity.TASKS_ID_LIST_KEY);
        return taskIdList.size();
    }

    @Override
    public Object getGroup(int i) {
        int month = mMonthsList.get(i);
        Map<String, Object> monthMap = mMonthsMap.get(month);
        return monthMap;
    }

    @Override
    public Object getChild(int i, int i1) {
        int month = mMonthsList.get(i);
        Map<String, Object> monthMap = mMonthsMap.get(month);
        ArrayList<Integer> taskIdList = (ArrayList<Integer>)monthMap.get(TaskListActivity.TASKS_ID_LIST_KEY);
        int taskId = taskIdList.get(i1);
        return taskId;
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        GroupViewHolder groupViewHolder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.statistics_group, viewGroup, false);
            groupViewHolder = new GroupViewHolder(view);
            view.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) view.getTag();
        }

        int month = mMonthsList.get(i);
        Map<String, Object> monthMap = mMonthsMap.get(month);
        long tasksTotalDuration = (long)monthMap.get(TaskListActivity.TASKS_DURATION_KEY);
        int totalHours = (int) TimeUnit.MILLISECONDS.toHours(tasksTotalDuration);
        int totalMinutes = (int)(TimeUnit.MILLISECONDS.toMinutes(tasksTotalDuration) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(tasksTotalDuration)));
        int totalSeconds = (int) (TimeUnit.MILLISECONDS.toSeconds(tasksTotalDuration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(tasksTotalDuration)));

        int year = month/12;
        int monthOfYear = month%12;

        groupViewHolder.date.setText(String.format("%02d.%d", monthOfYear, year));
        groupViewHolder.totalDuration.setText(String.format("%02d:%02d:%02d", totalHours, totalMinutes, totalSeconds));
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        ChildViewHolder childViewHolder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.statistics_child, viewGroup, false);
            childViewHolder = new ChildViewHolder(view);
            view.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) view.getTag();
        }

        int month = mMonthsList.get(i);
        Map<String, Object> monthMap = mMonthsMap.get(month);
        long tasksTotalDuration = (long)monthMap.get(TaskListActivity.TASKS_DURATION_KEY);
        ArrayList<Map<String,Object>> taskDataList = (ArrayList<Map<String,Object>>) monthMap.get(TaskListActivity.TASKS_INFO_LIST);
        Map<String,Object> taskData = taskDataList.get(i1);
        long taskTotalDuration = (long) taskData.get(TaskListActivity.TASK_TOTAL_DURATION);
        String title = (String) taskData.get(TaskListActivity.TASK_TITLE_KEY);
        int totalHours = (int) TimeUnit.MILLISECONDS.toHours(taskTotalDuration);
        int totalMinutes = (int)(TimeUnit.MILLISECONDS.toMinutes(taskTotalDuration) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(taskTotalDuration)));
        int totalSeconds = (int) (TimeUnit.MILLISECONDS.toSeconds(taskTotalDuration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(taskTotalDuration)));

        int progress = (int)((float)taskTotalDuration/tasksTotalDuration*1000);


        childViewHolder.taskTitle.setText(title);
        childViewHolder.taskTotalDuration.setText(String.format("%02d:%02d:%02d", totalHours, totalMinutes, totalSeconds));
        childViewHolder.taskRelativeProgress.setMax(1000);
        childViewHolder.taskRelativeProgress.setProgress(progress);
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    private class ChildViewHolder {
        TextView taskTitle;
        TextView taskTotalDuration;
        ProgressBar taskRelativeProgress;

        public ChildViewHolder(View v){
            taskTitle = (TextView) v.findViewById(R.id.statistics_child_name);
            taskTotalDuration = (TextView) v.findViewById(R.id.statistics_child_duration);
            taskRelativeProgress = (ProgressBar)v.findViewById(R.id.child_progress);
        }

    }

    private class GroupViewHolder {
        TextView date;
        TextView totalDuration;

        public GroupViewHolder(View v) {
            date = (TextView) v.findViewById(R.id.statistics_group_text);
            totalDuration = (TextView) v.findViewById(R.id.statistics_group_duration);
        }
    }
}
