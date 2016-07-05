package com.serhiyboiko.taskmanager.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.alarm_manager.TaskManager;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;

import java.util.GregorianCalendar;

/**
 * Created on 02.07.2016.
 */
public class TaskAutoRepeatAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int taskId = intent.getIntExtra(TaskManager.TASK_ID_EXTRA, 0);

        RealmIO realmIO = new RealmIO(context);
        realmIO.getRealm().beginTransaction();
        Task taskToRestart = realmIO.findTaskById(taskId);
        taskToRestart.setTaskStart(new GregorianCalendar());
        taskToRestart.setTaskRestart(null);
        taskToRestart.setTaskEnd(null);
        taskToRestart.setTimeSpend(0);
        realmIO.getRealm().commitTransaction();
        new TaskManager(context).registerTask(taskToRestart);
        //Toast.makeText(context, "autorestart", Toast.LENGTH_SHORT).show();
    }
}
