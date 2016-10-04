package com.rv150.bestbefore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

/**
 * Created by rv150 on 06.01.2016.
 */
class StringWrapper {
    private String mTitle;
    private Calendar mDate;
    private Calendar mCreatedAt;


    StringWrapper(String mTitle, Calendar mDate) {
        this.mTitle = mTitle;
        this.mDate = mDate;
        this.mCreatedAt = new GregorianCalendar();
    }

    StringWrapper(String mTitle, Calendar mDate, Calendar mCreatedAt) {
        this.mTitle = mTitle;
        this.mDate = mDate;
        this.mCreatedAt = mCreatedAt;
    }


    String getTitle() {
        return mTitle;
    }

    void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    Calendar getDate() {
        return mDate;
    }

    Calendar createdAt() { return mCreatedAt; }

    void setDate(Calendar mDate) {
        this.mDate = mDate;
    }

    JSONObject getJSON() throws JSONException {
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




    static Comparator<StringWrapper> getFreshToSpoiledComparator() {
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

    static Comparator<StringWrapper> getSpoiledToFreshComparator() {
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

    static Comparator<StringWrapper> getStandartComparator() {
        return new Comparator<StringWrapper>() {
            public int compare(StringWrapper one, StringWrapper two) {
                if (one.createdAt().before(two.createdAt())) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        };
    }
}

