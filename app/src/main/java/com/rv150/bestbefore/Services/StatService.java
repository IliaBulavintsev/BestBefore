package com.rv150.bestbefore.Services;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ivan on 25.03.17.
 */

public class StatService {

    private static final String serverURL = "http://212.109.192.197:8081/";

    private static final String DEVICE_ID = "deviceId";
    private static final String ACTION = "action";
    private static final String ACTION_IMPORT = "import";
    private static final String ACTION_EXPORT_TO_FILE = "export_file";
    private static final String ACTION_EXPORT_TO_EXCEL = "export_excel";
    private static final String ACTION_GOOGLE_BACKUP = "google_backup";
    private static final String ACTION_GOOGLE_RESTORE = "google_restore";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void markImport(Context context) {
        makeJob(context, ACTION_IMPORT);
    }

    public static void markExportFile(Context context) {
        makeJob(context, ACTION_EXPORT_TO_FILE);
    }

    public static void markExportExcel(Context context) {
        makeJob(context, ACTION_EXPORT_TO_EXCEL);
    }

    public static void markGoogleBackup(Context context) {
        makeJob(context, ACTION_GOOGLE_BACKUP);
    }

    public static void markGoogleRestore(Context context) {
        makeJob(context, ACTION_GOOGLE_RESTORE);
    }


    private static void makeJob(Context context, String action) {
        try {
            JSONObject request = new JSONObject();
            request.put(DEVICE_ID, getDeviceId(context));
            request.put(ACTION, action);
            executor.execute(new Request(serverURL + "stat", request));
        }
        catch (Exception ex) {
            Log.e(StatService.class.getSimpleName(), ex.getMessage());
        }
    }




    private static class Request implements Runnable {
        private final String URL;
        private final JSONObject data;

        Request(String URL, JSONObject data) {
            this.URL = URL;
            this.data = data;
        }

        @Override
        public void run() {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setConnectTimeout(5000);

                Log.d(StatService.class.getSimpleName(), "Sending request...");

                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(URLEncoder.encode(data.toString(), "UTF-8"));
                wr.flush();
                wr.close();
                connection.getResponseCode();
                Log.d(StatService.class.getSimpleName(), "Response was got");
            }
            catch (Exception ex) {
                Log.e(StatService.class.getSimpleName(), ex.getMessage());
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }




    private static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
}
