package com.serhiyboiko.taskmanager.utils.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.model.Task;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Amegar on 03.06.2016.
 */
public class SharedPrefsDeserializer {
    private Context mContext;

    private final static int DEFAULT_IDLE_TASK_COLOR = 0xffffdddd;
    private final static int DEFAULT_STARTED_TASK_COLOR = 0xffffffcc;
    private final static int DEFAULT_ENDED_TASK_COLOR = 0xffddffdd;

    static final String IDLE_TASK_BACKGROUND_COLOR = "idle_task_background_color";
    static final String STARTED_TASK_BACKGROUND_COLOR = "started_task_background_color";
    static final String ENDED_TASK_BACKGROUND_COLOR = "finished_task_background_color";

    public SharedPrefsDeserializer(Context context){
        mContext = context;
    }

    public ArrayList<Task> getTaskList(){
        Gson gson = new Gson();
        Type taskListType = new TypeToken<Collection<Task>>() {}.getType();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String taskListJson = sp.getString(SharedPrefsSerializer.TASK_JSON_ARRAY, "");
        ArrayList<Task> taskList = gson.fromJson(taskListJson, taskListType);
        if (taskList == null){
            taskList = new ArrayList<>();
        }
        return taskList;
    }

    public int getListSorting(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int sortingId = sp.getInt(SharedPrefsSerializer.SORTING_TYPE, -1);
        return sortingId;
    }

    public int[] getTaskBackgroundColors() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int idleColor = sp.getInt(IDLE_TASK_BACKGROUND_COLOR,
                mContext.getResources().getColor(R.color.idle_task_background_color_default));
        int startedColor = sp.getInt(STARTED_TASK_BACKGROUND_COLOR,
                mContext.getResources().getColor(R.color.started_task_background_color_default));
        int endedColor = sp.getInt(ENDED_TASK_BACKGROUND_COLOR,
                mContext.getResources().getColor(R.color.finished_task_background_color_default));
        int[] taskBackgroundColors = {idleColor, startedColor, endedColor};
        return taskBackgroundColors;
    }
}
