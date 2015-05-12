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
package com.android.metalsexchange.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class Utility {

    public final static String CURRENCY_NIS = "nis";
    public final static String CURRENCY_USD = "usd";
    public final static String CURRENCY_GBP = "gbp";
    public final static String CURRENCY_EUR = "eur";


    public final static String GOLD  = "index";
    public final static String SILVER = "silver";
    public final static String PLATINUM = "platinum";
    public final static String PALLADIUM = "palladium";
    public final static int METALS_COUNT = 4;


    public final static int GOLD_IDX  = 0;
    public final static int SILVER_IDX = 1;
    public final static int PLATINUM_IDX = 2;
    public final static int PALLADIUM_IDX = 3;

//    public final static double GRAMS_IN_OUNCE = 28.3495231;
    public final static double GRAMS_IN_OUNCE = 31.1034768;//troy ounce

    private static boolean TWO_PANE;

    public static String getPreferredCurrency(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_main_currency_key),context.getString(R.string.pref_main_currency_default));
    }


    public static int getPreferredCurrencyColumnId(String currencyId) {
        if(currencyId.equals(CURRENCY_NIS)){
            return ExchangeRatesFragment.COL_RATE_NIS;
        } else if(currencyId.equals(CURRENCY_USD)){
            return ExchangeRatesFragment.COL_RATE_USD;
        }else if(currencyId.equals(CURRENCY_GBP)){
            return ExchangeRatesFragment.COL_RATE_GBP;
        }else if(currencyId.equals(CURRENCY_EUR)){
            return ExchangeRatesFragment.COL_RATE_EUR;
        }
        return ExchangeRatesFragment.COL_RATE_NIS;
    }

    public static int getTrendPreferredCurrencyColumnId(String currencyId) {
        if(currencyId.equals(CURRENCY_NIS)){
            return TrendGraphFragment.COL_RATE_NIS_RATE;
        } else if(currencyId.equals(CURRENCY_USD)){
            return TrendGraphFragment.COL_RATE_USD_RATE;
        }else if(currencyId.equals(CURRENCY_GBP)){
            return TrendGraphFragment.COL_RATE_GBP_RATE;
        }else if(currencyId.equals(CURRENCY_EUR)){
            return TrendGraphFragment.COL_RATE_EUR_RATE;
        }
        return TrendGraphFragment.COL_RATE_NIS_RATE;
    }

    public static boolean isGrams(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_grams))
                .equals(context.getString(R.string.pref_units_grams));
    }

    public static boolean isTwoPanesView(){
        return TWO_PANE;
    }

    public static void setTwoPanesView(boolean twoPane){
        TWO_PANE = twoPane;
    }

    public static String getCurrentMetalId(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("metal_id", GOLD);
    }

    public static void setCurrentMetalId(String metalId, Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("metal_id", metalId).commit();
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);
        long yesterdayTime = System.currentTimeMillis()-  1000 * 60 * 60 * 24;
        int yesterdayJulianDay = Time.getJulianDay(yesterdayTime, time.gmtoff);


        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis)));
        } else if (julianDay == yesterdayJulianDay) {
            String yesterday = context.getString(R.string.yesterday);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    yesterday,
                    getFormattedMonthDay(context, dateInMillis)));
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getShortFormattedMonthDay(Context context, long dateInMillis ) {
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("dd/MM/yy");
        String shortDate = monthDayFormat.format(dateInMillis);
        return shortDate;
    }

    public static int getTabIdxForMetal(String metalId) {
        if(metalId.equals(GOLD)){
            return GOLD_IDX;
        } else if(metalId.equals(SILVER)){
            return SILVER_IDX;
        }else if(metalId.equals(PLATINUM)){
            return PLATINUM_IDX;
        }else if(metalId.equals(PALLADIUM)) {
            return PALLADIUM_IDX;
        }
        return -1;
    }

    public static int getArtResourceForMetal(String metalId) {
        if(metalId.equals(GOLD)){
            return R.drawable.gold;
        } else if(metalId.equals(SILVER)){
            return R.drawable.silver;
        }else if(metalId.equals(PLATINUM)){
            return R.drawable.platinum;
        }else if(metalId.equals(PALLADIUM)) {
            return R.drawable.palladium;
        }
        return -1;
    }

    public static String getWeightName(boolean isGrams, Context context) {
        if(isGrams){
            return context.getString(R.string.pref_units_label_grams);
        }
        return context.getString(R.string.pref_units_label_ounce);
    }


    public static String getMetalName(String metalId, Context context) {
        if(metalId.equals(GOLD)){
            return context.getString(R.string.GOLD);
        } else if(metalId.equals(SILVER)){
            return context.getString(R.string.SILVER);
        }else if(metalId.equals(PLATINUM)){
            return context.getString(R.string.PLATINUM);
        }else if(metalId.equals(PALLADIUM)) {
            return context.getString(R.string.PALLADIUM);
        }
        return "";
    }

    public static int getTabPositionForMetalId(String metalId){
        if(metalId.equals(GOLD)){
            return 0;
        } else if(metalId.equals(SILVER)){
            return 1;
        }else if(metalId.equals(PLATINUM)){
            return 2;
        }else if(metalId.equals(PALLADIUM)) {
            return 3;
        }
        return 0;
    }

    public static String getMetalIdForTabPosition(int position) {

        switch (position){
            case 0:
                return Utility.GOLD;
            case 1:
                return Utility.SILVER;
            case 2:
                return Utility.PLATINUM;
            case 3:
                return Utility.PALLADIUM;
        }
        return null;

    }


    public static int getIconResourceForMetal(String metalId) {
        if(metalId.equals(GOLD)){
            return R.drawable.ic_gold;
        } else if(metalId.equals(SILVER)){
            return R.drawable.ic_silver;
        }else if(metalId.equals(PLATINUM)){
            return R.drawable.ic_platinum;
        }else if(metalId.equals(PALLADIUM)) {
            return R.drawable.ic_palladium;
        }
        return -1;
    }

    public static String getFormattedCurrency(Double price, String currencyId, Context context, boolean convertIfNeeded){
        Locale locale = null;
        if(currencyId.equals(CURRENCY_NIS)){
            locale = new Locale("iw","IL");
        } else if(currencyId.equals(CURRENCY_USD)){
            locale = Locale.US;
        }else if(currencyId.equals(CURRENCY_GBP)){
            locale = Locale.UK;
        }else if(currencyId.equals(CURRENCY_EUR)){
            locale = Locale.FRANCE;
        }
        Currency curr = Currency.getInstance(locale);
        if(convertIfNeeded && !isGrams(context)) {
            price *= GRAMS_IN_OUNCE;
        }
        // get and print the symbol of the currency
        String symbol = curr.getSymbol(locale);
        DecimalFormat myFormatter = new DecimalFormat("###.##");
        return symbol+myFormatter.format(price);
    }

    public static List<String> getAllMetalIds(){
        return Arrays.asList(GOLD, SILVER, PLATINUM, PALLADIUM);
    }

}