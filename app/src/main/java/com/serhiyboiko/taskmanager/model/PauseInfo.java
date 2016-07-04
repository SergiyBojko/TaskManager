package com.serhiyboiko.taskmanager.model;

import java.util.GregorianCalendar;

import io.realm.RealmObject;

/**
 * Created on 02.07.2016.
 */
public class PauseInfo extends RealmObject {
    private long mPauseStart;
    private long mPauseDuration;

    public PauseInfo() {
    }

    public long getPauseDuration() {
        return mPauseDuration;
    }

    public void start() {
        mPauseStart = new GregorianCalendar().getTimeInMillis();
    }

    public void finish() {
        long finishTime = new GregorianCalendar().getTimeInMillis();
        mPauseDuration = finishTime - mPauseStart;
    }
}
