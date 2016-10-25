package com.rv150.bestbefore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

/**
 * Created by rv150 on 06.01.2016.
 */
public class StringWrapper {
    private String mTitle;
    private Calendar mDate;
    private Calendar mCreatedAt;
    private int quantity = 1;


    public  StringWrapper(String mTitle, Calendar mDate) {
        this(mTitle, mDate, new GregorianCalendar());
    }

    public StringWrapper(String mTitle, Calendar mDate, Calendar mCreatedAt) {
        this.mTitle = mTitle;
        this.mDate = mDate;
        this.mCreatedAt = mCreatedAt;
    }

    public  StringWrapper (String title, String date) {
        this(title, date, null);
    }

    public StringWrapper (String title, String date, String createdAt) {
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

        result.put("name", mTitle);

        int myYear = mDate.get(Calendar.YEAR);
        int myMonth = mDate.get(Calendar.MONTH);
        int myDay = mDate.get(Calendar.DAY_OF_MONTH);
        String str;
        if (myMonth < 9) {
            str = myDay + "." + "0" + myMonth + "." + myYear;
        } else {
            str = myDay + "." + myMonth + "." + myYear;
        }
        result.put("date", str);

        int DayCreated =  mCreatedAt.get(Calendar.DAY_OF_MONTH);
        int MonthCreated = mCreatedAt.get(Calendar.MONTH);
        int YearCreated = mCreatedAt.get(Calendar.YEAR);
        int HourCreated = mCreatedAt.get(Calendar.HOUR_OF_DAY);
        int MinuteCreated = mCreatedAt.get(Calendar.MINUTE);
        int SecondCreated = mCreatedAt.get(Calendar.SECOND);
        String createdAtStr = YearCreated + "." + MonthCreated + "." + DayCreated  + "." + HourCreated + "." + MinuteCreated + "." + SecondCreated;
        result.put("createdAt", createdAtStr);
        return result;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public static Comparator<StringWrapper> getFreshToSpoiledComparator() {
        return new Comparator<StringWrapper>() {
            public int compare(StringWrapper one, StringWrapper two) {
                if (one.getDate().before(two.getDate())) {
                    return 1;
                }
                else {
                    return -1;
                }
            }
        };
    }

    public static Comparator<StringWrapper> getSpoiledToFreshComparator() {
        return new Comparator<StringWrapper>() {
            public int compare(StringWrapper one, StringWrapper two) {
                if (one.getDate().before(two.getDate())) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        };
    }

    public static Comparator<StringWrapper> getStandartComparator() {
        return new Comparator<StringWrapper>() {
            public int compare(StringWrapper one, StringWrapper two) {
                if (one.getCreatedAt().before(two.getCreatedAt())) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        };
    }
}

