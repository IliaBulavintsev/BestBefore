package com.rv150.bestbefore.Network;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Resources;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rudnev on 05.10.2016.
 */

public class HttpPostRestore extends AsyncTask<String, String, String> {

    private String responseStr = null;
    private Context context;
    private String error;
    private ProgressDialog dialog;
    private ProductDAO productDAO;

    public HttpPostRestore(Context context) {
        this.context = context;
        error =  context.getString(R.string.restore_failed);
        dialog = new ProgressDialog(context);
        productDAO = new ProductDAO(context);
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

        JSONArray productArray;
        JSONArray groupArray;
        final List<Product> products = new ArrayList<>();
        final List<Group> groups = new ArrayList<>();
        try {
            final JSONObject inputJson = new JSONObject(input);
            productArray = inputJson.getJSONArray("products");
            groupArray = inputJson.getJSONArray("groups");

            // Заполнение списков...
            for (int i = 0; i < productArray.length(); ++i) {
                JSONObject item = productArray.getJSONObject(i);
                final String name = item.getString("name");
                final long dateInMillis = item.getLong("date");
                final long createdAtInMillis = item.getLong("createdAt");
                final int quantity = item.getInt("quantity");
                long groupId = item.getLong("groupId");
                final int viewed = item.getInt("viewed");
                int removed = item.getInt("removed");
                long removedAt = item.getLong("removedAt");
                int measure = item.getInt("measure");
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(dateInMillis);
                // тк на сервере могут лежать старые данные еще до миграции, то
                date.set(Calendar.HOUR_OF_DAY, 23);
                date.set(Calendar.MINUTE, 59);

                Calendar createdAt = Calendar.getInstance();
                createdAt.setTimeInMillis(createdAtInMillis);
                Product product = new Product(name, date, createdAt, quantity, groupId);
                product.setViewed(viewed);
                product.setMeasure(measure);
                product.setRemoved(removed);
                product.setRemovedAt(removedAt);
                products.add(product);
            }


            for (int i = 0; i < groupArray.length(); ++i) {
                JSONObject item = groupArray.getJSONObject(i);
                final String name = item.getString("name");
                final long oldId = item.getLong("id");
                Group group = new Group(name);
                group.setId(oldId);
                groups.add(group);
            }
        }
        catch (JSONException e) {
            Toast toast = Toast.makeText(context,
                    R.string.restore_failed, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }



        List<Product> currentProducts = productDAO.getAll();
        if (!currentProducts.isEmpty()) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.do_you_want_to_overwite_existing)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveRestored(products, groups);
                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
        else {
            saveRestored(products, groups);
        }
    }

    private void saveRestored(List<Product> products, List<Group> groups) {
        Map<Long, Long> oldIdToNew = new HashMap<>(); // Старые ID к новым для правильных внешних ключей
        GroupDAO groupDAO = new GroupDAO(context);

        productDAO.deleteAll();
        groupDAO.deleteAll();

        for (Group group: groups) {
            long oldId = group.getId();
            long newId = groupDAO.insertGroup(group);
            oldIdToNew.put(oldId, newId);
        }

        for (Product product: products) {
            long oldGroupId = product.getGroupId();
            if (oldGroupId != -1) {
                long newGroupId =  oldIdToNew.get(oldGroupId);
                product.setGroupId(newGroupId);
            }
            productDAO.insertProduct(product);
        }

        Toast toast = Toast.makeText(context,
                R.string.restore_success, Toast.LENGTH_SHORT);
        toast.show();
    }
}
