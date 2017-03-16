package org.cheeseandbacon.wellsweather;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public final class StorageHelper {
    private static final String TAG = "StorageHelper";

    private static byte[] getBytesFromObject(Object object){
        byte[] bytes = null;

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutput output = new ObjectOutputStream(byteArrayOutputStream);
            output.writeObject(object);
            output.flush();

            bytes = byteArrayOutputStream.toByteArray();

            byteArrayOutputStream.close();
        } catch (IOException e) {
            Log.w(TAG, "IOException occurred while getting bytes from object", e);
        }

        return bytes;
    }

    private static Object getObjectFromBytes(byte[] bytes){
        Object object = null;

        try{
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInput input = new ObjectInputStream(byteArrayInputStream);
            object = input.readObject();
        } catch (IOException e) {
            Log.w(TAG, "IOException occurred while getting object from bytes", e);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "ClassNotFoundException occurred while getting object from bytes", e);
        }

        return object;
    }

    public static void saveToInternal(Context context, String fileName, Object object){
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(getBytesFromObject(object));
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "FileNotFoundException occurred while saving data to internal storage", e);
        } catch (IOException e) {
            Log.w(TAG, "IOException occurred while saving data to internal storage", e);
        }
    }

    public static Object loadFromInternal(Context context, String fileName){
        Object object = null;

        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);
            byte[] bytes = new byte[(int)fileInputStream.getChannel().size()];
            fileInputStream.read(bytes);

            object = getObjectFromBytes(bytes);

            fileInputStream.close();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "FileNotFoundException occurred while loading data from internal storage", e);
        } catch (IOException e) {
            Log.w(TAG, "IOException occurred while loading data from internal storage", e);
        }

        return object;
    }
}
