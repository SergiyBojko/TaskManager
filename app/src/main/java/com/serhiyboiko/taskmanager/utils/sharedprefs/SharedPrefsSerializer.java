package com.serhiyboiko.taskmanager.utils.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.serhiyboiko.taskmanager.model.Task;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Amegar on 03.06.2016.
 */
public class SharedPrefsSerializer {
    private Context mContext;
    private String mFilename;

    final static String LIST_SIZE = "list_size";

    public SharedPrefsSerializer (Context context, String filename){
        mContext = context;
        mFilename = filename;
    }

    public void saveTaskListToSharedPrefs (ArrayList<Task> taskList){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mFilename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int listSize = taskList.size();
        editor.putInt(LIST_SIZE, listSize);
        for (int i = 0; i<listSize; i++){
            Task item = taskList.get(i);
            item.saveInSharedPrefs(editor, i);
        }
        editor.commit();

    }


}
