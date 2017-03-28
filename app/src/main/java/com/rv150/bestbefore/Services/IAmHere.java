package com.rv150.bestbefore.Services;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by ivan on 21.03.17.
 */

public class IAmHere implements Runnable {

        private static final String serverURL = "http://212.109.192.197:8081/checkMe";
        private Context mContext;

        public IAmHere(Context mContext) {
            this.mContext = mContext;
        }

    @Override
        public void run() {
            try {
                String deviceId =  Settings.Secure.getString(mContext.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                makeRequest(deviceId);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage());
            }
        }

        private void makeRequest(String data) throws Exception {
            URL url;
            HttpURLConnection connection = null;
            JSONObject request = new JSONObject();
            try {
                request.put("deviceId", data);
                url = new URL(serverURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setConnectTimeout(5000);

                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(URLEncoder.encode(request.toString(), "UTF-8"));
                wr.flush();
                wr.close();
                connection.getResponseCode();
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }
