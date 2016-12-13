package com.rv150.bestbefore.Models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

/**
 * Created by rv150 on 06.01.2016.
 */
public class Product implements Parcelable {
    private String mTitle;
    private Calendar mDate;
    private Calendar mCreatedAt;
    private int mQuantity;
    private long mGroupId = -1;
    private long mId;
    private int mViewed = 0;
    private int mRemoved;
    private long mRemovedAt;

    public Product() {
        mDate = mCreatedAt = Calendar.getInstance();
    }

    public Product(String title, Calendar date, int quantity, long groupId) {
        this(title, date, new GregorianCalendar(), quantity, groupId);
    }

    public Product(String title, Calendar date, Calendar createdAt, int quantity, long groupId) {
        this.mTitle = title;
        this.mDate = date;
        this.mCreatedAt = createdAt;
        this.mQuantity = quantity;
        this.mGroupId = groupId;
    }

    public Product(String title, String date, int quantity, long groupId) {
        this(title, date, null, quantity, groupId);
    }

    public Product(String title, String date, String createdAt, int quantity, long groupId) {
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
        this.mGroupId = groupId;
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeLong(mDate.getTimeInMillis());
        parcel.writeLong(mCreatedAt.getTimeInMillis());
        parcel.writeInt(mQuantity);
        parcel.writeLong(mGroupId);
        parcel.writeLong(mId);
        parcel.writeInt(mViewed);
        parcel.writeInt(mRemoved);
        parcel.writeLong(mRemovedAt);
    }

    public static final Parcelable.Creator<Product> CREATOR
            = new Parcelable.Creator<Product>() {
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    private Product(Parcel in) {
        mTitle = in.readString();
        mDate = Calendar.getInstance();
        mDate.setTimeInMillis(in.readLong());
        mCreatedAt = Calendar.getInstance();
        mCreatedAt.setTimeInMillis(in.readLong());
        mQuantity = in.readInt();
        mGroupId = in.readLong();
        mId = in.readLong();
        mViewed = in.readInt();
        mRemoved = in.readInt();
        mRemovedAt = in.readLong();
    }




    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
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
        result.put("groupId", mGroupId);
        result.put("viewed", getViewed());
        return result;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public void setQuantity(int quantity) {
        this.mQuantity = quantity;
    }

    public long getGroupId() {
        return mGroupId;
    }

    public void setGroupId(long groupId) {
        this.mGroupId = groupId;
    }

    public int getViewed() {
        return mViewed;
    }

    public void setViewed(int viewed) {
        this.mViewed = viewed;
    }

    public int getRemoved() {
        return mRemoved;
    }

    public void setRemoved(int removed) {
        this.mRemoved = removed;
    }

    public long getRemovedAt() {
        return mRemovedAt;
    }

    public void setRemovedAt(long removedAt) {
        this.mRemovedAt = removedAt;
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

