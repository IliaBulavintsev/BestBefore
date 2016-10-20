package com.rv150.bestbefore.Network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rv150.bestbefore.Resources;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Rudnev on 20.10.2016.
 */

public class SendStatistic extends AsyncTask<String, String, String> {
        private Context context;
        private String responseStr;

        public SendStatistic(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String dataUrl = Resources.SERVER_URL + "stat";
            URL url;
            HttpURLConnection connection = null;
            try {
// Create connection
                url = new URL(dataUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setConnectTimeout(3000);


// Send request
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(URLEncoder.encode(params[0], "UTF-8"));
                wr.flush();
                wr.close();

                Log.d("status: ", "Stat has been sent");

                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    //response.append('\r');
                }
                rd.close();
                responseStr = response.toString();
                Log.d("Server response", responseStr);

            }
            catch (Exception e) {
                responseStr = null;
            }

            finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
            return responseStr;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

