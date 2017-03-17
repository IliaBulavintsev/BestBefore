package com.rv150.bestbefore.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

/**
 * Created by rv150 on 06.01.2016.
 */
public class Product implements Parcelable, Serializable {
    private static final long serialVersionUID = -305914420954100092L;
    private String mTitle;
    private Calendar mProduced;
    private Calendar mDate;
    private Calendar mCreatedAt;
    private int mQuantity;
    private long mGroupId = -1;
    private long mId;
    private int mViewed = 0;
    private int mRemoved;
    private long mRemovedAt;
    private int measure;
    private long photo;     // Имя файла с фотографией + .jpeg



    public Product() {
        mTitle = "";
        mDate = Calendar.getInstance();
        mCreatedAt = Calendar.getInstance();
        mProduced = Calendar.getInstance();
        mProduced.setTimeInMillis(0);
    }


    public Product(String title, Calendar date, Calendar createdAt, int quantity, long groupId) {
        if (title != null) {
            this.mTitle = title;
        }
        else {
            mTitle = "";
        }
        this.mDate = date;
        this.mCreatedAt = createdAt;
        this.mQuantity = quantity;
        this.mGroupId = groupId;
        mProduced = Calendar.getInstance();
        mProduced.setTimeInMillis(0);
    }

    public Product(String title, String date, int quantity, long groupId) {
        this(title, date, null, quantity, groupId);
    }

    public Product(String title, String date, String createdAt, int quantity, long groupId) {
        if (title != null) {
            this.mTitle = title;
        }
        else {
            mTitle = "";
        }
        String[] array = date.split("\\.");
        int myDay = Integer.parseInt(array[0]);
        int myMonth = Integer.parseInt(array[1]);
        int myYear = Integer.parseInt(array[2]);
        this.mDate = new GregorianCalendar(myYear, myMonth, myDay, 23, 59);

        this.mCreatedAt = Calendar.getInstance();

        if (createdAt != null) {
            String[] createdAtSplit = createdAt.split("\\.");
            int year = Integer.parseInt(createdAtSplit[0]);
            int month = Integer.parseInt(createdAtSplit[1]);
            int day = Integer.parseInt(createdAtSplit[2]);
            int hour = Integer.parseInt(createdAtSplit[3]);
            int minute = Integer.parseInt(createdAtSplit[4]);
            int second = Integer.parseInt(createdAtSplit[5]);
            mCreatedAt.set(year, month, day, hour, minute, second);
        }
        this.mQuantity = quantity;
        this.mGroupId = groupId;
        mProduced = Calendar.getInstance();
        mProduced.setTimeInMillis(0);
    }



    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeUTF(mTitle);
        oos.writeLong(mDate.getTimeInMillis());
        oos.writeLong(mCreatedAt.getTimeInMillis());
        oos.writeInt(mQuantity);
        oos.writeLong(mGroupId);
        oos.writeLong(mId);
        oos.writeInt(mViewed);
        oos.writeInt(mRemoved);
        oos.writeLong(mRemovedAt);
        oos.writeLong(mProduced.getTimeInMillis());
        oos.writeInt(measure);
        oos.writeLong(photo);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        mTitle = in.readUTF();
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
        mProduced = Calendar.getInstance();
        mProduced.setTimeInMillis(in.readLong());
        measure = in.readInt();
        try {
            photo = in.readLong();
        }
        catch (IOException e) {
            photo = 0;
        }
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
        parcel.writeLong(mProduced.getTimeInMillis());
        parcel.writeInt(measure);
        parcel.writeLong(photo);
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
        mProduced = Calendar.getInstance();
        mProduced.setTimeInMillis(in.readLong());
        measure = in.readInt();
        photo = in.readLong();
    }


    public boolean isFresh() {
        Calendar now = Calendar.getInstance();
        return now.before(mDate);
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

    public void setTitle(String title) {
        if (title == null) {
            mTitle = "";
        }
        else {
            mTitle = title;
        }
    }

    public Calendar getProduced() {
        return mProduced;
    }

    public void setProduced(Calendar produced) {
        this.mProduced = produced;
    }

    public Calendar getDate() {
        return mDate;
    }

    public Calendar getCreatedAt() { return mCreatedAt; }

    public void setDate(Calendar mDate) {
        this.mDate = mDate;
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

    public int getMeasure() {
        return measure;
    }

    public void setMeasure(int measure) {
        this.measure = measure;
    }

    public long getPhoto() {
        return photo;
    }

    public void setPhoto(long photo) {
        this.photo = photo;
    }

    public static Comparator<Product> getFreshToSpoiledComparator() {
        return new Comparator<Product>() {
            public int compare(Product one, Product two) {
                if (one.getDate().before(two.getDate())) {
                    return 1;
                }
                else if (one.getDate().after(two.getDate())) {
                    return -1;
                }
                else {
                    return 0;
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
                else if (one.getDate().after(two.getDate())) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
        };
    }

    public static Comparator<Product> getStandartComparator() {
        return new Comparator<Product>() {
            public int compare(Product one, Product two) {
                if (one.getCreatedAt().before(two.getCreatedAt())) {
                    return 1;
                }
                else if (one.getCreatedAt().after(two.getCreatedAt())) {
                    return -1;
                }
                else {
                    return 0;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        if (mQuantity != product.mQuantity) return false;
        if (mGroupId != product.mGroupId) return false;
        if (mId != product.mId) return false;
        if (mViewed != product.mViewed) return false;
        if (mRemoved != product.mRemoved) return false;
        if (mRemovedAt != product.mRemovedAt) return false;
        if (measure != product.measure) return false;
        if (photo != product.photo) return false;
        if (!mTitle.equals(product.mTitle)) return false;
        if (!mProduced.equals(product.mProduced)) return false;
        if (!mDate.equals(product.mDate)) return false;
        return mCreatedAt.equals(product.mCreatedAt);

    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }
}

