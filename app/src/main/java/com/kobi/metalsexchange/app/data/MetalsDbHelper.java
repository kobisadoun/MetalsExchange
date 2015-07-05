/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kobi.metalsexchange.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kobi.metalsexchange.app.Utility;

/**
 * Manages a local database for metals rate data.
 */
public class MetalsDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "metals.db";

    private Context context;

    public MetalsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_METALS_EXCHANGE_RATES_TABLE = "CREATE TABLE " + MetalsContract.MetalsRateEntry.TABLE_NAME + " (" +
                MetalsContract.MetalsRateEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                MetalsContract.MetalsRateEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_METAL_ID + " STRING NOT NULL," +

                MetalsContract.MetalsRateEntry.COLUMN_ILS_RATE + " REAL NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_USD_RATE + " REAL NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_GBP_RATE + " REAL NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_EUR_RATE + " REAL NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_CAD_RATE + " REAL NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_DKK_RATE + " REAL NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_NOK_RATE + " REAL NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_SEK_RATE + " REAL NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_CHF_RATE + " REAL NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_JOD_RATE + " REAL NOT NULL, " +
                MetalsContract.MetalsRateEntry.COLUMN_EGP_RATE + " REAL NOT NULL, " +

                " UNIQUE (" + MetalsContract.MetalsRateEntry.COLUMN_DATE + ", " +
                MetalsContract.MetalsRateEntry.COLUMN_METAL_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_METALS_EXCHANGE_RATES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MetalsContract.MetalsRateEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
        Utility.resetRatesStatus(context);
    }
}
