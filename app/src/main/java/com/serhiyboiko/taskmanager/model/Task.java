package com.serhiyboiko.taskmanager.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.GregorianCalendar;

public class Task implements Parcelable{
    private String mTitle;
    private String mCommentary;
    private GregorianCalendar mTaskStart;
    private GregorianCalendar mTaskEnd;
    private long mTimeSpend;


    public Task(){

    }

    public Task(String title, String commentary){
        mTitle = title;
        mCommentary = commentary;
        Log.i("created new item", this.toString());
    }

    private Task(Parcel parcel) {
        mTitle = parcel.readString();
        mCommentary = parcel.readString();
        long startTimeInMills = 0;
        long endTimeInMills = 0;
        startTimeInMills = parcel.readLong();

        if (startTimeInMills != 0){
            mTaskStart = new GregorianCalendar();
            mTaskStart.setTimeInMillis(startTimeInMills);
            endTimeInMills = parcel.readLong();
            if (endTimeInMills != 0){
                mTaskEnd = new GregorianCalendar();
                mTaskEnd.setTimeInMillis(endTimeInMills);
                mTimeSpend = parcel.readLong();
            }
        }


        Log.i("created from parcel", this.toString());
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
        return mTaskEnd;
    }

    public void setTaskEnd(GregorianCalendar taskEnd) {
        mTaskEnd = taskEnd;
    }

    public GregorianCalendar getTaskStart() {
        return mTaskStart;
    }

    public void setTaskStart(GregorianCalendar taskStart) {
        mTaskStart = taskStart;
    }

    public long getTimeSpend() {
        return mTimeSpend;
    }

    public void setTimeSpend(long timeSpend) {
        mTimeSpend = timeSpend;
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
        if (mTaskStart != null){
            dest.writeLong(mTaskStart.getTimeInMillis());
        }
        if (mTaskEnd != null){
            dest.writeLong(mTaskEnd.getTimeInMillis());
            dest.writeLong(mTimeSpend);
        }


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
        return mTitle + " " + mCommentary + " " +  mTaskStart + " " +  mTaskEnd;
    }
}
