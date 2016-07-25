package com.serhiyboiko.taskmanager.utils.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.serhiyboiko.taskmanager.R;

public class SharedPrefsDeserializer {
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    static final String IDLE_TASK_BACKGROUND_COLOR = "idle_task_background_color";
    static final String STARTED_TASK_BACKGROUND_COLOR = "started_task_background_color";
    static final String ENDED_TASK_BACKGROUND_COLOR = "finished_task_background_color";
    final static String MAXIMUM_TASK_DURATION = "maximum_task_duration";
    final static String SORTING_TYPE = "sorting_type";

    final static String SORTING_TUTORIAL_COMPLETED = "sorting_tutorial_completed";
    final static String SETTINGS_TUTORIAL_COMPLETED = "settings_tutorial_completed";
    final static String LOCATION_TUTORIAL_COMPLETED = "location_tutorial_completed";
    final static String NEW_TASK_TUTORIAL_COMPLETED = "new_task_tutorial_completed";


    public SharedPrefsDeserializer(Context context){
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public int getListSorting(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int sortingId = sp.getInt(SORTING_TYPE, -1);
        return sortingId;
    }

    public int[] getTaskBackgroundColors() {
        int idleColor = mSharedPreferences.getInt(IDLE_TASK_BACKGROUND_COLOR,
                mContext.getResources().getColor(R.color.idle_task_background_color_default));
        int startedColor = mSharedPreferences.getInt(STARTED_TASK_BACKGROUND_COLOR,
                mContext.getResources().getColor(R.color.started_task_background_color_default));
        int endedColor = mSharedPreferences.getInt(ENDED_TASK_BACKGROUND_COLOR,
                mContext.getResources().getColor(R.color.finished_task_background_color_default));
        int[] taskBackgroundColors = {idleColor, startedColor, endedColor};
        return taskBackgroundColors;
    }

    public int getMaxTaskDuration(){
        return mSharedPreferences.getInt(MAXIMUM_TASK_DURATION, 0);

    }

    public static boolean isNewTaskTutorialCompleted (Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(NEW_TASK_TUTORIAL_COMPLETED, false);
    }

    public static boolean isSortingTutorialCompleted (Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SORTING_TUTORIAL_COMPLETED, false);
    }

    public static boolean isSettingsTutorialCompleted (Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SETTINGS_TUTORIAL_COMPLETED, false);
    }

    public static boolean isTaskLocationTutorialCompleted (Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(LOCATION_TUTORIAL_COMPLETED, false);
    }
}
