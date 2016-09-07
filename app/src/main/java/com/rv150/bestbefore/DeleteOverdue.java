package com.rv150.bestbefore;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Ivan on 06.07.2016.
 * Класс получает на вход список и удаляет элементы, просроченные более чем на один день
 */
public class DeleteOverdue {
    private List<StringWrapper> wrapperList;
    private List<StringWrapper> deleted;
    private SharedPreferences sPrefs;

    private Queue<String> queue;

    DeleteOverdue(List<StringWrapper> list, Context context) {
        wrapperList = list;
        deleted = new ArrayList<>();
        queue = new LinkedList<>();
        sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public List<String> delete() {
        loadDeleted(); // Подгружаем ранее просроченные продукты
        List<String> newOverdue = new ArrayList<>();
            for (Iterator<StringWrapper> iterator = wrapperList.iterator(); iterator.hasNext(); ) {
                StringWrapper currentItem = iterator.next();
                Calendar date = currentItem.getDate();
                date.set(Calendar.HOUR_OF_DAY, 23);
                date.set(Calendar.MINUTE, 59);
                Calendar currentDate = new GregorianCalendar();
                long difference = currentDate.getTimeInMillis() - date.getTimeInMillis();
                if (difference > 0) {
                    deleted.add(currentItem); // Кладем в deleted все просроченные продукты
                    newOverdue.add(currentItem.getTitle()); // А в новые просроки название
                    iterator.remove();  // И удаляем из основого списка
                }
            }
        saveDeleted(); // Сoхраняем просроченные, а wrapperList сохранится в MainActivity
        return newOverdue;
    }

    private void loadDeleted() {
        deleted.clear();
        for (int i = 0; sPrefs.contains("del" + String.valueOf(i)); ++i) {
            if (sPrefs.getString("del" + String.valueOf(i), "").equals("") || i >= 1000) {
                break;
            }
            final String title = sPrefs.getString("del" + String.valueOf(i), "");
            final String date = sPrefs.getString("del" + String.valueOf(i + 1000), "0.0.0");
            String[] array = date.split("\\.");
            int myDay = Integer.parseInt(array[0]);
            int myMonth = Integer.parseInt(array[1]);
            int myYear = Integer.parseInt(array[2]);

            StringWrapper temp = new StringWrapper(title, new GregorianCalendar(myYear, myMonth, myDay));
            deleted.add(temp);
        }
    }



    private void saveDeleted() {
        SharedPreferences.Editor editor = sPrefs.edit();
            for (int i = 0; i < deleted.size(); ++i) {
                Calendar temp = deleted.get(i).getDate();
                int myYear = temp.get(Calendar.YEAR);
                int myMonth = temp.get(Calendar.MONTH);
                int myDay = temp.get(Calendar.DAY_OF_MONTH);
                String str;
                if (myMonth < 9) {
                    str = myDay + "." + "0" + myMonth + "." + myYear;
                } else {
                    str = myDay + "." + myMonth + "." + myYear;
                }
                editor.putString("del" + String.valueOf(i), deleted.get(i).getTitle());
                editor.putString("del" + String.valueOf(i + 1000), str);
            }
            editor.putString("del" + String.valueOf(deleted.size()), ""); // признак конца списка
            editor.apply();
        }
}
