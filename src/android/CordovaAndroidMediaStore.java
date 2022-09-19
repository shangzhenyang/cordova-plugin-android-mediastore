package com.heartade;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;

import android.provider.MediaStore;
import android.os.Build;
import android.content.ContentValues;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaAndroidMediaStore extends CordovaPlugin {
    private static final String[] SUPPORTED_MIME = {"image/png", "image/jpeg", "image/webp"};
    private static final Bitmap.CompressFormat[] BITMAP_FORMATS = {
        Bitmap.CompressFormat.PNG,
        Bitmap.CompressFormat.JPEG,
        Bitmap.CompressFormat.WEBP
    };

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("store")) {
            //String message = args.getString(0);
            this.store(args.getString(0), args.getString(1), args.getString(2), callbackContext);
            return true;
        }
        return false;
    }

    private void store(String byteString, String fileDir, String fileName, CallbackContext callbackContext) {
        // fileDir ex. /Pictures
        // fileName ex. capture.jpg
        final CordovaInterface _cordova = cordova;

        cordova.getThreadPool().execute(new Runnable() {
            CordovaInterface cordova = _cordova;
            @Override
            public void run() {
                try {
                    String[] fileNameSplit = fileName.split("\\.");
                    String fileExtension = fileNameSplit[fileNameSplit.length - 1];
                    int mimeIndex = 0;
                    if (fileExtension.equalsIgnoreCase("jpg") ||
                            fileExtension.equalsIgnoreCase("jpeg")) {
                        mimeIndex = 1;
                    } else if (fileExtension.equalsIgnoreCase("webp")) {
                        mimeIndex = 2;
                    }

                    byte[] byteArray = Base64.decode(byteString, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    Context context = this.cordova.getActivity();
                    ContentResolver contentResolver = context.getContentResolver();
                    if (Build.VERSION.SDK_INT >= 29) {
                        final ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, SUPPORTED_MIME[mimeIndex]);
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, fileDir);
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

                        Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                        OutputStream out = contentResolver.openOutputStream(imageUri);
                        bitmap.compress(BITMAP_FORMATS[mimeIndex], 100, out);

                        contentValues.clear();
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                        contentResolver.update(imageUri, contentValues, null, null);
                    } else {
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        File dir = new File(path + "/" + fileDir);
                        dir.mkdirs();
                        File file = new File(dir, fileName);
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(BITMAP_FORMATS[mimeIndex], 100, out);
                        Uri contentUri = Uri.fromFile(file);
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri));
                    }
                    callbackContext.success();
                } catch (RuntimeException | IOException e) {
                    e.printStackTrace();
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }
}
