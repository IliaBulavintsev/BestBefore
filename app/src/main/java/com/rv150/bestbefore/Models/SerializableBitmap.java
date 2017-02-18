package com.rv150.bestbefore.Models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by ivan on 18.02.17.
 */

public class SerializableBitmap implements Serializable {
    private static final long serialVersionUID = 1710387017035522910L;
    private Bitmap bitmap;

    public SerializableBitmap() {
    }

    public SerializableBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private class BitmapDataObject implements Serializable {
        private static final long serialVersionUID = 111696345129311948L;
        byte[] imageByteArray;
    }

    /**
     * Included for serialization - write this layer to the output stream.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        BitmapDataObject bitmapDataObject = new BitmapDataObject();
        bitmapDataObject.imageByteArray = stream.toByteArray();
        out.writeObject(bitmapDataObject);
    }

    /**
     * Included for serialization - read this object from the supplied input stream.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        BitmapDataObject bitmapDataObject = (BitmapDataObject) in.readObject();
        bitmap = BitmapFactory.decodeByteArray(bitmapDataObject.imageByteArray, 0, bitmapDataObject.imageByteArray.length);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}


