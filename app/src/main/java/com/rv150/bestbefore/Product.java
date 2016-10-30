package com.rv150.bestbefore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

/**
 * Created by rv150 on 06.01.2016.
 */
public class Product {
    private String mTitle;
    private Calendar mDate;
    private Calendar mCreatedAt;
    final private int mQuantity;


    public Product(String mTitle, Calendar mDate, int quantity) {
        this(mTitle, mDate, new GregorianCalendar(), quantity);
    }

    public Product(String title, Calendar date, Calendar createdAt, int quantity) {
        this.mTitle = title;
        this.mDate = date;
        this.mCreatedAt = createdAt;
        this.mQuantity = quantity;
    }

    public Product(String title, String date, int quantity) {
        this(title, date, null, quantity);
    }

    public Product(String title, String date, String createdAt, int quantity) {
        this.mTitle = title;
        String[] array = date.split("\\.");
        int myDay = Integer.parseInt(array[0]);
        int myMonth = Integer.parseInt(array[1]);
        int myYear = Integer.parseInt(array[2]);
        this.mDate = new GregorianCalendar(myYear, myMonth, myDay);

        if (createdAt == null) {
            this.mCreatedAt = new GregorianCalendar();
        }
        else {
            String[] createdAtSplit = createdAt.split("\\.");
            int Year = Integer.parseInt(createdAtSplit[0]);
            int Month = Integer.parseInt(createdAtSplit[1]);
            int Day = Integer.parseInt(createdAtSplit[2]);
            int Hour = Integer.parseInt(createdAtSplit[3]);
            int Minute = Integer.parseInt(createdAtSplit[4]);
            int Second = Integer.parseInt(createdAtSplit[5]);
            this.mCreatedAt = new GregorianCalendar
                    (Year, Month, Day, Hour, Minute, Second);
        }
        this.mQuantity = quantity;
    }


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public Calendar getDate() {
        return mDate;
    }

    public String getDateStr() {
        int myYear = mDate.get(Calendar.YEAR);
        int myMonth = mDate.get(Calendar.MONTH);
        int myDay = mDate.get(Calendar.DAY_OF_MONTH);
        if (myMonth < 9) {
            return myDay + "." + "0" + myMonth + "." + myYear;
        } else {
            return myDay + "." + myMonth + "." + myYear;
        }
    }

    public Calendar getCreatedAt() { return mCreatedAt; }

    public String getCreatedAtStr() {
        int DayCreated =  mCreatedAt.get(Calendar.DAY_OF_MONTH);
        int MonthCreated = mCreatedAt.get(Calendar.MONTH);
        int YearCreated = mCreatedAt.get(Calendar.YEAR);
        int HourCreated = mCreatedAt.get(Calendar.HOUR_OF_DAY);
        int MinuteCreated = mCreatedAt.get(Calendar.MINUTE);
        int SecondCreated = mCreatedAt.get(Calendar.SECOND);
        return YearCreated + "." + MonthCreated + "." + DayCreated  + "."
                + HourCreated + "." + MinuteCreated + "." + SecondCreated;
    }


    public void setDate(Calendar mDate) {
        this.mDate = mDate;
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("name", getTitle());
        result.put("date", getDateStr());
        result.put("createdAt", getCreatedAtStr());
        result.put("quantity", getQuantity());
        return result;
    }

    public int getQuantity() {
        return mQuantity;
    }


    public static Comparator<Product> getFreshToSpoiledComparator() {
        return new Comparator<Product>() {
            public int compare(Product one, Product two) {
                if (one.getDate().before(two.getDate())) {
                    return 1;
                }
                else {
                    return -1;
                }
            }
        };
    }

    public static Comparator<Product> getSpoiledToFreshComparator() {
        return new Comparator<Product>() {
            public int compare(Product one, Product two) {
                if (one.getDate().before(two.getDate())) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        };
    }

    public static Comparator<Product> getStandartComparator() {
        return new Comparator<Product>() {
            public int compare(Product one, Product two) {
                if (one.getCreatedAt().before(two.getCreatedAt())) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        };
    }

    public static Comparator<Product> getByNameComparator() {
        return new Comparator<Product>() {
            public int compare(Product one, Product two) {
                return one.getTitle().compareTo(two.getTitle());
            }
        };
    }
}

