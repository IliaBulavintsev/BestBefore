package com.rv150.bestbefore;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Rudnev on 05.10.2016.
 */

class HttpPostRestore extends AsyncTask<String, String, String> {

    private String responseStr = null;
    private Context context;
    private String error;

    HttpPostRestore(Context context) {
        this.context = context;
        error =  context.getString(R.string.restore_failed);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String dataUrl = "http://192.168.43.244:8080/restore";
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
            connection.setConnectTimeout(5000);


// Send request

            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(URLEncoder.encode(params[0], "UTF-8"));
            wr.flush();
            wr.close();
// Get Response
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
        catch (SocketTimeoutException e) {
            error = context.getString(R.string.error_timeout);
        }
        catch (IOException e) {
            error =  context.getString(R.string.restore_failed);
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

        if (responseStr == null) {
            Toast toast = Toast.makeText(context,
                    error, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (responseStr.equals("no data")) {
            Toast toast = Toast.makeText(context,
                    R.string.data_not_found, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        parseResult(responseStr);
    }



    private void parseResult (String input) {
        try {
            final JSONObject inputJson = new JSONObject(input);
            JSONArray fresh = inputJson.getJSONArray("fresh");
            JSONArray overdue = inputJson.getJSONArray("overdue");



        }





        Toast toast = Toast.makeText(context,
                R.string.restore_success, Toast.LENGTH_SHORT);
        toast.show();
    }
}
