package com.serhiyboiko.taskmanager.utils.json;

import android.content.Context;

import com.serhiyboiko.taskmanager.model.Task;

import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by Amegar on 02.06.2016.
 */
public class JsonSerializer {
    private Context mContext;
    private String mFilename;

    public JsonSerializer(Context context, String filename) {
        mContext = context;
        mFilename = filename;
    }

    //converts task list to JSONArray and saves it to file
    public void saveTaskListToJson(ArrayList<Task> t) {
        final ArrayList<Task> taskList = t;
        JSONArray jsonTaskList = new JSONArray();
        for(Task tempTask:taskList){
            jsonTaskList.put(tempTask.createJson());
        }

        Writer writer = null;
        try{
            OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(jsonTaskList.toString());
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
