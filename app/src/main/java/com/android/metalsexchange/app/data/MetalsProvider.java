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
package com.android.metalsexchange.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MetalsProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MetalsDbHelper mOpenHelper;

    static final int RATES = 100;
    static final int RATES_BY_METAL = 101;
    static final int RATES_BY_METAL_AND_DATE = 102;

    private static final SQLiteQueryBuilder sRatesByMetalSettingQueryBuilder;

    static{
        sRatesByMetalSettingQueryBuilder = new SQLiteQueryBuilder();
        
        sRatesByMetalSettingQueryBuilder.setTables(
                MetalsContract.MetalsRateEntry.TABLE_NAME);
    }

    private static final String sMetalSelection =
            MetalsContract.MetalsRateEntry.TABLE_NAME+
                    "." + MetalsContract.MetalsRateEntry.COLUMN_METAL_ID + " = ? ";

    private static final String sMetalWithStartDateSelection =
            MetalsContract.MetalsRateEntry.TABLE_NAME+
                    "." + MetalsContract.MetalsRateEntry.COLUMN_METAL_ID + " = ? AND " +
                    MetalsContract.MetalsRateEntry.COLUMN_DATE + " <= ? ";

    private static final String sMetalAndDaySelection =
            MetalsContract.MetalsRateEntry.TABLE_NAME +
                    "." + MetalsContract.MetalsRateEntry.COLUMN_METAL_ID + " = ? AND " +
                    MetalsContract.MetalsRateEntry.COLUMN_DATE + " = ? ";

    private Cursor getRatesByMetal(Uri uri, String[] projection, String sortOrder) {
        String metalId = MetalsContract.MetalsRateEntry.getMetalIdFromUri(uri);
        long startDate = MetalsContract.MetalsRateEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sMetalSelection;
            selectionArgs = new String[]{metalId};
        } else {
            selectionArgs = new String[]{metalId, Long.toString(startDate)};
            selection = sMetalWithStartDateSelection;
        }

        return sRatesByMetalSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getRatesByMetalIdAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String metalId = MetalsContract.MetalsRateEntry.getMetalIdFromUri(uri);
        long date = MetalsContract.MetalsRateEntry.getDateFromUri(uri);

        return sRatesByMetalSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMetalAndDaySelection,
                new String[]{metalId, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MetalsContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MetalsContract.PATH_METALS, RATES);
        matcher.addURI(authority, MetalsContract.PATH_METALS + "/*", RATES_BY_METAL);
        matcher.addURI(authority, MetalsContract.PATH_METALS + "/*/#", RATES_BY_METAL_AND_DATE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MetalsDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case RATES_BY_METAL_AND_DATE:
                return MetalsContract.MetalsRateEntry.CONTENT_ITEM_TYPE;
            case RATES_BY_METAL:
                return MetalsContract.MetalsRateEntry.CONTENT_TYPE;
            case RATES:
                return MetalsContract.MetalsRateEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "rates/*/*"
            case RATES_BY_METAL_AND_DATE:
            {
                retCursor = getRatesByMetalIdAndDate(uri, projection, sortOrder);
                break;
            }
            // "rates/*"
            case RATES_BY_METAL: {
                retCursor = getRatesByMetal(uri, projection, sortOrder);
                break;
            }
            // "rates"
            case RATES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MetalsContract.MetalsRateEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case RATES: {
                normalizeDate(values);
                long _id = db.insert(MetalsContract.MetalsRateEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MetalsContract.MetalsRateEntry.buildMetalsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case RATES:
                rowsDeleted = db.delete(
                        MetalsContract.MetalsRateEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(MetalsContract.MetalsRateEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(MetalsContract.MetalsRateEntry.COLUMN_DATE);
            values.put(MetalsContract.MetalsRateEntry.COLUMN_DATE, MetalsContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case RATES:
                normalizeDate(values);
                rowsUpdated = db.update(MetalsContract.MetalsRateEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RATES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(MetalsContract.MetalsRateEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}