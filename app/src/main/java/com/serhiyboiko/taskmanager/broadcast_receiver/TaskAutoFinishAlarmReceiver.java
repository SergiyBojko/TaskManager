package com.serhiyboiko.taskmanager.broadcast_receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.activity.TaskListActivity;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.model.TaskExecInfo;
import com.serhiyboiko.taskmanager.utils.alarm_manager.TaskManager;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;

import java.util.GregorianCalendar;

public class TaskAutoFinishAlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, TaskListActivity.class);

        int taskId = intent.getIntExtra(TaskManager.TASK_ID_EXTRA, 0);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(TaskListActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        Notification notification = builder.setContentTitle("Task Manager")
                .setContentText(intent.getStringExtra(TaskManager.TITLE_EXTRA) + " finished due to maximum task duration")
                .setTicker("Task finished due to max duration time")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

        RealmIO realmIO = new RealmIO(context);
        realmIO.getRealm().beginTransaction();
        Task finishedTask = realmIO.findTaskById(taskId);
        finishedTask.setTaskEnd(new GregorianCalendar());
        TaskExecInfo taskExecInfo;
        taskExecInfo = TaskExecInfo.createTaskExecInfo(realmIO, finishedTask);
        realmIO.getRealm().commitTransaction();
        //Toast.makeText(context, "autofinish", Toast.LENGTH_SHORT).show();
    }
}
