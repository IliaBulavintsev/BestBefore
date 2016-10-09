package com.rv150.bestbefore;

import android.content.Context;

import com.rv150.bestbefore.Preferences.SharedPrefsManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ivan on 06.07.2016.
 * Класс получает на вход список и переносит просроченные в отдельную коллекцию
 */
public class DeleteOverdue {
    public static List<String> delete(Context context, List<StringWrapper> wrapperList) {
        List<StringWrapper> overdued = SharedPrefsManager.getOverdueProducts(context);
        List<String> newOverdue = new ArrayList<>();

            for (Iterator<StringWrapper> iterator = wrapperList.iterator(); iterator.hasNext(); ) {
                StringWrapper currentItem = iterator.next();
                Calendar date = currentItem.getDate();
                date.set(Calendar.HOUR_OF_DAY, 23);
                date.set(Calendar.MINUTE, 59);
                Calendar currentDate = new GregorianCalendar();
                long difference = currentDate.getTimeInMillis() - date.getTimeInMillis();
                if (difference > 0) {
                    overdued.add(0, currentItem); // Кладем в overdued все просроченные продукты
                    int days = (int) (difference / (1000 * 60 * 60 * 24));
                    String title = currentItem.getTitle();
                    if (days == 0) {
                        title += " (сегодня)";
                    }
                    else
                    if (days == 1) {
                        title += " (вчера)";
                    }
                    else
                    if ((days >= 10) && (days <= 20)) {
                        title += " (" + days + " дней назад)";
                    }
                    else if ((days % 10 >= 2) && (days % 10 <= 4)) {
                        title += " (" + days + " дня назад)";
                    }
                    else if (days % 10 == 1) {
                        title += " (" + days + " день назад)";
                    }
                    else {
                        title += " (" + days + " дней назад)";
                    }


                    newOverdue.add(0, title); // А в новые просроки название
                    iterator.remove();  // И удаляем из основого списка
                }
            }
        SharedPrefsManager.saveOverdueProducts(overdued, context);
        // Сoхраняем просроченные, а wrapperList сохранится в MainActivity
        return newOverdue;
    }
}
