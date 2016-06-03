package com.serhiyboiko.taskmanager.utils.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.serhiyboiko.taskmanager.model.Task;

import java.util.ArrayList;

/**
 * Created by Amegar on 03.06.2016.
 */
public class SharedPrefsDeserializer {
    private Context mContext;
    private String mFilename;

    public SharedPrefsDeserializer(Context context, String filename){
        mContext = context;
        mFilename = filename;
    }

    public ArrayList<Task> getTaskListFromSharedPrefs(){
        SharedPreferences sp = mContext.getSharedPreferences(mFilename, Context.MODE_PRIVATE);
        Task task;
        ArrayList<Task> taskArrayList= new ArrayList<>();
        int taskListSize = sp.getInt(SharedPrefsSerializer.LIST_SIZE, 0);
        for (int i = 0; i<taskListSize; i++){
            taskArrayList.add(new Task(sp, i));
        }
        return taskArrayList;
    }
}
