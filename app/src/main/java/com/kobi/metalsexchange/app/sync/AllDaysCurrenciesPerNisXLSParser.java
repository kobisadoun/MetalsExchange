package com.kobi.metalsexchange.app.sync;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.kobi.metalsexchange.app.R;
import com.kobi.metalsexchange.app.Utility;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class AllDaysCurrenciesPerNisXLSParser {

    public final String LOG_TAG = AllDaysCurrenciesPerNisXLSParser.class.getSimpleName();
    private HashMap<String, HashMap<String, Double>> rawValuesMap = new HashMap<String, HashMap<String, Double>>();

    private Context context;

	public AllDaysCurrenciesPerNisXLSParser(Context context){
        this.context = context;
        parseXlsFromUrl();
	}

    public HashMap<String, HashMap<String, Double>> getCurrenciesPerNIS(){
        return rawValuesMap;
    }

    private void parseXlsFromUrl() {
        HttpURLConnection urlConnection = null;
        try {
            BufferedReader reader = null;
            Uri builtUri = Uri.parse("http://www.boi.org.il/he/Markets/Documents/yazigmizt.xls").buildUpon().build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent", "Chrome/41.0.2272.89");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            parseExcel(inputStream);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            Utility.setRatesStatus(context, MetalsExchangeSyncAdapter.RATES_STATUS_SERVER_DOWN);
            Handler mainHandler = new Handler(context.getMainLooper());
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context, context.getResources().getString(R.string.error_no_response_from_server), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
            Utility.setRatesStatus(context, MetalsExchangeSyncAdapter.RATES_STATUS_SERVER_INVALID);
            Handler mainHandler = new Handler(context.getMainLooper());
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context, context.getResources().getString(R.string.error_server_response_not_valid), Toast.LENGTH_LONG).show();
                }
            });
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void parseExcel(InputStream fis){

        try{
            // Create a workbook using the Input Stream
            HSSFWorkbook myWorkBook = new HSSFWorkbook(fis);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            // We now need something to iterate through the cells
            Iterator<Row> rowIter = mySheet.rowIterator();
            while(rowIter.hasNext()){

                HSSFRow myRow = (HSSFRow) rowIter.next();
                // Skip the first 3 rows
                if(myRow.getRowNum() < 3) {
                    continue;
                }

                Iterator<Cell> cellIter = myRow.cellIterator();
                HashMap<String, Double> rawMap = new HashMap<String, Double>();
                Date rowDate = null;
                while(cellIter.hasNext()){

                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    String cellValue = "";

                    // Check for cell Type
                  //  if(myCell.getCellType() == HSSFCell.CELL_TYPE_STRING){
                  //      cellValue = myCell.getStringCellValue();
                  // }
                   // else {
                    //    cellValue = String.valueOf(myCell.getNumericCellValue());
                    //}

                    // Just some log information
                 //   Log.v(LOG_TAG, cellValue);


                    // Push the parsed data in the Java Object
                    // Check for cell index
                    switch (myCell.getColumnIndex()) {
                        case 0:// Date
                            rowDate = myCell.getDateCellValue();
                            break;
                        case 2:// USD
                            rawMap.put("USD", myCell.getNumericCellValue());
                            break;
                        case 4:// GBP
                            rawMap.put("GBP", myCell.getNumericCellValue());
                            break;
                        case 21://EUR
                            rawMap.put("EUR", myCell.getNumericCellValue());
                            break;
                        default:
                            break;
                    }
                }
                if(rowDate != null) {
                    SimpleDateFormat monthDayFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String shortDate = monthDayFormat.format(rowDate);
                    rawValuesMap.put(shortDate, rawMap);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}