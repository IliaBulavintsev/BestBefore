package com.rv150.bestbefore;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.style.StyleSpan;

import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Models.Product;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ivan on 06.07.2016.
 * Класс получает на вход список и переносит просроченные в отдельную коллекцию
 */
public class DeleteOverdue {
    public static List<String> getOverdueNamesAndRemoveFresh(List<Product> wrapperList) {
        Collections.sort(wrapperList, Product.getFreshToSpoiledComparator());
        List<String> newOverdue = new ArrayList<>();
        for (Iterator<Product> iterator = wrapperList.iterator(); iterator.hasNext(); ) {
            Product currentItem = iterator.next();
            Calendar date = currentItem.getDate();
            Calendar currentDate = new GregorianCalendar();
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
                newOverdue.add(title); // А в новые просроки название
            }
            else {
                iterator.remove();
            }
        }
        return newOverdue;
    }

    public static SpannedString bold(String text, String boldText) {
        int start = text.length() + 1;

        SpannableStringBuilder ssb = new SpannableStringBuilder(text + " " + boldText);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        ssb.setSpan(boldSpan, start, start + boldText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return new SpannedString(ssb);
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