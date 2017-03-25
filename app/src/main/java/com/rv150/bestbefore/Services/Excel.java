package com.rv150.bestbefore.Services;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Resources;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ivan on 19.03.17.
 */

public class Excel extends AsyncTask<String, Void, Boolean> {
    private final ProgressDialog mProgressDialog;
    private final Context mContext;
    private final SharedPreferences sPrefs;
    private final List<String> mColumns;
    private final List<String> mCategories;

    private final ProductDAO productDAO;
    private final GroupDAO groupDAO;

    private String mTargetPath;

    private final int mGroupsCount;

    private boolean noDataFlag = false;

    public Excel(Context context, List<String> columns, List<String> categories, int groupsCount, String targetPath) {
        this.mContext = context;
        sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mProgressDialog = new ProgressDialog(mContext);
        mColumns = columns;
        mCategories = categories;
        mTargetPath = targetPath;
        productDAO = ProductDAO.getInstance(context);
        groupDAO = GroupDAO.getInstance(context);
        mGroupsCount = groupsCount;
    }

    @Override
    protected void onPreExecute() {
        this.mProgressDialog.setMessage(mContext.getString(R.string.forming_data));
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.show();
    }


    protected Boolean doInBackground(final String... args) {
        try {
            boolean csv = makeCsv();
            return csv && makeXls();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    protected void onPostExecute(final Boolean success) {
        if (this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
        }
        if (success) {
            Toast.makeText(mContext, R.string.excel_file_was_formed, Toast.LENGTH_SHORT).show();
        } else {
            if (noDataFlag) {
                Toast.makeText(mContext, R.string.nothing_to_export, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(mContext, R.string.internal_error_has_occured, Toast.LENGTH_SHORT).show();
            }
        }
    }





    private boolean makeCsv() throws Exception {
        File exportDir = mContext.getCacheDir();

        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                return false;
            }
        }


        List<Product> finalProducts = new ArrayList<>();
        if (mCategories.contains(mContext.getString(R.string.all_fresh_products))) {
            finalProducts = productDAO.getFresh();
        }
        else {
            for (int i = 0; i < mGroupsCount; ++i) {
                String groupName = mCategories.get(i);
                long groupId = groupDAO.get(groupName).getId();
                finalProducts.addAll(productDAO.getFreshFromGroup(groupId));
            }
        }

        if (mCategories.contains(mContext.getString(R.string.overdue_products))) {
            finalProducts.addAll(productDAO.getOverdued());
        }
        if (mCategories.contains(mContext.getString(R.string.trash))) {
            finalProducts.addAll(productDAO.getRemoved());
        }

        if (finalProducts.isEmpty()) {
            noDataFlag = true;
            return false;
        }

        sortList(finalProducts);


        File file = new File(exportDir, "bestBefore.csv");

        CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
        String[] header = mColumns.toArray(new String[mColumns.size()]);
        csvWrite.writeNext(header);


        for (Product product: finalProducts) {
            String name = product.getTitle();

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String produced = sdf.format(product.getProduced().getTime());
            String bestBefore = sdf.format(product.getDate().getTime());

            String quantity = String.valueOf(product.getQuantity());
            int measure = product.getMeasure();
            quantity += " " + Resources.Measures.values()[measure].getText();

            long groupId = product.getGroupId();

            String groupName = "";
            if (groupId != -1) {
                groupName = groupDAO.get(groupId).getName();
            }

            List<String> line = new ArrayList<>();
            if (mColumns.contains(mContext.getString(R.string.name))) {
                line.add(name);
            }
            if (mColumns.contains(mContext.getString(R.string.date_produced))) {
                line.add(produced);
            }
            if (mColumns.contains(mContext.getString(R.string.okay_before))) {
                line.add(bestBefore);
            }
            if (mColumns.contains(mContext.getString(R.string.quantity))) {
                line.add(quantity);
            }
            if (mColumns.contains(mContext.getString(R.string.group))) {
                line.add(groupName);
            }

            String arrStr[] = line.toArray(new String[line.size()]);
            csvWrite.writeNext(arrStr);
        }
        csvWrite.close();
        return true;
    }

    private boolean makeXls() throws Exception {

        String defaultFileName = mContext.getString(R.string.default_file_name);
        String fileName = sPrefs.getString("excel_name", defaultFileName);

        boolean useDateTime = sPrefs.getBoolean("add_datetime_to_excel", true);
        if (useDateTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            String dateTime = sdf.format(Calendar.getInstance().getTime());
            fileName += ' ' + dateTime;
        }

        fileName += ".xls";

        String inFilePath = mContext.getCacheDir().getPath() + "/bestBefore.csv";
        String outFilePath = mTargetPath + '/' + fileName;


        InputStream csvStream = new FileInputStream(inFilePath);
        InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
        CSVReader csvReader = new CSVReader(csvStreamReader);


        String[] line;
        List<String[]> arList = new ArrayList<>();

        while ((line = csvReader.readNext()) != null) {
            arList.add(line);
        }


        HSSFWorkbook hwb = new HSSFWorkbook();
        HSSFSheet sheet = hwb.createSheet(mContext.getString(R.string.products_list));
        for (int k = 0; k < arList.size(); k++)
        {
            String[] ardata = arList.get(k);
            HSSFRow row = sheet.createRow((short) k);

            for (int p = 0; p < ardata.length; p++)
            {
                HSSFCell cell = row.createCell(p);
                String data = ardata[p];
                if(data.startsWith("=")){
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    data=data.replaceAll("\"", "");
                    data=data.replaceAll("=", "");
                    cell.setCellValue(data);
                }else if(data.startsWith("\"")){
                    data=data.replaceAll("\"", "");
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(data);
                }else{
                    data=data.replaceAll("\"", "");
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(data);
                }
            }
            System.out.println();
        }
        FileOutputStream fileOut = new FileOutputStream(outFilePath);
        hwb.write(fileOut);
        fileOut.close();

        return true;
    }

    private void sortList(List<Product> list) {
        String howToSort = sPrefs.getString(Resources.PREF_HOW_TO_SORT_EXCEL, Resources.STANDART);
        switch (howToSort) {
            case Resources.STANDART:
                Collections.sort(list, Product.getStandartComparator());
                break;
            case Resources.SPOILED_TO_FRESH:
                Collections.sort(list, Product.getSpoiledToFreshComparator());
                break;
            case Resources.FRESH_TO_SPOILED:
                Collections.sort(list, Product.getFreshToSpoiledComparator());
                break;
            case Resources.BY_NAME:
                Collections.sort(list, Product.getByNameComparator());
                break;
            case Resources.BY_GROUPS:
                Collections.sort(list, new Comparator<Product>() {
                    @Override
                    public int compare(Product o1, Product o2) {
                        if (o1.getGroupId() == -1 && o2.getGroupId() != -1) {
                            return -1;
                        }
                        if (o2.getGroupId() == -1 && o1.getGroupId() != -1) {
                            return 1;
                        }
                        if (o1.getGroupId() == -1 && o2.getGroupId() == -1) {
                            return 0;
                        }

                        Group group1 = groupDAO.get(o1.getGroupId());
                        Group group2 = groupDAO.get(o2.getGroupId());
                        return group1.getName().compareTo(group2.getName());
                    }
                });
                break;
            default:
                break;
        }
    }
}
