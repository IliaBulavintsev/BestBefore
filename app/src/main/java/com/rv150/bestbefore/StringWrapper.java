package com.rv150.bestbefore;

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


    public StringWrapper(String mTitle, Calendar mDate) {
        this.mTitle = mTitle;
        this.mDate = mDate;
        this.mCreatedAt = new GregorianCalendar();
    }

    public StringWrapper(String mTitle, Calendar mDate, Calendar mCreatedAt) {
        this.mTitle = mTitle;
        this.mDate = mDate;
        this.mCreatedAt = mCreatedAt;
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

    public Calendar createdAt() { return mCreatedAt; }

    public void setDate(Calendar mDate) {
        this.mDate = mDate;
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

