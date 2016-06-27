package com.serhiyboiko.taskmanager.utils.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.serhiyboiko.taskmanager.R;

public class SharedPrefsDeserializer {
    private Context mContext;

    static final String IDLE_TASK_BACKGROUND_COLOR = "idle_task_background_color";
    static final String STARTED_TASK_BACKGROUND_COLOR = "started_task_background_color";
    static final String ENDED_TASK_BACKGROUND_COLOR = "finished_task_background_color";
    final static String MAXIMUM_TASK_DURATION = "maximum_task_duration";

    public SharedPrefsDeserializer(Context context){
        mContext = context;
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

    public int getMaxTaskDuration(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int maxDuration = sp.getInt(MAXIMUM_TASK_DURATION, 0);
        return maxDuration;
    }
}
