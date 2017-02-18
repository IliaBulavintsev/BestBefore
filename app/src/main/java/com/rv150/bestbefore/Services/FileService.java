package com.rv150.bestbefore.Services;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Exceptions.DuplicateEntryException;
import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Models.Product;
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
            final Map<String, List> map = (Map) objectInputStream.readObject();
            objectInputStream.close();

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
                                saveMapToBD(context, map, false, context.getString(R.string.import_success));
                            }

                        })
                        .setNeutralButton(R.string.combine, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveMapToBD(context, map, true, context.getString(R.string.import_success));
                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
            else {
                saveMapToBD(context, map, false, context.getString(R.string.import_success));
            }
        }
        catch (Exception e) {
            Toast toast = Toast.makeText(context,
                    R.string.restore_failed, Toast.LENGTH_SHORT);
            toast.show();
        }
    }



    public static void saveMapToBD(Context context, Map<String, List> map, boolean union, String successMsg) {
        try {

            ProductDAO productDAO = ProductDAO.getInstance(context);
            GroupDAO groupDAO = GroupDAO.getInstance(context);

            List<Product> products = (List) map.get("products");
            List<Group> groups = (List) map.get("groups");

            Map<Long, Long> oldIdToNew = new HashMap<>(); // Старые ID к новым для правильных внешних ключей

            if (!union) {
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
                productDAO.insertProduct(product);
            }

            Toast toast = Toast.makeText(context,
                    successMsg, Toast.LENGTH_SHORT);
            toast.show();
        }
        catch (Exception e) {
            Toast toast = Toast.makeText(context,
                    R.string.restore_failed, Toast.LENGTH_SHORT);
            toast.show();
        }
    }





    public static void handleDirectoryChoice(String path, Context context) {

        ProductDAO productDAO = ProductDAO.getInstance(context);
        GroupDAO groupDAO = GroupDAO.getInstance(context);

        List<Product> products = productDAO.getAll();
        if (products.isEmpty()) {
            Toast.makeText(context,
                    R.string.nothing_to_export, Toast.LENGTH_SHORT).show();
            return;
        }
        List<Group> groups = groupDAO.getAll();
        Map<String, List> map = new HashMap<>();
        map.put("products", products);
        if (!groups.isEmpty()) {
            map.put("groups", groups);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String dateTime = sdf.format(Calendar.getInstance().getTime());
            String fileName = "Products " + dateTime + ".txt";
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
            Toast.makeText(context, String.format(context.getString(R.string.export_success),
                    fileName, path), Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Log.e(TAG, "Error exporting to file");
            Toast.makeText(context,
                    R.string.internal_error_has_occured, Toast.LENGTH_SHORT).show();
        }
    }
}
