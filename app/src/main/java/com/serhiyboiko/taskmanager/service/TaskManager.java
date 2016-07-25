package com.serhiyboiko.taskmanager.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationServices;
import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.activity.TaskListActivity;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.model.TaskExecInfo;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;

import java.util.GregorianCalendar;

import io.realm.RealmResults;

/**
 * Created on 12.07.2016.
 */
public class TaskManager extends Service implements GoogleApiClient.ConnectionCallbacks {

    private HandlerThread mServiceThread;
    private Runnable mRunnable;
    private Handler mHandler;
    private static boolean sIsRunning;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsLocationAvailable;
    private boolean mIsConnectedToGoogleApi;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mRunnable = new Runnable() {
            RealmIO realmIO;
            RealmResults<Task> autoFinishTasks;
            RealmResults<Task> autoRestartTasks;
            RealmResults<Task> autoStartByLocationtTasks;

            @Override
            public void run() {
                if (sIsRunning) {
                    mHandler.postDelayed(mRunnable, 1000);
                }
                if (realmIO == null) {
                    realmIO = new RealmIO(TaskManager.this);
                }
                realmIO.getRealm().beginTransaction();


                autoFinishTasks = realmIO.getAllAutoFinishTasks();
                autoRestartTasks = realmIO.getAllAutoRestartTasks();
                autoStartByLocationtTasks = realmIO.getAllTasksAssignedToLocation();


                int taskElapsedTime;
                int taskMaxDuration;
                long taskStart;
                int totalPauseDuration;
                int taskPeriod;
                GregorianCalendar taskEndCalendar;
                GregorianCalendar taskStartCalendar;

                for (Task task : autoFinishTasks) {
                    taskMaxDuration = task.getTaskMaxDuration() * 1000;
                    totalPauseDuration = task.getTotalPauseDuration();
                    if (task.getTaskRestart() == null) {
                        if (task.getTaskStart() == null) {
                            continue;
                        }
                        taskStart = task.getTaskStart().getTimeInMillis();
                    } else {
                        taskStart = task.getTaskRestart().getTimeInMillis();
                    }
                    taskElapsedTime = (int) (System.currentTimeMillis() - taskStart - totalPauseDuration);
                    if (taskMaxDuration < taskElapsedTime) {
                        taskEndCalendar = new GregorianCalendar();
                        taskEndCalendar.setTimeInMillis(taskStart + taskMaxDuration + totalPauseDuration);
                        task.setTaskEnd(taskEndCalendar);
                        TaskExecInfo.createTaskExecInfo(realmIO, task);
                        showNotification(task, R.string.finished, R.string.task_finished_max_duration_exceeded_message);
                    }
                }

                for (Task task : autoRestartTasks) {
                    taskPeriod = task.getPeriodInMills();
                    if (task.getTaskStart() == null) {
                        continue;
                    }
                    taskStart = task.getTaskStart().getTimeInMillis();

                    taskElapsedTime = (int) (System.currentTimeMillis() - taskStart);

                    if (taskPeriod < taskElapsedTime) {
                        if (task.getTaskEnd() == null) {
                            taskEndCalendar = new GregorianCalendar();
                            taskEndCalendar.setTimeInMillis(taskStart + taskPeriod);
                            task.setTaskEnd(taskEndCalendar);
                            TaskExecInfo.createTaskExecInfo(realmIO, task);
                        }


                        taskStartCalendar = new GregorianCalendar();
                        taskStartCalendar.setTimeInMillis(taskStart + taskPeriod);
                        task.setTaskStart(taskStartCalendar);
                        task.setTaskEnd(null);
                        task.setTaskRestart(null);
                        task.setTimeSpend(0);
                        task.getPauseInfoList().deleteAllFromRealm();


                    }

                }

                if (mIsConnectedToGoogleApi){
                    try{
                        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
                        if (locationAvailability.isLocationAvailable()) {
                            Location currentLocation;
                            Location taskExecutionLocation  = new Location("");
                            float distanceInMeters;

                            for (Task task : autoStartByLocationtTasks) {
                                taskExecutionLocation.setLongitude(task.getLongitude());
                                taskExecutionLocation.setLatitude(task.getLatitude());

                                currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                                distanceInMeters = currentLocation.distanceTo(taskExecutionLocation);
                                Log.i("TaskService", "distance " + distanceInMeters);

                                if (task.getTaskStart() == null && distanceInMeters < 100){
                                    task.setTaskStart(new GregorianCalendar());
                                    showNotification(task, R.string.started, R.string.task_started_location_entered_message);
                                }

                                if (task.getTaskStart() != null && task.getTaskEnd() == null && distanceInMeters > 110){
                                    task.setTaskEnd(new GregorianCalendar());
                                    TaskExecInfo.createTaskExecInfo(realmIO, task);
                                    showNotification(task, R.string.finished, R.string.task_finished_location_left_message);
                                }

                            }
                        }
                    } catch (SecurityException e){
                        e.printStackTrace();
                    }
                }
                realmIO.getRealm().commitTransaction();
            }
        };

        mServiceThread = new HandlerThread("TaskService", Process.THREAD_PRIORITY_BACKGROUND);
        mServiceThread.start();

        sIsRunning = true;

        mHandler = new Handler(mServiceThread.getLooper());


        mHandler.post(mRunnable);
    }


    private void showNotification(Task task, int titleid, int messageId) {
        Intent notificationIntent = new Intent(this, TaskListActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TaskListActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        Notification notification = builder.setContentTitle(task.getTitle() + getString(titleid))
                .setContentText(getString(messageId))
                .setTicker(task.getTitle() + getString(titleid))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TaskManagerService", "onStartCommand");
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("TaskManagerService", "onDestroy");
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("TaskManagerService", "onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isRunning() {
        return sIsRunning;
    }

    public static void setIsRunning(boolean isRunning) {
        sIsRunning = isRunning;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mIsConnectedToGoogleApi = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        mIsConnectedToGoogleApi = false;
    }
}
