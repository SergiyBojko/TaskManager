package com.serhiyboiko.taskmanager.broadcast_receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.activity.TaskListActivity;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;

import java.util.GregorianCalendar;

import io.realm.Realm;

/**
 * Created by Amegar on 23.06.2016.
 */
public class TaskAutoFinishAlarmReceiver extends BroadcastReceiver {

    private static final int UNREGISTERED = 0;

    final static String TITLE_EXTRA = "title";
    private static final String REQUEST_CODE_EXTRA = "alert_request_code";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, TaskListActivity.class);

        int alertRequestCode = intent.getIntExtra(REQUEST_CODE_EXTRA, 0);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(TaskListActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        Notification notification = builder.setContentTitle("Task Manager")
                .setContentText(intent.getStringExtra(TITLE_EXTRA) + " finished due to maximum task duration")
                .setTicker("Task finished due to max duration time")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(alertRequestCode, notification);

        RealmIO realmIO = new RealmIO(context);
        Task finishedTask = realmIO.findTaskByRequestCode(alertRequestCode);
        Realm.getDefaultInstance().beginTransaction();
        finishedTask.setTaskEnd(new GregorianCalendar());
        finishedTask.setAlertRequestCode(UNREGISTERED);
        Realm.getDefaultInstance().commitTransaction();

        realmIO.putTask(finishedTask);

        Log.i("BroadcastReceiver", "notification send");

        Intent updateDataIntent = new Intent("com.serhiyboiko.taskmanager.UPDATE_DATA");
        updateDataIntent.addCategory("android.intent.category.DEFAULT");
        updateDataIntent.putExtra("primary_key", finishedTask.getId());

        context.sendBroadcast(updateDataIntent);
    }
}
