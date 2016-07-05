package com.serhiyboiko.taskmanager.utils.file_io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created on 30.06.2016.
 */
public class FileIO {

    public static String saveImage (Bitmap bitmap, String filename, Context context){

        File image;
        FileOutputStream out;
        if (isExternalStorageWritable()){
            image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
        } else {
            image = new File(context.getFilesDir(), filename);
        }

        try {
            out = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image.toString();
    }

    public static Bitmap loadImage (String path){
        return BitmapFactory.decodeFile(path);
    }

    public static void removeFile (String path){
        if(path != null && !path.isEmpty()){
            File file = new File(path);
            file.delete();
        }
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
