package com.serhiyboiko.taskmanager.utils.alarm_manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import io.realm.Realm;

/**
 * Created by Amegar on 22.06.2016.
 */
public class TaskAutoFinishManager {

    private static final int UNREGISTERED = 0;
    private Context mContext;
    private AlarmManager mAlarmManager;

    private final static String MAXIMUM_TASK_DURATION = "maximum_task_duration";
    private static final String LAST_ALERT_REQUEST_CODE = "last_alert_request_code";

    private final static String TITLE_EXTRA = "title";
    private static final String REQUEST_CODE_EXTRA = "alert_request_code";

    public TaskAutoFinishManager(Context context){
        mContext = context;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public void updateTaskAutoFinishTime (ArrayList<Task> tasks){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int taskMaxDuration = sp.getInt(MAXIMUM_TASK_DURATION, 0);
        if (taskMaxDuration != 0){
            for(Task task:tasks){
                int alertRequestCode = task.getAlertRequestCode();
                if(alertRequestCode != 0){
                    GregorianCalendar taskRestartTime = task.getTaskRestart();
                    GregorianCalendar taskStartTime = task.getTaskStart();
                    if (taskRestartTime != null || taskStartTime != null){
                        Intent notificationIntent = new Intent("com.serhiyboiko.taskmanager.DISPLAY_NOTIFICATION");
                        notificationIntent.addCategory("android.intent.category.DEFAULT");
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
            }
        } else {
            for(Task task:tasks){
                int alertRequestCode = task.getAlertRequestCode();
                if(alertRequestCode != 0){
                    Intent notificationIntent = new Intent("com.serhiyboiko.taskmanager.DISPLAY_NOTIFICATION");
                    notificationIntent.addCategory("android.intent.category.DEFAULT");
                    notificationIntent.putExtra(TITLE_EXTRA, task.getTitle());
                    notificationIntent.putExtra(REQUEST_CODE_EXTRA, alertRequestCode);

                    PendingIntent broadcast = PendingIntent.getBroadcast(mContext, alertRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mAlarmManager.cancel(broadcast);
                    Realm.getDefaultInstance().beginTransaction();
                    task.setAlertRequestCode(0);
                    Realm.getDefaultInstance().commitTransaction();
                }
            }
        }

    }

    public void registerTaskForAutoFinish (Task task){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int taskMaxDuration = sp.getInt(MAXIMUM_TASK_DURATION, 0);
        int lastAlertRequestCode = sp.getInt(LAST_ALERT_REQUEST_CODE, 0);

        if (taskMaxDuration != 0){
            GregorianCalendar taskRestartTime = task.getTaskRestart();
            GregorianCalendar taskStartTime = task.getTaskStart();
            if (taskRestartTime != null || taskStartTime != null){
                Intent notificationIntent = new Intent("com.serhiyboiko.taskmanager.DISPLAY_NOTIFICATION");
                notificationIntent.addCategory("android.intent.category.DEFAULT");
                notificationIntent.putExtra(TITLE_EXTRA, task.getTitle());

                int alertRequestCode = ++lastAlertRequestCode;
                if (alertRequestCode == UNREGISTERED){
                    alertRequestCode += 1;
                }
                notificationIntent.putExtra(REQUEST_CODE_EXTRA, alertRequestCode);

                PendingIntent broadcast = PendingIntent.getBroadcast(mContext, alertRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if(taskRestartTime != null){
                    long finishTime = taskRestartTime.getTimeInMillis() + taskMaxDuration*1000;
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, finishTime, broadcast);
                } else {
                    long finishTime = taskStartTime.getTimeInMillis() + taskMaxDuration*1000;
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, finishTime, broadcast);
                }
                Realm.getDefaultInstance().beginTransaction();
                task.setAlertRequestCode(alertRequestCode);
                Realm.getDefaultInstance().commitTransaction();

                RealmIO rio = new RealmIO(mContext);
                rio.putTask(task);

                sp.edit().putInt(LAST_ALERT_REQUEST_CODE, alertRequestCode).commit();
            }
        }
    }

    public void unregisterTaskAutoFinish(Task task) {
        int alertRequestCode = task.getAlertRequestCode();
        if(alertRequestCode != 0){
            Intent notificationIntent = new Intent("com.serhiyboiko.taskmanager.DISPLAY_NOTIFICATION");
            notificationIntent.addCategory("android.intent.category.DEFAULT");
            notificationIntent.putExtra(TITLE_EXTRA, task.getTitle());
            notificationIntent.putExtra(REQUEST_CODE_EXTRA, alertRequestCode);

            PendingIntent broadcast = PendingIntent.getBroadcast(mContext, alertRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mAlarmManager.cancel(broadcast);
            Realm.getDefaultInstance().beginTransaction();
            task.setAlertRequestCode(0);
            Realm.getDefaultInstance().commitTransaction();
        }
    }

}
