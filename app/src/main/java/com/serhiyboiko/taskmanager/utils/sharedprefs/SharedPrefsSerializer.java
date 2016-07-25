package com.serhiyboiko.taskmanager.utils.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefsSerializer {
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public SharedPrefsSerializer (Context context){
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void saveSorting (int sortingId){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(SharedPrefsDeserializer.SORTING_TYPE, sortingId);
        editor.apply();
    }

    public static void newTaskTutorialCompleted (Context context, boolean isCompleted){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsDeserializer.NEW_TASK_TUTORIAL_COMPLETED, isCompleted);
        editor.apply();
    }

    public static void sortingTutorialCompleted (Context context, boolean isCompleted){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsDeserializer.SORTING_TUTORIAL_COMPLETED, isCompleted);
        editor.apply();
    }

    public static void settingsTutorialCompleted (Context context, boolean isCompleted){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsDeserializer.SETTINGS_TUTORIAL_COMPLETED, isCompleted);
        editor.apply();
    }

    public static void taskLocationTutorialCompleted (Context context, boolean isCompleted){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SharedPrefsDeserializer.LOCATION_TUTORIAL_COMPLETED, isCompleted);
        editor.apply();
    }

}
