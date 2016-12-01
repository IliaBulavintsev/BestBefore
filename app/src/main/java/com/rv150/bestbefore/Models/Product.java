package com.rv150.bestbefore.Models;

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
    private int mQuantity;
    private Long groupId;
    private long id;
    private int viewed = 0;


    public Product(String title, Calendar date, int quantity, Long groupId) {
        this(title, date, new GregorianCalendar(), quantity, groupId);
    }

    public Product(String title, Calendar date, Calendar createdAt, int quantity, Long groupId) {
        this.mTitle = title;
        this.mDate = date;
        this.mCreatedAt = createdAt;
        this.mQuantity = quantity;
        this.groupId = groupId;
    }

    public Product(String title, String date, int quantity, Long groupId) {
        this(title, date, null, quantity, groupId);
    }

    public Product(String title, String date, String createdAt, int quantity, Long groupId) {
        this.mTitle = title;
        String[] array = date.split("\\.");
        int myDay = Integer.parseInt(array[0]);
        int myMonth = Integer.parseInt(array[1]);
        int myYear = Integer.parseInt(array[2]);
        this.mDate = new GregorianCalendar(myYear, myMonth, myDay, 23, 59);

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
        this.groupId = groupId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Calendar getCreatedAt() { return mCreatedAt; }

    public void setDate(Calendar mDate) {
        this.mDate = mDate;
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("name", getTitle());
        result.put("date", getDate().getTimeInMillis());
        result.put("createdAt", getCreatedAt().getTimeInMillis());
        result.put("quantity", getQuantity());
        if (groupId == null) {
            result.put("groupId", -1);
        }
        else {
            result.put("groupId", groupId);
        }
        result.put("viewed", getViewed());
        return result;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public void setQuantity(int quantity) {
        this.mQuantity = quantity;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public int getViewed() {
        return viewed;
    }

    public void setViewed(int viewed) {
        this.viewed = viewed;
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

