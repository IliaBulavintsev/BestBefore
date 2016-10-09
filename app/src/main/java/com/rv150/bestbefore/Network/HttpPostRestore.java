package com.rv150.bestbefore.Network;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Preferences.SharedPrefsManager;
import com.rv150.bestbefore.StringWrapper;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rudnev on 05.10.2016.
 */

public class HttpPostRestore extends AsyncTask<String, String, String> {

    private String responseStr = null;
    private Context context;
    private String error;
    private ProgressDialog dialog;

    public HttpPostRestore(Context context) {
        this.context = context;
        error =  context.getString(R.string.restore_failed);
        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage(context.getString(R.string.fetching_data));
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        String dataUrl = Resources.SERVER_URL + "restore";
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
        dialog.hide();

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

        JSONArray freshJson;
        JSONArray overdueJson;
        final List<StringWrapper> fresh = new ArrayList<>();
        final List<StringWrapper> overdue = new ArrayList<>();
        try {
            final JSONObject inputJson = new JSONObject(input);
            freshJson = inputJson.getJSONArray("fresh");
            overdueJson = inputJson.getJSONArray("overdue");

            // Заполнение списков...
            for (int i = 0; i < freshJson.length(); ++i) {
                JSONObject item = freshJson.getJSONObject(i);
                String name = item.getString("name");
                String date = item.getString("date");
                String createdAt = item.getString("getCreatedAt");
                StringWrapper product = new StringWrapper(name, date, createdAt);
                fresh.add(product);
            }

            // Поле getCreatedAt передается по сети, но не имеет смысла
            for (int i = 0; i < overdueJson.length(); ++i) {
                JSONObject item = overdueJson.getJSONObject(i);
                String name = item.getString("name");
                String date = item.getString("date");
                StringWrapper product = new StringWrapper(name, date);
                overdue.add(product);
            }
        }
        catch (JSONException e) {
            Toast toast = Toast.makeText(context,
                    R.string.restore_failed, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }


        List<StringWrapper> currentFresh = SharedPrefsManager.getFreshProducts(context);
        List<StringWrapper> currentOverdue = SharedPrefsManager.getOverdueProducts(context);
        if (!currentFresh.isEmpty() || !currentOverdue.isEmpty()) {
            new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.do_you_want_to_overwite_existing)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveRestored(fresh, overdue);
                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
        saveRestored(fresh, overdue);
    }

    private void saveRestored(List<StringWrapper> fresh, List<StringWrapper> overdue) {
        SharedPrefsManager.saveFreshProducts(fresh, context);
        SharedPrefsManager.saveOverdueProducts(overdue, context);
        Toast toast = Toast.makeText(context,
                R.string.restore_success, Toast.LENGTH_SHORT);
        toast.show();
    }
}
