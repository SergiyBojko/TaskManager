package com.serhiyboiko.taskmanager.model;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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

    public Task(JSONObject json){
        long startTimeInMills, endTimeInMills, elapsedTimeInMills = 0;
        try {
            mTitle = json.getString(TITLE);
            mCommentary = json.getString(COMMENTARY);
            startTimeInMills = json.getLong(START_DATE);
            if (startTimeInMills != CALENDAR_EMPTY){
                mTaskStart = new GregorianCalendar();
                mTaskStart.setTimeInMillis(startTimeInMills);
                endTimeInMills = json.getLong(END_DATE);
                if (endTimeInMills != CALENDAR_EMPTY){
                    mTaskEnd = new GregorianCalendar();
                    mTaskEnd.setTimeInMillis(endTimeInMills);
                    mTimeSpend = json.getLong(ELAPSED_TIME);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Task(SharedPreferences sp, int index){
        long startTimeInMills, endTimeInMills, elapsedTimeInMills = 0;
        mTitle = sp.getString(TITLE + index, "");
        mCommentary = sp.getString(COMMENTARY + index, "");
        startTimeInMills = sp.getLong(START_DATE + index, CALENDAR_EMPTY);
        if (startTimeInMills != CALENDAR_EMPTY) {
            mTaskStart = new GregorianCalendar();
            mTaskStart.setTimeInMillis(startTimeInMills);
            endTimeInMills = sp.getLong(END_DATE + index, CALENDAR_EMPTY);
            if (endTimeInMills != CALENDAR_EMPTY){
                mTaskEnd = new GregorianCalendar();
                mTaskEnd.setTimeInMillis(endTimeInMills);
                mTimeSpend = sp.getLong(ELAPSED_TIME + index, CALENDAR_EMPTY);
            }
        }

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

    public JSONObject createJson (){
        JSONObject jsonObject = new JSONObject();
        long startTimeInMills = CALENDAR_EMPTY;
        long endTimeInMills = CALENDAR_EMPTY;
        long elapsedTime = CALENDAR_EMPTY;
        if (mTaskStart != null){
            startTimeInMills = mTaskStart.getTimeInMillis();
            if (mTaskEnd != null){
                endTimeInMills = mTaskEnd.getTimeInMillis();
                elapsedTime = mTimeSpend;
            }
        }
        try {
            jsonObject.put(TITLE, mTitle);
            jsonObject.put(COMMENTARY, mCommentary);
            jsonObject.put(START_DATE, startTimeInMills);
            jsonObject.put(END_DATE, endTimeInMills);
            jsonObject.put(ELAPSED_TIME, elapsedTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public void saveInSharedPrefs(SharedPreferences.Editor editor, int i) {
        long startTimeInMills = CALENDAR_EMPTY;
        long endTimeInMills = CALENDAR_EMPTY;
        long elapsedTime = CALENDAR_EMPTY;

        if(mTaskStart != null){
            startTimeInMills = mTaskStart.getTimeInMillis();

            if(mTaskEnd != null){
                endTimeInMills = mTaskEnd.getTimeInMillis();
                elapsedTime = mTimeSpend;
            } else {
            }
        }
        editor.putString(TITLE + i, mTitle);
        editor.putString(COMMENTARY + i, mCommentary);
        editor.putLong(START_DATE + i, startTimeInMills);
        editor.putLong(END_DATE + i, endTimeInMills);
        editor.putLong(ELAPSED_TIME + i, elapsedTime);
    }

    @Override
    public String toString() {
        return mTitle + " " + mCommentary + " " +  mTaskStart + " " +  mTaskEnd;
    }
}
