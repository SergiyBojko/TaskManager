package com.serhiyboiko.taskmanager.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable{
    private String mTitle;
    private String mCommentary;

    public Task(){

    }

    public Task(String title, String commentary){
        mTitle = title;
        mCommentary = commentary;
    }

    private Task(Parcel parcel) {
        mTitle = parcel.readString();
        mCommentary = parcel.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    //implemented parcelable methods to allow saving task in Bundle
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mCommentary);
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };



}
