package com.serhiyboiko.taskmanager.utils.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefsSerializer {
    private Context mContext;

    final static String SORTING_TYPE = "sorting_type";


    public SharedPrefsSerializer (Context context){
        mContext = context;
    }

    public void saveSorting (int sortingId){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SORTING_TYPE, sortingId);
        editor.commit();
    }

}
