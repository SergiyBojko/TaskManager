package com.serhiyboiko.taskmanager.utils.backup;

import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.support.v4.util.TimeUtils;
import android.util.Log;

import com.serhiyboiko.taskmanager.activity.TaskListActivity;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.file_io.FileIO;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;


import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.RealmResults;

/**
 * Created by user on 22.07.2016.
 */
public class BackupAgent extends android.app.backup.BackupAgent {
    // The name of the SharedPreferences file
    static final String PREFS = "/data/data/com.serhiyboiko.taskmanager/shared_prefs/com.serhiyboiko.taskmanager_preferences.xml";
    static final String DEFAULT_REALM = "/data/data/com.serhiyboiko.taskmanager/files/default.realm";

    @Override
    public void onCreate() {

    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        synchronized (TaskListActivity.sDataLock) {
            writeRealm(data);
            writeSharedPrefs(data);
            writeAvatars(data);
            Log.i("BackupAgent", "onBackup");
        }

    }

    private void writeRealm(BackupDataOutput data) {
        int bufferLength;
        File realm = new File(DEFAULT_REALM);
        try {
            byte[] buffer = FileUtils.readFileToByteArray(realm);
            bufferLength = buffer.length;
            data.writeEntityHeader(DEFAULT_REALM, bufferLength);
            data.writeEntityData(buffer, bufferLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSharedPrefs(BackupDataOutput data) {
        int bufferLength;
        File sharedPrefs = new File(PREFS);
        try {
            byte[] buffer = FileUtils.readFileToByteArray(sharedPrefs);
            bufferLength = buffer.length;
            data.writeEntityHeader(PREFS, bufferLength);
            data.writeEntityData(buffer, bufferLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeAvatars(BackupDataOutput data) throws IOException {
        String avatarPath;
        Bitmap avatar;
        byte[] buffer;
        int bufferLength;
        ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
        RealmResults<Task> tasks = new RealmIO(getApplicationContext()).getAllTasks();
        for (Task task:tasks){
            avatarPath = task.getAvatarLocation();
            Log.i("BackupAgent", "onBackup" + " " + avatarPath);
            if (avatarPath != null && !avatarPath.equals("")){
                avatar = FileIO.loadImage(avatarPath);
                avatar.compress(Bitmap.CompressFormat.PNG, 100, bufStream);
                buffer = bufStream.toByteArray();
                bufferLength = buffer.length;
                Log.i("BackupAgent", "bufferLength" + " " + bufferLength);
                Log.i("BackupAgent", "buffer" + " " + buffer);
                data.writeEntityHeader(avatarPath, bufferLength);
                data.writeEntityData(buffer, bufferLength);
            }
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        synchronized (TaskListActivity.sDataLock) {
            Log.i("BackupAgent", "onRestore");
            String key;
            int bufferLength;
            File file;
            FileOutputStream out;
            while (data.readNextHeader()){
                key = data.getKey();
                bufferLength = data.getDataSize();
                Log.i("BackupAgent", "entity_key : " + key);

                byte[] buffer = new byte[bufferLength];
                data.readEntityData(buffer, 0, bufferLength);
                file = new File(key);
                if (!file.exists()){
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                out = new FileOutputStream(file);
                out.write(buffer);
                out.close();
            }
        }
    }
}
