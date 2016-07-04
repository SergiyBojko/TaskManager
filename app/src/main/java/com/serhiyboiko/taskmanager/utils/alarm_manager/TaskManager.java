package com.serhiyboiko.taskmanager.utils.alarm_manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.serhiyboiko.taskmanager.model.Task;

import java.util.GregorianCalendar;

public class TaskManager {

    private static final String AUTOFINISH_TASK_ACTION = "com.serhiyboiko.taskmanager.AUTOFINISH_TASK";
    private static final String REPEAT_TASK_ACTION = "com.serhiyboiko.taskmanager.REPEAT_TASK";
    private static final String DEFAULT_CATEGORY = "android.intent.category.DEFAULT";
    private static final String TAG = "TaskManager";
    private Context mContext;
    private AlarmManager mAlarmManager;


    public final static String TITLE_EXTRA = "title";
    public static final String TASK_ID_EXTRA = "task_id";

    public TaskManager(Context context){
        mContext = context;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public void registerTask(Task task){
        int taskMaxDuration = task.getTaskMaxDuration();
        int taskPeriod = task.getPeriodInMills();
        if (taskMaxDuration != 0){
            int taskId = task.getId();
            int alertRequestCode = taskId;
            GregorianCalendar taskRestartTime = task.getTaskRestart();
            GregorianCalendar taskStartTime = task.getTaskStart();
            GregorianCalendar taskEndTime = task.getTaskEnd();
            if (taskStartTime != null && taskEndTime == null){
                Intent notificationIntent = new Intent(AUTOFINISH_TASK_ACTION);
                notificationIntent.addCategory(DEFAULT_CATEGORY);
                notificationIntent.putExtra(TITLE_EXTRA, task.getTitle());
                notificationIntent.putExtra(TASK_ID_EXTRA, taskId);

                PendingIntent broadcast = PendingIntent.getBroadcast(mContext, alertRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                int totalPauseDuration = task.getTotalPauseDuration();

                long finishTime;
                if(taskRestartTime != null){
                    finishTime = taskRestartTime.getTimeInMillis() + taskMaxDuration*1000;
                } else {
                    finishTime = taskStartTime.getTimeInMillis() + taskMaxDuration*1000;
                }
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, finishTime + totalPauseDuration, broadcast);
            }
        }

        if (taskPeriod != 0){
            int taskId = task.getId();
            int alertRequestCode = taskId;
            GregorianCalendar taskStartTime = task.getTaskStart();
            if (taskStartTime != null){
                Intent notificationIntent = new Intent(REPEAT_TASK_ACTION);
                notificationIntent.addCategory(DEFAULT_CATEGORY);
                notificationIntent.putExtra(TITLE_EXTRA, task.getTitle());
                notificationIntent.putExtra(TASK_ID_EXTRA, taskId);

                PendingIntent broadcast = PendingIntent.getBroadcast(mContext, alertRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                long restartTime = taskStartTime.getTimeInMillis() + taskPeriod;
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, restartTime, broadcast);
            }
        }
    }

    public void unregisterTaskAutoRepeat (Task task) {
        int taskId = task.getId();
        int alertRequestCode = taskId;
        Intent notificationIntent = new Intent(REPEAT_TASK_ACTION);
        notificationIntent.addCategory(DEFAULT_CATEGORY);
        notificationIntent.putExtra(TITLE_EXTRA, task.getTitle());
        notificationIntent.putExtra(TASK_ID_EXTRA, taskId);

        PendingIntent broadcast = PendingIntent.getBroadcast(mContext, alertRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.cancel(broadcast);

    }

    public void unregisterTaskAutoFinish(Task task) {
        int alertRequestCode = task.getId();
        GregorianCalendar taskStartTime = task.getTaskStart();
        GregorianCalendar taskEndTime = task.getTaskEnd();
        if (taskStartTime != null && taskEndTime == null){
            Intent notificationIntent = new Intent(AUTOFINISH_TASK_ACTION);
            notificationIntent.addCategory(DEFAULT_CATEGORY);
            notificationIntent.putExtra(TITLE_EXTRA, task.getTitle());
            notificationIntent.putExtra(TASK_ID_EXTRA, alertRequestCode);

            PendingIntent broadcast = PendingIntent.getBroadcast(mContext, alertRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mAlarmManager.cancel(broadcast);
        }
    }

}
