package com.serhiyboiko.taskmanager.model;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Comparator;
import java.util.GregorianCalendar;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Parcelable{
    private final static long CALENDAR_EMPTY = -1;
    private static final String NEXT_TASK_ID = "next_task_id";

    @PrimaryKey
    private long mId;
    private String mTitle;
    private String mCommentary;
    private long mTaskStart;
    private long mTaskEnd;
    private long mTaskRestart;
    private long mTimeSpend;
    private int mAlertRequestCode;

    public Task(){}

    public Task(Context context, String commentary, String title){
        mTitle = title;
        mCommentary = commentary;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        mId = sp.getLong(NEXT_TASK_ID, 0);

        long nextId = ++mId;
        sp.edit().putLong(NEXT_TASK_ID, nextId).commit();

        Log.i("created new task", this.toString());
    }

    private Task(Parcel parcel) {
        mTitle = parcel.readString();
        mCommentary = parcel.readString();
        long startTimeInMills;
        long endTimeInMills;
        long restartTimeInMills;
        startTimeInMills = parcel.readLong();

        if (startTimeInMills != CALENDAR_EMPTY){
            mTaskStart = startTimeInMills;
            endTimeInMills = parcel.readLong();
            if (endTimeInMills != CALENDAR_EMPTY){
                mTaskEnd = endTimeInMills;
                mTimeSpend = parcel.readLong();
            }
        }
        restartTimeInMills = parcel.readLong();
        if (restartTimeInMills != CALENDAR_EMPTY){
            mTaskRestart = restartTimeInMills;
        }

        mId = parcel.readLong();


        Log.i("created from parcel", this.toString());
    }


    public long getId() {
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
        if (taskEnd != null){
            mTaskEnd = taskEnd.getTimeInMillis();
            mTimeSpend = mTaskEnd - mTaskStart;
        } else {
            mTaskEnd = 0;
            mTimeSpend = 0;
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
        }
    }

    public long getTimeSpend() {
        return mTimeSpend;
    }

    public void setTimeSpend(long timeSpend) {
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

    public int getAlertRequestCode() {
        return mAlertRequestCode;
    }

    public void setAlertRequestCode(int alertRequestCode) {
        mAlertRequestCode = alertRequestCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //implemented parcelable methods to allow saving task in Bundle
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mCommentary);
        if (mTaskStart > 0){
            dest.writeLong(mTaskStart);
        } else {
            dest.writeLong(CALENDAR_EMPTY);
        }
        if (mTaskEnd > 0){
            dest.writeLong(mTaskEnd);
            dest.writeLong(mTimeSpend);
        } else {
            dest.writeLong(CALENDAR_EMPTY);
        }
        if (mTaskRestart > 0){
            dest.writeLong(mTaskRestart);
        } else {
            dest.writeLong(CALENDAR_EMPTY);
        }

        dest.writeLong(mId);

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

    public static class ComparatorAZ implements Comparator<Task>{

        @Override
        public int compare(Task lhs, Task rhs) {
            int comparison = lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
            if (comparison != 0){
                return comparison;
            }
            GregorianCalendar lhsStart = lhs.getTaskStart();
            GregorianCalendar rhsStart = rhs.getTaskStart();
            if(lhsStart != null && rhsStart != null){
                comparison = (int)(rhsStart.getTimeInMillis() - lhsStart.getTimeInMillis());
            } else {
                if (lhsStart == null & rhsStart == null) return 0;
                if (rhsStart == null) return -1;
                if (lhsStart == null) return 1;
            }
            return comparison;
        }
    }

    public static class ComparatorZA implements Comparator<Task>{

        @Override
        public int compare(Task lhs, Task rhs) {
            int comparison = rhs.getTitle().toLowerCase().compareTo(lhs.getTitle().toLowerCase());
            if (comparison != 0){
                return comparison;
            }
            GregorianCalendar lhsStart = lhs.getTaskStart();
            GregorianCalendar rhsStart = rhs.getTaskStart();
            if(lhsStart != null && rhsStart != null){
                comparison = (int)(rhsStart.getTimeInMillis() - lhsStart.getTimeInMillis());
            } else {
                if (lhsStart == null & rhsStart == null) return 0;
                if (rhsStart == null) return -1;
                if (lhsStart == null) return 1;
            }
            return comparison;
        }
    }

    public static class ComparatorNewerOlder implements Comparator<Task>{

        @Override
        public int compare(Task lhs, Task rhs) {
            int comparison = 0;
            GregorianCalendar lhsStart = lhs.getTaskStart();
            GregorianCalendar rhsStart = rhs.getTaskStart();
            if(lhsStart != null && rhsStart != null){
                comparison = (int)(rhsStart.getTimeInMillis() - lhsStart.getTimeInMillis());
            } else {
                if (rhsStart == null) comparison += -1;
                if (lhsStart == null) comparison += 1;
            }
            if (comparison != 0){
                return comparison;
            }
            comparison = lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
            return comparison;
        }
    }

    public static class ComparatorOlderNewer implements Comparator<Task>{

        @Override
        public int compare(Task lhs, Task rhs) {
            int comparison = 0;
            GregorianCalendar lhsStart = lhs.getTaskStart();
            GregorianCalendar rhsStart = rhs.getTaskStart();
            if(lhsStart != null && rhsStart != null){
                comparison = (int)(lhsStart.getTimeInMillis() - rhsStart.getTimeInMillis());
            } else {
                if (rhsStart == null) comparison += -1;
                if (lhsStart == null) comparison += 1;
            }
            if (comparison != 0){
                return comparison;
            }
            comparison = lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
            return comparison;
        }
    }
}
