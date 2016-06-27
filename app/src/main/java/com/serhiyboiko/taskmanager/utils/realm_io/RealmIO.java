package com.serhiyboiko.taskmanager.utils.realm_io;

import android.content.Context;

import com.serhiyboiko.taskmanager.model.Task;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class RealmIO {

    private final static String TASK_ID = "mId";

    private Realm mRealm;

    public Realm getRealm() {
        return mRealm;
    }

    public RealmIO(Context context){

        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).build();
        Realm.setDefaultConfiguration(realmConfig);

        mRealm = Realm.getDefaultInstance();
    }

    public void putTask (Task task){
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(task);
        mRealm.commitTransaction();

    }

    public void removeTask (Task task){
       task.deleteFromRealm();
    }

    public void removeAllTasks() {
        RealmResults<Task> tasks = mRealm.where(Task.class).findAll();
        mRealm.beginTransaction();
        tasks.deleteAllFromRealm();
        mRealm.commitTransaction();
    }

    public RealmResults<Task> getAllTasks(){
        RealmResults<Task> tasks = mRealm.where(Task.class).findAll();
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
