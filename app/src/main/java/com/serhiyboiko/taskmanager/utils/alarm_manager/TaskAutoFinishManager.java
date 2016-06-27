package com.serhiyboiko.taskmanager.utils.alarm_manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.serhiyboiko.taskmanager.model.Task;

import java.util.GregorianCalendar;

import io.realm.RealmResults;

public class TaskAutoFinishManager {

    public static final String DISPLAY_NOTIFICATION = "com.serhiyboiko.taskmanager.DISPLAY_NOTIFICATION";
    private static final String DEFAULT_CATEGORY = "android.intent.category.DEFAULT";
    private static final String TAG = "TaskAutoFinishManager";
    private Context mContext;
    private AlarmManager mAlarmManager;

    private final static String MAXIMUM_TASK_DURATION = "maximum_task_duration";

    private final static String TITLE_EXTRA = "title";
    private static final String REQUEST_CODE_EXTRA = "alert_request_code";

    public TaskAutoFinishManager(Context context){
        mContext = context;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public void updateTaskAutoFinishTime (RealmResults<Task> tasks){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int taskMaxDuration = sp.getInt(MAXIMUM_TASK_DURATION, 0);
        if (taskMaxDuration != 0){
            for(Task task:tasks){
                int alertRequestCode = task.getId();
                GregorianCalendar taskRestartTime = task.getTaskRestart();
                GregorianCalendar taskStartTime = task.getTaskStart();
                GregorianCalendar taskEndTime = task.getTaskEnd();
                if ((taskRestartTime != null || taskStartTime != null)&&(taskEndTime == null)){
                    Log.i(TAG, "new intent in updater");
                    Intent notificationIntent = new Intent(DISPLAY_NOTIFICATION);
                    notificationIntent.addCategory(DEFAULT_CATEGORY);
                    notificationIntent.putExtra(TITLE_EXTRA, task.getTitle());
                    notificationIntent.putExtra(REQUEST_CODE_EXTRA, alertRequestCode);

                    PendingIntent broadcast = PendingIntent.getBroadcast(mContext, alertRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    long finishTime;
                    if(taskRestartTime != null){
                        finishTime = taskRestartTime.getTimeInMillis() + taskMaxDuration*1000;
                    } else {
                        finishTime = taskStartTime.getTimeInMillis() + taskMaxDuration*1000;
                    }
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, finishTime, broadcast);
                }

            }
        } else {
            for(Task task:tasks){
                unregisterTaskAutoFinish(task);
            }
        }

    }

    public void registerTaskForAutoFinish (Task task){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int taskMaxDuration = sp.getInt(MAXIMUM_TASK_DURATION, 0);
        if (taskMaxDuration != 0){
            int alertRequestCode = task.getId();
            GregorianCalendar taskRestartTime = task.getTaskRestart();
            GregorianCalendar taskStartTime = task.getTaskStart();
            if (taskRestartTime != null || taskStartTime != null){
                Intent notificationIntent = new Intent(DISPLAY_NOTIFICATION);
                notificationIntent.addCategory(DEFAULT_CATEGORY);
                notificationIntent.putExtra(TITLE_EXTRA, task.getTitle());
                notificationIntent.putExtra(REQUEST_CODE_EXTRA, alertRequestCode);

                PendingIntent broadcast = PendingIntent.getBroadcast(mContext, alertRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if(taskRestartTime != null){
                    long finishTime = taskRestartTime.getTimeInMillis() + taskMaxDuration*1000;
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, finishTime, broadcast);
                } else {
                    long finishTime = taskStartTime.getTimeInMillis() + taskMaxDuration*1000;
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, finishTime, broadcast);
                }
            }
        }
    }

    public void unregisterTaskAutoFinish(Task task) {
        int alertRequestCode = task.getId();
        GregorianCalendar taskRestartTime = task.getTaskRestart();
        GregorianCalendar taskStartTime = task.getTaskStart();
        GregorianCalendar taskEndTime = task.getTaskEnd();
        if ((taskRestartTime != null || taskStartTime != null)&&(taskEndTime == null)){
            Intent notificationIntent = new Intent(DISPLAY_NOTIFICATION);
            notificationIntent.addCategory(DEFAULT_CATEGORY);
            notificationIntent.putExtra(TITLE_EXTRA, task.getTitle());
            notificationIntent.putExtra(REQUEST_CODE_EXTRA, alertRequestCode);

            PendingIntent broadcast = PendingIntent.getBroadcast(mContext, alertRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mAlarmManager.cancel(broadcast);
        }
    }

}
