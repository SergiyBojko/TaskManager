package com.serhiyboiko.taskmanager.utils.json;

import android.content.Context;

import com.serhiyboiko.taskmanager.model.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

/**
 * Created by Amegar on 03.06.2016.
 */
public class JsonDeserializer {
    private Context mContext;
    private String mFilename;

    public JsonDeserializer(Context context, String filename) {
        mContext = context;
        mFilename = filename;
    }

    //reads JSON file and creates Array list of Tasks
    public ArrayList<Task> getTaskListFromJson(){
        JSONArray jsonTaskList;
        ArrayList<Task> taskArrayList = new ArrayList<>();

        Reader reader = null;
        BufferedReader bufferedReader = null;
        try {
            InputStream in = mContext.openFileInput(mFilename);
            reader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(reader);
            String tempString = "";

            StringBuilder jsonArrayString = new StringBuilder();
            //read string from file
            while ((tempString = bufferedReader.readLine()) != null) {
                jsonArrayString.append(tempString);
            }
            //create JSONArray from file
            jsonTaskList = new JSONArray(jsonArrayString.toString());
            JSONObject taskJson;
            Task task;
            //create Task objects from JSONArray
            for (int i = 0; i<jsonTaskList.length(); i++){
                taskJson = jsonTaskList.getJSONObject(i);
                task = new Task(taskJson);
                taskArrayList.add(task);
            }

        }catch (IOException e){
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        } finally{
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return taskArrayList;
    }
}
