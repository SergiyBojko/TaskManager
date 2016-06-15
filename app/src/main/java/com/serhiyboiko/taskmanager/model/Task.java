package com.serhiyboiko.taskmanager.model;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.GregorianCalendar;

public class Task implements Parcelable{
    private final static long CALENDAR_EMPTY = -1;
    private String mTitle;
    private String mCommentary;
    private GregorianCalendar mTaskStart;
    private GregorianCalendar mTaskEnd;
    private long mTimeSpend;

    public final static String TITLE = "title";
    public final static String COMMENTARY = "commentary";
    public final static String START_DATE = "start";
    public final static String END_DATE = "end";
    public final static String ELAPSED_TIME = "elapsed_time";


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

        if (startTimeInMills != CALENDAR_EMPTY){
            mTaskStart = new GregorianCalendar();
            mTaskStart.setTimeInMillis(startTimeInMills);
            endTimeInMills = parcel.readLong();
            if (endTimeInMills != CALENDAR_EMPTY){
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
        } else {
            dest.writeLong(CALENDAR_EMPTY);
        }
        if (mTaskEnd != null){
            dest.writeLong(mTaskEnd.getTimeInMillis());
            dest.writeLong(mTimeSpend);
        } else {
            dest.writeLong(CALENDAR_EMPTY);
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
