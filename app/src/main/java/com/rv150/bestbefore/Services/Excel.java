package com.rv150.bestbefore.Services;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.rv150.bestbefore.R;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ivan on 19.03.17.
 */

public class Excel extends AsyncTask<String, Void, Boolean> {


    private final ProgressDialog mProgressDialog;
    private final Context mContext;

    private String mTargetPath;

    public Excel(Context context) {
        this.mContext = context;
        mProgressDialog = new ProgressDialog(mContext);
    }

    @Override

    protected void onPreExecute()

    {

        this.mProgressDialog.setMessage("Exporting database...");

        this.mProgressDialog.show();

    }


    protected Boolean doInBackground(final String... args) {
        mTargetPath = args[0];
        File exportDir = mContext.getCacheDir();

        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                return false;
            }
        }

        DBHelper dbHelper = DBHelper.getInstance(mContext);

        File file = new File(exportDir, "bestBefore.csv");

        try {

            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

            String[] header = {
                    mContext.getString(R.string.name),
                    mContext.getString(R.string.date_produced),
                    mContext.getString(R.string.okay_before),
                    mContext.getString(R.string.quantity),
                    mContext.getString(R.string.group)
            };
            csvWrite.writeNext(header);

            Cursor curProduct = dbHelper.getReadableDatabase().rawQuery("select " +
                    DBHelper.Product.COLUMN_NAME_NAME + ", " +
                    DBHelper.Product.COLUMN_NAME_PRODUCED + ", " +
                    DBHelper.Product.COLUMN_NAME_DATE + ", " +
                    DBHelper.Product.COLUMN_NAME_QUANTITY + ", " +
                    DBHelper.Product.COLUMN_NAME_GROUP_ID +
                    " from " + DBHelper.Product.TABLE_NAME, null);

            while (curProduct.moveToNext()) {

                String name = curProduct.getString(0);

                long producedMillis = curProduct.getLong(1);
                long bestBeforeMillis = curProduct.getLong(2);

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                String produced = sdf.format(new Date(producedMillis));
                String bestBefore = sdf.format(new Date(bestBeforeMillis));

                String quantity = String.valueOf(curProduct.getInt(3));

                long groupId = curProduct.getInt(4);
                String groupName = "";

                Cursor curGroup = dbHelper.getReadableDatabase().rawQuery("SELECT " +
                        DBHelper.Group.COLUMN_NAME_NAME +
                        " FROM " + DBHelper.Group.TABLE_NAME +
                        " WHERE " + DBHelper.Group._ID + " = ?", new String[] {String.valueOf(groupId)});
                if (curGroup.moveToNext()) {
                    groupName = curGroup.getString(0);
                }
                curGroup.close();

                String arrStr[] = {
                        name,
                        produced,
                        bestBefore,
                        quantity,
                        groupName
                };

                csvWrite.writeNext(arrStr);
            }

            csvWrite.close();
            curProduct.close();
            return makeXls();

        } catch (IOException e) {
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
            Toast.makeText(mContext, R.string.internal_error_has_occured, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean makeXls() {

        String inFilePath = mContext.getCacheDir().getPath() + "/bestBefore.csv";
        String outFilePath = mTargetPath + "/Products.xlsx";


        try {
            InputStream csvStream = new FileInputStream(inFilePath);
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            CSVReader csvReader = new CSVReader(csvStreamReader);
            

            String[] line;
            List<String[]> arList = new ArrayList<>();

            while ((line = csvReader.readNext()) != null) {
                arList.add(line);
            }


            HSSFWorkbook hwb = new HSSFWorkbook();
            HSSFSheet sheet = hwb.createSheet("new sheet");
            for(int k=0;k<arList.size();k++)
            {
                String[] ardata = arList.get(k);
                HSSFRow row = sheet.createRow((short) k);

                for(int p=0; p < ardata.length; p++)
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
        } catch ( Exception ex ) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
