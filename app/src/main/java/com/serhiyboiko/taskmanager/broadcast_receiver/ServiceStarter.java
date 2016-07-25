package com.serhiyboiko.taskmanager.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.serhiyboiko.taskmanager.service.TaskManager;

/**
 * Created on 13.07.2016.
 */

//Starts TaskManager service when phone is booted
public class ServiceStarter extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TaskManager.isRunning()){
            context.startService(new Intent(context, TaskManager.class));
        }
    }
}
