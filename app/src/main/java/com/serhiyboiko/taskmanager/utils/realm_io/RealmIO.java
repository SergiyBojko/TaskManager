package com.serhiyboiko.taskmanager.utils.realm_io;

import android.content.Context;

import com.serhiyboiko.taskmanager.model.PauseInfo;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.model.TaskExecInfo;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class RealmIO {

    private final static int SCHEMA_VERSION = 2;

    private final static String TASK_ID = "mId";

    private Realm mRealm;

    public RealmIO(Context context){

        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(context)
                .schemaVersion(SCHEMA_VERSION)
                .migration(new TaskMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        mRealm = Realm.getDefaultInstance();
    }

    public Realm getRealm() {
        return mRealm;
    }

    public void putTask (Task task){
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(task);
        mRealm.commitTransaction();

    }

    public PauseInfo putPauseInfo (PauseInfo pauseInfo){
        PauseInfo managedPause = mRealm.copyToRealm(pauseInfo);
        return managedPause;
    }

    public TaskExecInfo putTaskExecInfo (TaskExecInfo taskExecInfo){
        TaskExecInfo managedTaskExecInfo = mRealm.copyToRealm(taskExecInfo);
        return managedTaskExecInfo;
    }

    public void hideTask(Task task){
        mRealm.beginTransaction();
        task.getPauseInfoList().deleteAllFromRealm();
        task.setHidden(true);
        mRealm.commitTransaction();

    }

    public void hideAllTasks() {
        RealmResults<Task> tasks = mRealm.where(Task.class).equalTo("mIsHidden", false).findAll();
        RealmResults<PauseInfo> pauses = mRealm.where(PauseInfo.class).findAll();
        mRealm.beginTransaction();
        for (Task t:tasks){
            t.setHidden(true);
        }
        pauses.deleteAllFromRealm();
        mRealm.commitTransaction();
    }

    public void deleteAllHiddenTasks(){
        RealmResults<Task> tasks = mRealm.where(Task.class).equalTo("mIsHidden", true).findAll();
        mRealm.beginTransaction();
        for (Task t:tasks){
            t.getTaskExecInfoList().deleteAllFromRealm();
        }
        tasks.deleteAllFromRealm();
        mRealm.commitTransaction();
    }

    public void deleteTask(Task task){
        mRealm.beginTransaction();
        task.getPauseInfoList().deleteAllFromRealm();
        task.getTaskExecInfoList().deleteAllFromRealm();
        task.deleteFromRealm();
        mRealm.commitTransaction();

    }

    public void deleteAllTasks() {
        RealmResults<Task> tasks = mRealm.where(Task.class).findAll();
        RealmResults<PauseInfo> pauses = mRealm.where(PauseInfo.class).findAll();
        RealmResults<TaskExecInfo> taskInfo = mRealm.where(TaskExecInfo.class).findAll();
        mRealm.beginTransaction();
        taskInfo.deleteAllFromRealm();
        pauses.deleteAllFromRealm();
        tasks.deleteAllFromRealm();
        mRealm.commitTransaction();
    }

    public RealmResults<Task> getAllTasks(){
        RealmResults<Task> tasks = mRealm.where(Task.class).equalTo("mIsHidden", false).findAll();
        return tasks;
    }

    public Task findTaskById (int id){
        Task task = mRealm.where(Task.class).equalTo(TASK_ID, id).findFirst();
        return task;
    }

    public void close(){
        mRealm.close();
    }

}
