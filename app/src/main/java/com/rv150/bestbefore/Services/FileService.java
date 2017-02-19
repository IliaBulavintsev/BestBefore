package com.rv150.bestbefore.Services;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
                Toast.makeText(context, R.string.out_of_memory_send_feedback, Toast.LENGTH_LONG).show();
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
        private String mSuccessMsg;

        private final boolean mUnion;
        private boolean isSuccess = false;

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
                mSuccessMsg = params[0];
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
                isSuccess = true;
            }
            catch (Exception e) {
                Log.e(TAG, "Error saving in DB: " + e.getMessage());
                isSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.hide();
            if (isSuccess) {
                Toast.makeText(mContext,
                        mSuccessMsg, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(mContext, R.string.internal_error_has_occured,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }




    public static class DoExport extends AsyncTask<String, Void, Void> {

        private Context mContext;
        private boolean isSuccess = false;
        private String path;
        private String fileName;

        public DoExport(Context context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(mContext, R.string.please_wait, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... params) {
            path = params[0];
            ProductDAO productDAO = ProductDAO.getInstance(mContext);
            GroupDAO groupDAO = GroupDAO.getInstance(mContext);

            List<Product> products = productDAO.getAll();
            if (products.isEmpty()) {
                return null;
            }
            List<Group> groups = groupDAO.getAll();
            Map<String, Object> map = new HashMap<>();
            map.put("products", products);
            if (!groups.isEmpty()) {
                map.put("groups", groups);
            }

            int size = products.size();

            for (int i = 0; i < size; ++i) {
                Product product = products.get(i);
                long fileId = product.getPhoto();
                if (fileId != 0) {
                    Bitmap bitmap = getBitmapFromFileId(mContext, fileId);
                    SerializableBitmap serializableBitmap = new SerializableBitmap(bitmap);
                    map.put(String.valueOf(fileId), serializableBitmap);
                }
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                String dateTime = sdf.format(Calendar.getInstance().getTime());
                fileName = "Products " + dateTime + ".txt";
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
            }
            catch (Exception e) {
                Log.e(TAG, "Error exporting to file");
                isSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isSuccess) {
                Toast.makeText(mContext, String.format(mContext.getString(R.string.export_success),
                        fileName, path), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(mContext, R.string.internal_error_has_occured, Toast.LENGTH_SHORT).show();
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

    public static long saveBitmapToFile(Context context, Bitmap bitmap, long fileId) throws IOException {
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
