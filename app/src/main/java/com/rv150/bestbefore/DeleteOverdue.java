package com.rv150.bestbefore;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.Services.DBHelper;

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
    public static List<String> getOverdueNamesAndRemoveFresh(List<Product> wrapperList) {
        List<String> newOverdue = new ArrayList<>();
        for (Iterator<Product> iterator = wrapperList.iterator(); iterator.hasNext(); ) {
            Product currentItem = iterator.next();
            Calendar date = currentItem.getDate();
            Calendar currentDate = new GregorianCalendar();
            Calendar createdAt = currentItem.getCreatedAt();
            int viewed = currentItem.getViewed();
            long difference = currentDate.getTimeInMillis() - date.getTimeInMillis();
            if (difference > 0 && viewed == 0) {    // Уже просмотренным продуктам
                int days = (int) (difference / (1000 * 60 * 60 * 24));    // присвоим viewed = 1
                String title = currentItem.getTitle();
                if (days == 0) {
                    title += " (сегодня)";
                } else if (days == 1) {
                    title += " (вчера)";
                } else if ((days >= 10) && (days <= 20)) {
                    title += " (" + days + " дней назад)";
                } else if ((days % 10 >= 2) && (days % 10 <= 4)) {
                    title += " (" + days + " дня назад)";
                } else if (days % 10 == 1) {
                    title += " (" + days + " день назад)";
                } else {
                    title += " (" + days + " дней назад)";
                }
                newOverdue.add(0, title); // А в новые просроки название
            }
            else {
                iterator.remove();
            }
        }
        return newOverdue;
    }

    public static void markViewed(ProductDAO productDAO, List<Product> wrapperList) {
        for (Product product : wrapperList) {
            Calendar date = product.getDate();
            int viewed = product.getViewed();
            long difference = new GregorianCalendar().getTimeInMillis() - date.getTimeInMillis();
            if (difference > 0 && viewed == 0) {
                product.setViewed(1);
                productDAO.updateProduct(product);
            }
        }
    }

    public static void removeOverduedFromList(List<Product> wrapperList) {
        for (Iterator<Product> iterator = wrapperList.iterator(); iterator.hasNext(); ) {
            Product currentItem = iterator.next();
            Calendar date = currentItem.getDate();
            Calendar createdAt = currentItem.getCreatedAt();
            long difference = new GregorianCalendar().getTimeInMillis() - date.getTimeInMillis();
            if (difference > 0) {
                iterator.remove();
            }
        }
    }
}