package com.rv150.bestbefore.Services;

import android.content.Context;
import android.provider.Settings;

import com.rv150.bestbefore.Network.SendStatistic;
import com.rv150.bestbefore.Preferences.SharedPrefsManager;
import com.rv150.bestbefore.StringWrapper;

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
        List<StringWrapper> freshFood = SharedPrefsManager.getFreshProducts(context);
        List<StringWrapper> overdueFood = SharedPrefsManager.getOverdueProducts(context);

        final String deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // Отсылаемый jsonArray
        JSONObject result = new JSONObject();
        try {

            result.put("deviceId", deviceId);



            // Массив свежих продуктов
            JSONArray freshProducts = new JSONArray();
            for (StringWrapper item : freshFood) {
                JSONObject json = item.getJSON();
                freshProducts.put(json);
            }

            // Массив просроченных
            JSONArray overdueProducts = new JSONArray();
            for (StringWrapper item : overdueFood) {
                JSONObject json = item.getJSON();
                overdueProducts.put(json);
            }

            if (freshFood.isEmpty() && overdueFood.isEmpty()) {
                String name = "NO PRODUCTS";
                if (message != null) {
                    name += " (" + message + ")";
                }
                final StringWrapper costyl =
                        new StringWrapper(name, new GregorianCalendar());
                freshProducts.put(costyl.getJSON());
            }

            result.put("fresh", freshProducts);
            result.put("overdue", overdueProducts);
        }
        catch (JSONException e) {
            return;
        }
        new SendStatistic(context).execute(result.toString());
    }
}
