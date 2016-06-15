package com.serhiyboiko.taskmanager.utils.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.serhiyboiko.taskmanager.model.Task;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Amegar on 03.06.2016.
 */
public class SharedPrefsSerializer {
    private Context mContext;

    final static String TASK_JSON_ARRAY = "task_json_array";
    final static String SORTING_TYPE = "sorting_type";


    public SharedPrefsSerializer (Context context){
        mContext = context;
    }

    public void saveTaskList(ArrayList<Task> taskList){
        Gson gson = new Gson();
        Type taskListType = new TypeToken<Collection<Task>>() {}.getType();
        String taskListJson = gson.toJson(taskList, taskListType);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TASK_JSON_ARRAY, taskListJson);
        editor.commit();
    }

    public void saveSorting (int sortingId){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SORTING_TYPE, sortingId);
        editor.commit();
    }

}
