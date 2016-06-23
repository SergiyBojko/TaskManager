package com.serhiyboiko.taskmanager.utils.realm_io;

import android.content.Context;

import com.serhiyboiko.taskmanager.adapter.TaskListAdapter;
import com.serhiyboiko.taskmanager.model.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by Amegar on 23.06.2016.
 */
public class RealmIO {

    private TaskListAdapter mTaskListAdapter;
    private Realm mRealm;
    private RealmResults<Task> mTasks;

    public RealmIO(Context context){

        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).build();
        Realm.setDefaultConfiguration(realmConfig);

        mRealm = Realm.getDefaultInstance();

        mTasks = mRealm.where(Task.class).findAll();
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
        mRealm.beginTransaction();
        mTasks.deleteAllFromRealm();
        mRealm.commitTransaction();
    }

    public ArrayList<Task> getAllTasks(){
        Task[] taskArray = {};
        taskArray = mTasks.toArray(taskArray);
        ArrayList<Task> taskList = new ArrayList<>(Arrays.asList(taskArray));

        return taskList;
    }

    public Task findTaskByRequestCode (int requestCode){
        Task task = mRealm.where(Task.class).equalTo("mAlertRequestCode", requestCode).findFirst();
        return task;
    }

    public void close(){
        mRealm.close();
    }


}
