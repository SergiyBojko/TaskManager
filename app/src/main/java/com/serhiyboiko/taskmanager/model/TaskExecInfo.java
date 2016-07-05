package com.serhiyboiko.taskmanager.model;

import android.support.annotation.NonNull;

import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;

import io.realm.RealmObject;

/**
 * Created on 02.07.2016.
 */
public class TaskExecInfo extends RealmObject {
    private long mTaskStart;
    private long mTaskEnd;
    private long mDuration;

    public TaskExecInfo (){}

    public TaskExecInfo(long taskStart, long taskEnd, long duration) {
        mTaskStart = taskStart;
        mTaskEnd = taskEnd;
        mDuration = duration;
    }

    @NonNull
    public static TaskExecInfo createTaskExecInfo(RealmIO realmIO, Task finishedTask) {
        TaskExecInfo taskExecInfo;
        if (finishedTask.getTaskRestart() == null){
            //if task finished for first time create new statistics item
            taskExecInfo = new TaskExecInfo(finishedTask.getTaskStart().getTimeInMillis(), finishedTask.getTaskEnd().getTimeInMillis(), finishedTask.getTimeSpend());
            TaskExecInfo managedTaskExecInfo = realmIO.putTaskExecInfo(taskExecInfo);
            finishedTask.getTaskExecInfoList().add(managedTaskExecInfo);
        } else {
            //else update existing
            taskExecInfo = realmIO.getRealm().where(TaskExecInfo.class).equalTo("mTaskStart", finishedTask.getTaskStart().getTimeInMillis()).findFirst();
            taskExecInfo.setTaskEnd(finishedTask.getTaskEnd().getTimeInMillis());
            taskExecInfo.setDuration(finishedTask.getTimeSpend());
            //taskExecInfo = new TaskExecInfo(finishedTask.getTaskRestart().getTimeInMillis(), finishedTask.getTaskEnd().getTimeInMillis(), finishedTask.getTimeSpend());
        }
        return taskExecInfo;
    }

    public long getTaskStart() {
        return mTaskStart;
    }

    public void setTaskStart(long taskStart) {
        mTaskStart = taskStart;
    }

    public long getTaskEnd() {
        return mTaskEnd;
    }

    public void setTaskEnd(long taskEnd) {
        mTaskEnd = taskEnd;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }
}
