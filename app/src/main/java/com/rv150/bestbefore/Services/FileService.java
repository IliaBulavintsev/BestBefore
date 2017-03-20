package com.rv150.bestbefore.Services;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.rv150.bestbefore.Activities.Preferences;
import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Exceptions.DuplicateEntryException;
import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.Models.SerializableBitmap;
import com.rv150.bestbefore.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ivan on 13.02.17.
 */

public class FileService {

    private static final String TAG = FileService.class.getSimpleName();

    public static void readFromFile(final Context context, Intent intent) {
        Uri uri = intent.getData();
        File file = new File(uri.getPath());
        if (!file.exists()) {
            Toast toast = Toast.makeText(context,
                    R.string.file_open_error, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        try {
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            final Map<String, Object> map;
            try {
                 map = (Map) objectInputStream.readObject();
            }
            catch (OutOfMemoryError ex) {
                Toast.makeText(context, R.string.out_of_memory_try_use_less_photos, Toast.LENGTH_LONG).show();
                return;
            }
            finally {
                try {
                    objectInputStream.close();
                }
                catch (IOException ex) {
                    Log.e(TAG, "Error closing stream");
                }
            }

            ProductDAO productDAO = ProductDAO.getInstance(context);

            List<Product> currentProducts = productDAO.getAll();
            if (!currentProducts.isEmpty()) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.warning)
                        .setMessage(R.string.do_you_want_to_overwrite_existing_from_file)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new SavingMapToDB(context, map, false).execute(context.getString(R.string.import_success));
                            }

                        })
                        .setNeutralButton(R.string.combine, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new SavingMapToDB(context, map, true).execute(context.getString(R.string.import_success));
                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
            else {
                new SavingMapToDB(context, map, false).execute(context.getString(R.string.import_success));
            }
        }
        catch (Exception e) {
            Toast toast = Toast.makeText(context,
                    R.string.internal_error_has_occured, Toast.LENGTH_SHORT);
            toast.show();
        }
    }





    public static class SavingMapToDB extends AsyncTask<String, Void, Void> {
        private Map<String, Object> map;
        private ProgressDialog dialog;
        private Context mContext;
        private String message;

        private final boolean mUnion;


        public SavingMapToDB(Context context, Map<String, Object> map, boolean union) {
            this.map = map;
            this.mContext = context;
            this.mUnion = union;
            dialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage(mContext.getString(R.string.import_process));
            dialog.setCancelable(false);
            dialog.setInverseBackgroundForced(false);
            dialog.show();
        }


        @Override
        protected Void doInBackground(String... params) {
            try {
                message = params[0];
                ProductDAO productDAO = ProductDAO.getInstance(mContext);
                GroupDAO groupDAO = GroupDAO.getInstance(mContext);

                List<Product> products = (List) map.get("products");
                List<Group> groups = (List) map.get("groups");

                Map<Long, Long> oldIdToNew = new HashMap<>(); // Старые ID к новым для правильных внешних ключей

                if (!mUnion) {
                    productDAO.deleteAll();
                    groupDAO.deleteAll();
                }

                if (groups != null) {
                    for (Group group : groups) {
                        long oldId = group.getId();
                        long newId;
                        try {
                            newId = groupDAO.insertGroup(group);
                        } catch (DuplicateEntryException e) {
                            newId = groupDAO.get(group.getName()).getId();
                        }
                        oldIdToNew.put(oldId, newId);
                    }
                }

                for (Product product : products) {
                    long oldGroupId = product.getGroupId();
                    if (oldGroupId != -1) {
                        long newGroupId = oldIdToNew.get(oldGroupId);
                        product.setGroupId(newGroupId);
                    }

                    long fileId = product.getPhoto();
                    if (fileId != 0) {
                        SerializableBitmap serializableBitmap = (SerializableBitmap) map.get(String.valueOf(fileId));
                        Bitmap bitmap = serializableBitmap.getBitmap();
                        saveBitmapToFile(mContext, bitmap, fileId);
                    }
                    productDAO.insertProduct(product);
                }
            }
            catch (Exception e) {
                Log.e(TAG, "Error saving in DB: " + e.getMessage());
                message = mContext.getString(R.string.internal_error_has_occured);
            }
            catch (OutOfMemoryError ex) {
                Log.e(TAG, "Error saving in DB: out of memory!");
                message = mContext.getString(R.string.out_of_memory_try_use_less_photos);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.hide();
            dialog = null;
            Toast.makeText(mContext,
                    message, Toast.LENGTH_SHORT).show();
        }
    }




    public static class DoExport extends AsyncTask<String, Integer, Void> {

        private Context mContext;
        private boolean isSuccess = false;
        private String path;
        private String fileName;
        private SharedPreferences sPrefs;
        private String errorMsg = "";

        private NotificationCompat.Builder mBuilder;
        private final int mId = 945271120;
        private NotificationManager mNotifyManager;

        public DoExport(Context context) {
            this.mContext = context;
            sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(mContext, R.string.please_wait, Toast.LENGTH_SHORT).show();
            initNotification();
            setStartedNotification();
        }

        private void initNotification() {
            mNotifyManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(mContext);
        }
        private void setStartedNotification() {
            mBuilder.setContentText(mContext.getString(R.string.export_in_progress))
                    .setSmallIcon(R.drawable.notify);
            if (Build.VERSION.SDK_INT >= 16) {
                mBuilder.setPriority(Notification.PRIORITY_MAX);
            }


            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(mContext, Preferences.class);

            // The stack builder object will contain an artificial back stack for
            // the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(Preferences.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            mNotifyManager.notify(mId, mBuilder.build());
        }

        private void updateProgressNotification(int incr) {
            mBuilder.setProgress(100, incr, false);
            mNotifyManager.notify(mId, mBuilder.build());
        }

        private void setCompletedNotification() {
            mBuilder.setSmallIcon(R.drawable.notify)
                    .setContentTitle(mContext.getString(R.string.export_finished))
                    .setContentText("");

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(mContext, Preferences.class);

            // The stack builder object will contain an artificial back stack for
            // the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(Preferences.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            mNotifyManager.notify(mId, mBuilder.build());
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d(TAG, "onProgressUpdate with argument = " + values[0]);
            super.onProgressUpdate(values);
            updateProgressNotification(values[0]);
        }


        @Override
        protected Void doInBackground(String... params) {

            try {
                path = params[0];
                ProductDAO productDAO = ProductDAO.getInstance(mContext);
                GroupDAO groupDAO = GroupDAO.getInstance(mContext);
                List<Product> products = productDAO.getAll();
                if (products.isEmpty()) {
                    return null;
                }
                publishProgress(5);
                List<Group> groups = groupDAO.getAll();
                Map<String, Object> map = new HashMap<>();
                map.put("products", products);
                if (!groups.isEmpty()) {
                    map.put("groups", groups);
                }

                int size = products.size();


                for (int i = 0; i < size; ++i) {
                    publishProgress(i * 100 / size);
                    Product product = products.get(i);
                    long fileId = product.getPhoto();
                    if (fileId != 0) {
                        Bitmap bitmap = getBitmapFromFileId(mContext, fileId);
                        SerializableBitmap serializableBitmap = new SerializableBitmap(bitmap);
                        map.put(String.valueOf(fileId), serializableBitmap);
                    }
                }

                String defaultFileName = mContext.getString(R.string.default_file_name);
                fileName = sPrefs.getString("file_name", defaultFileName);

                boolean useDateTime = sPrefs.getBoolean("add_datetime", true);
                if (useDateTime) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                    String dateTime = sdf.format(Calendar.getInstance().getTime());
                    fileName += ' ' + dateTime;
                }

                fileName += ".txt";

                final File file = new File(path + "/" + fileName);
                if (!file.exists()) {
                    boolean result = file.createNewFile();
                    if (!result) {
                        throw new IOException("Error creating file");
                    }
                }
                OutputStream outputStream = new FileOutputStream(file);
                ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
                objectStream.writeObject(map);
                objectStream.flush();
                objectStream.close();
                isSuccess = true;
                publishProgress(100);
            }
            catch (Exception e) {
                Log.e(TAG, "Error exporting to file");
                isSuccess = false;
                errorMsg = mContext.getString(R.string.internal_error_has_occured);
            }
            catch (OutOfMemoryError ex) {
                Log.e(TAG, "Error saving in DB: out of memory!");
                isSuccess = false;
                errorMsg = mContext.getString(R.string.out_of_memory_try_use_less_photos);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setCompletedNotification();
            if (isSuccess) {
                Toast.makeText(mContext, String.format(mContext.getString(R.string.file_name_was_saved_to),
                        fileName, path), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_LONG).show();
            }
        }
    }



    // Return Bitmap, read from file with name ${fileId}.jpeg
    public static Bitmap getBitmapFromFileId (Context context, long fileId) {
        String fileName = context.getFilesDir() + "/" + fileId + ".jpeg";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(fileName, options);
    }


    // Save compressed image to file and return file ID
    public static long saveBitmapToFile(Context context, Bitmap bitmap) throws IOException {
        return saveBitmapToFile(context, bitmap, System.currentTimeMillis());
    }

    private static long saveBitmapToFile(Context context, Bitmap bitmap, long fileId) throws IOException {
        File file = new File(context.getFilesDir() + "/" + fileId  + ".jpeg");
        if (file.exists()) {
            return fileId;
        }
        boolean result = file.createNewFile();
        if (!result) {
            throw new IOException("Error creating file");
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
        outputStream.flush();
        outputStream.close();
        return fileId;
    }
}
