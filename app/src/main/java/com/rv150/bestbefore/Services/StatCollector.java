package com.rv150.bestbefore.Services;

import android.content.Context;
import android.provider.Settings;

import com.rv150.bestbefore.Network.SendStatistic;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Models.Product;

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
        List<Product> freshFood = ProductDAO.getFreshProducts(context);
        List<Product> overdueFood = ProductDAO.getOverdueProducts(context);

        final String deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // Отсылаемый jsonArray
        JSONObject result = new JSONObject();
        try {

            result.put("deviceId", deviceId);



            // Массив свежих продуктов
            JSONArray freshProducts = new JSONArray();
            for (Product item : freshFood) {
                JSONObject json = item.getJSON();
                freshProducts.put(json);
            }

            // Массив просроченных
            JSONArray overdueProducts = new JSONArray();
            for (Product item : overdueFood) {
                JSONObject json = item.getJSON();
                overdueProducts.put(json);
            }

            if (freshFood.isEmpty() && overdueFood.isEmpty()) {
                String name = "NO PRODUCTS";
                if (message != null) {
                    name += " (" + message + ")";
                }
                final Product costyl =
                        new Product(name, new GregorianCalendar(), 1, null);
                freshProducts.put(costyl.getJSON());
            }

            result.put("fresh", freshProducts);
            result.put("overdue", overdueProducts);
        }
        catch (JSONException e) {
            return;
        }
        new SendStatistic().execute(result.toString());
    }
}