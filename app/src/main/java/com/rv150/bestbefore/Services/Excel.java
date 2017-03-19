package com.rv150.bestbefore.Services;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ivan on 19.03.17.
 */

public class Excel extends AsyncTask<String, Void, Boolean> {


    private final ProgressDialog mProgressDialog;
    private final Context mContext;

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

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");

        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                return false;
            }
            ;
        }

        DBHelper dbHelper = DBHelper.getInstance(mContext);

        File file = new File(exportDir, "bestBefore.csv");

        try {

            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            //SQLiteDatabase db = dbhelper.getWritableDatabase();

            Cursor curCSV = dbHelper.getReadableDatabase().rawQuery("select * from " + DBHelper.Product.TABLE_NAME, null);

            csvWrite.writeNext(curCSV.getColumnNames());

            while (curCSV.moveToNext())

            {

                String arrStr[] = {
                        curCSV.getString(0),
                        curCSV.getString(1),
                        curCSV.getString(2),
                        curCSV.getString(3),
                        curCSV.getString(4),
                        curCSV.getString(5),
                        curCSV.getString(6)};

                csvWrite.writeNext(arrStr);
            }

            csvWrite.close();
            curCSV.close();

            /*String data="";
        data=readSavedData();
        data= data.replace(",", ";");
        writeData(data);*/

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
            Toast.makeText(mContext, "Export succeed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean makeXls() {
        ArrayList arList=null;
        ArrayList al=null;


        String inFilePath = Environment.getExternalStorageDirectory().toString()+"/bestBefore.csv";
        String outFilePath = Environment.getExternalStorageDirectory().toString()+"/test.xls";
        String thisLine;


        try {

            FileInputStream fis = new FileInputStream(inFilePath);
            DataInputStream myInput = new DataInputStream(fis);

            arList = new ArrayList();
            while ((thisLine = myInput.readLine()) != null) {
                al = new ArrayList();
                String strar[] = thisLine.split(",");
                for (int j = 0; j < strar.length; j++) {
                    al.add(strar[j]);
                }
                arList.add(al);
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("shit");
        }

        try
        {
            HSSFWorkbook hwb = new HSSFWorkbook();
            HSSFSheet sheet = hwb.createSheet("new sheet");
            for(int k=0;k<arList.size();k++)
            {
                ArrayList ardata = (ArrayList)arList.get(k);
                HSSFRow row = sheet.createRow((short) 0+k);
                for(int p=0;p<ardata.size();p++)
                {
                    HSSFCell cell = row.createCell((short) p);
                    String data = ardata.get(p).toString();
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
                    //*/
                    // cell.setCellValue(ardata.get(p).toString());
                }
                System.out.println();
            }
            FileOutputStream fileOut = new FileOutputStream(outFilePath);
            hwb.write(fileOut);
            fileOut.close();
            System.out.println("Your excel file has been generated");
        } catch ( Exception ex ) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
