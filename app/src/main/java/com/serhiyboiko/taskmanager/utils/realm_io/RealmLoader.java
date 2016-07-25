package com.serhiyboiko.taskmanager.utils.realm_io;


import android.content.Context;
import android.support.v4.content.Loader;

import com.serhiyboiko.taskmanager.model.Task;

import io.realm.RealmResults;

/**
 * Created on 14.07.2016.
 */
public class RealmLoader extends Loader<RealmResults<Task>> {

    private Context mContext;
    private RealmResults<Task> mTaskListRealmResults;


    public RealmLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        RealmIO RealmIO = new RealmIO(mContext);
        mTaskListRealmResults = RealmIO.getAllTasks();
        deliverResult(mTaskListRealmResults);
    }

    @Override
    protected void onStopLoading() {
        mTaskListRealmResults.removeChangeListeners();
    }
}
