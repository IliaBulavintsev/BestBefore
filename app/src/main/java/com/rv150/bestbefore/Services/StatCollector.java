package com.rv150.bestbefore.Services;

import android.content.Context;
import android.provider.Settings;

import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.Network.SendStatistic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Rudnev on 20.10.2016.
 */

public class StatCollector {
    public static void shareStatistic(Context context, String message) {
        ProductDAO productDAO = new ProductDAO(context);
        GroupDAO groupDAO = new GroupDAO(context);
        List<Product> products = productDAO.getAll();
        List<Group> groups = groupDAO.getAll();

        final String deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // Отсылаемый jsonArray
        JSONObject result = new JSONObject();
        try {

            result.put("deviceId", deviceId);



            // Массив свежих продуктов
            JSONArray productsArray = new JSONArray();
            for (Product product : products) {
                JSONObject json = product.getJSON();
                productsArray.put(json);
            }

            // Массив просроченных
            JSONArray groupArray = new JSONArray();
            for (Group group: groups) {
                JSONObject json = group.getJSON();
                groupArray.put(json);
            }

            if (products.isEmpty()) {
                String name = "NO PRODUCTS";
                if (message != null) {
                    name += " (" + message + ")";
                }
                final Product costyl =
                        new Product(name, new GregorianCalendar(), 1, null);
                productsArray.put(costyl.getJSON());
            }

            result.put("products", productsArray);
            result.put("groups", groupArray);
        }
        catch (JSONException e) {
            return;
        }
        new SendStatistic().execute(result.toString());
    }
}