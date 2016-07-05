package com.serhiyboiko.taskmanager.model;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Parcelable{

    public final static int ONE_TIME = 0;
    public final static int EVERY_HOUR = 1;
    public final static int EVERY_DAY = 2;
    public final static int EVERY_WEEK = 3;
    public final static int EVERY_MONTH = 4;
    public final static int EVERY_YEAR = 5;

    public static final String NEXT_TASK_ID = "next_task_id";

    @PrimaryKey
    private int mId;
    private String mTitle;
    private String mCommentary;
    private long mTaskStart;
    private long mTaskEnd;
    private long mTaskRestart;
    private long mTimeSpend;
    private int mTaskMaxDuration;
    private RealmList<PauseInfo> mPauseInfoList;
    private String mAvatarLocation;
    private int mPeriod;
    private boolean mIsPaused;
    //tasks that are deleted from app become hidden to provide statistics information
    private boolean mIsHidden;
    private RealmList<TaskExecInfo> mTaskExecInfoList;

    public Task(){}

    public Task(Context context, String commentary, String title, int taskMaxDuration, int period, String avatarLocation){
        mTitle = title;
        mCommentary = commentary;
        mTaskMaxDuration = taskMaxDuration;
        mPeriod = period;
        mAvatarLocation = avatarLocation;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        mId = sp.getInt(NEXT_TASK_ID, 0);
        int nextId = mId + 1;
        sp.edit().putInt(NEXT_TASK_ID, nextId).apply();

        Log.i("created new task", "id = " + mId);
        Log.i("created new task", "AvatarLocation = " + mAvatarLocation);
    }

    private Task(Parcel parcel) {
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getCommentary() {
        return mCommentary;
    }

    public void setCommentary(String commentary) {
        mCommentary = commentary;
    }

    public GregorianCalendar getTaskEnd() {
        if(mTaskEnd == 0){
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(mTaskEnd);
        return calendar;
    }

    public void setTaskEnd(GregorianCalendar taskEnd) {
        if (isPaused()){
            mIsPaused = false;
            if (mPauseInfoList.size() > 0){
                PauseInfo lastPause = mPauseInfoList.get(mPauseInfoList.size()-1);
                lastPause.finish();
            }
        }

        if (taskEnd != null){
            mTaskEnd = taskEnd.getTimeInMillis();
            int totalPauseDuration = getTotalPauseDuration();
            if (mTaskRestart == 0){
                mTimeSpend += mTaskEnd - mTaskStart - totalPauseDuration;
            } else {
                mTimeSpend += mTaskEnd - mTaskRestart - totalPauseDuration;
            }

        } else {
            if (mTaskRestart != 0 || mTaskStart != 0){
                //task state finished -> running
                mTimeSpend += new GregorianCalendar().getTimeInMillis() - mTaskEnd;
                mTaskEnd = 0;
            } else {
                //task state finished -> idle or running -> idle
                mTimeSpend = 0;
                mTaskEnd = 0;
            }

        }
    }

    public GregorianCalendar getTaskStart() {
        if(mTaskStart == 0){
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(mTaskStart);
        return calendar;
    }

    public void setTaskStart(GregorianCalendar taskStart) {
        if (taskStart != null){
            mTaskStart = taskStart.getTimeInMillis();
        } else {
            mTaskStart = 0;
            mTimeSpend = 0;
            mPauseInfoList.deleteAllFromRealm();
        }
    }

    public long getTimeSpend() {
        return mTimeSpend;
    }

    public void setTimeSpend(int timeSpend) {
        mTimeSpend = timeSpend;
    }

    public GregorianCalendar getTaskRestart() {
        if(mTaskRestart == 0){
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(mTaskRestart);
        return calendar;
    }

    public void setTaskRestart(GregorianCalendar taskRestart) {
        if (taskRestart != null){
            mTaskRestart = taskRestart.getTimeInMillis();
        } else {
            mTaskRestart = 0;
        }

    }

    public int getTaskMaxDuration() {
        return mTaskMaxDuration;
    }

    public void setTaskMaxDuration(int taskMaxDuration) {
        mTaskMaxDuration = taskMaxDuration;
    }

    public String getAvatarLocation() {
        return mAvatarLocation;
    }

    public void setAvatarLocation(String avatarLocation) {
        mAvatarLocation = avatarLocation;
    }

    public int getPeriod(){
        return mPeriod;
    }

    public int getPeriodInMills() {
        GregorianCalendar calendar;
        long taskStartTime = mTaskStart;
        long taskNextStartTime;
        int period;

        switch (mPeriod){
            case ONE_TIME:
                return 0;
            case EVERY_HOUR:
                //return 1000*60*60;
                return 1000*10;
            case EVERY_DAY:
                return 1000*60*60*24;
            case EVERY_WEEK:
                return 1000*60*60*24*7;
            case EVERY_MONTH:
                calendar = new GregorianCalendar()     ;
                calendar.setTimeInMillis(taskStartTime);
                calendar.add(Calendar.MONTH, 1);
                taskNextStartTime = calendar.getTimeInMillis();
                period = (int)(taskNextStartTime - taskStartTime);
                return period;
            case EVERY_YEAR:
                calendar = new GregorianCalendar();
                calendar.setTimeInMillis(taskStartTime);
                calendar.add(Calendar.YEAR, 1);
                taskNextStartTime = calendar.getTimeInMillis();
                period = (int)(taskNextStartTime - taskStartTime);
                return period;
            default:
                return 0;
        }
    }

    public void setPeriod(int period) {
        mPeriod = period;
    }

    public RealmList<PauseInfo> getPauseInfoList() {
        return mPauseInfoList;
    }

    public void setPaused(boolean paused) {
        mIsPaused = paused;
    }

    public boolean isPaused() {
        return mIsPaused;
    }

    public boolean isHidden() {
        return mIsHidden;
    }

    public void setHidden(boolean hidden) {
        mIsHidden = hidden;
    }

    public RealmList<TaskExecInfo> getTaskExecInfoList() {
        return mTaskExecInfoList;
    }

    public void setTaskExecInfoList(RealmList<TaskExecInfo> taskExecInfoList) {
        mTaskExecInfoList = taskExecInfoList;
    }

    public int getTotalPauseDuration() {
        int totalPauseDuration = 0;
        for (PauseInfo pauseInfo:mPauseInfoList){
            totalPauseDuration += pauseInfo.getPauseDuration();
        }
        return totalPauseDuration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //implemented parcelable methods to allow saving task in Bundle
    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Override
    public String toString() {
        return mId + " " + mTitle + " " + mCommentary + " " +  mTaskStart + " " +  mTaskEnd;
    }

    public Object getTotalDuration() {
        long taskTotalDuration = 0;
        for(TaskExecInfo taskExecInfo:mTaskExecInfoList){
            taskTotalDuration += taskExecInfo.getDuration();
        }
        return taskTotalDuration;
    }
}
