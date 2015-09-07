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
package com.kobi.metalsexchange.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.widget.Toast;

import com.kobi.metalsexchange.app.sync.MetalsExchangeSyncAdapter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utility {

    public final static String CURRENCY_ILS = "ILS";
    public final static String CURRENCY_USD = "USD";
    public final static String CURRENCY_GBP = "GBP";
    public final static String CURRENCY_EUR = "EUR";
    public final static String CURRENCY_CAD = "CAD";
    public final static String CURRENCY_DKK = "DKK";
    public final static String CURRENCY_NOK = "NOK";
    public final static String CURRENCY_SEK = "SEK";
    public final static String CURRENCY_CHF = "CHF";
    public final static String CURRENCY_JOD = "JOD";
    public final static String CURRENCY_EGP = "EGP";


    public final static String GOLD  = "gold";
    public final static String SILVER = "silver";
    public final static String PLATINUM = "platinum";
    public final static String PALLADIUM = "palladium";
    public final static int METALS_COUNT = 4;


    public final static int GOLD_IDX  = 0;
    public final static int SILVER_IDX = 1;
    public final static int PLATINUM_IDX = 2;
    public final static int PALLADIUM_IDX = 3;

    public final static String WEIGHT_GRAM = "GRAM";
    public final static String WEIGHT_OUNCE = "OUNCE";
    public final static String WEIGHT_GRAIN = "GRAIN";
    public final static String WEIGHT_BAHT = "BAHT";
    public final static String WEIGHT_PENNYWEIGHT = "PENNYWEIGHT";
    public final static String WEIGHT_TOLA = "TOLA";
    public final static String WEIGHT_DRAM = "DRAM";

    public final static double GRAMS_IN_OUNCE = 31.1034768;//troy ounce (oz t)
    public final static double GRAMS_IN_GRAIN = 0.064798918;//Grain (gr)
    public final static double GRAMS_IN_BAHT = 15.244;//Baht (?)
    public final static double GRAMS_IN_PENNYWEIGHT = 1.55517384;//Pennyweight (dwt)
    public final static double GRAMS_IN_TOLA = 11.6638;//Tola (tola)
    public final static double GRAMS_IN_DRAM = 1.7718451953134;//dr

    private static boolean TWO_PANE;

    public static boolean isSupportedCurrency(String currencyCode){
        switch (currencyCode) {
            case CURRENCY_ILS:
            case CURRENCY_USD:
            case CURRENCY_GBP:
            case CURRENCY_EUR:
            case CURRENCY_CAD:
            case CURRENCY_DKK:
            case CURRENCY_NOK:
            case CURRENCY_SEK:
            case CURRENCY_CHF:
            case CURRENCY_JOD:
            case CURRENCY_EGP:
                return true;
        }
        return false;
    }

    public static boolean isFreeSupportedCurrency(String currencyCode){
        switch (currencyCode) {
            case CURRENCY_ILS:
            case CURRENCY_USD:
            case CURRENCY_GBP:
            case CURRENCY_EUR:
                return true;
        }
        return false;
    }

    public static String getPreferredCurrency(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String currencyKey = context.getString(R.string.pref_main_currency_key);
        String preferredCurrency = prefs.getString(currencyKey,CURRENCY_USD);
        switch (preferredCurrency) {
            case "nis":
                return CURRENCY_ILS;
            case "usd":
                return CURRENCY_USD;
            case "gbp":
                return CURRENCY_GBP;
            case "eur":
                return CURRENCY_EUR;
        }
        return preferredCurrency;
    }

    public static List<String> getOtherCurrencies(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> defaultSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.pref_main_currency_values_default)));

        Set<String> a = prefs.getStringSet(context.getString(R.string.pref_main_other_currencies_key), defaultSet);
        List<String> otherCurrencies = new ArrayList<>();
        if(a != null) {
            for (String str : a) {
                otherCurrencies.add(str);
            }
        }
        return otherCurrencies;
    }

    public static int getPreferredCurrencyColumnId(String currencyId) {
        switch (currencyId) {
            case CURRENCY_ILS:
                return ExchangeRatesFragment.COL_RATE_ILS;
            case CURRENCY_USD:
                return ExchangeRatesFragment.COL_RATE_USD;
            case CURRENCY_GBP:
                return ExchangeRatesFragment.COL_RATE_GBP;
            case CURRENCY_EUR:
                return ExchangeRatesFragment.COL_RATE_EUR;
            case CURRENCY_CAD:
                return ExchangeRatesFragment.COL_RATE_CAD;
            case CURRENCY_DKK:
                return ExchangeRatesFragment.COL_RATE_DKK;
            case CURRENCY_NOK:
                return ExchangeRatesFragment.COL_RATE_NOK;
            case CURRENCY_SEK:
                return ExchangeRatesFragment.COL_RATE_SEK;
            case CURRENCY_CHF:
                return ExchangeRatesFragment.COL_RATE_CHF;
            case CURRENCY_JOD:
                return ExchangeRatesFragment.COL_RATE_JOD;
            case CURRENCY_EGP:
                return ExchangeRatesFragment.COL_RATE_EGP;
        }
        return ExchangeRatesFragment.COL_RATE_USD;

    }

    public static int getTrendPreferredCurrencyColumnId(String currencyId) {

        switch (currencyId) {
            case CURRENCY_ILS:
                return TrendGraphFragment.COL_RATE_ILS_RATE;
            case CURRENCY_USD:
                return TrendGraphFragment.COL_RATE_USD_RATE;
            case CURRENCY_GBP:
                return TrendGraphFragment.COL_RATE_GBP_RATE;
            case CURRENCY_EUR:
                return TrendGraphFragment.COL_RATE_EUR_RATE;
            case CURRENCY_CAD:
                return TrendGraphFragment.COL_RATE_CAD_RATE;
            case CURRENCY_DKK:
                return TrendGraphFragment.COL_RATE_DKK_RATE;
            case CURRENCY_NOK:
                return TrendGraphFragment.COL_RATE_NOK_RATE;
            case CURRENCY_SEK:
                return TrendGraphFragment.COL_RATE_SEK_RATE;
            case CURRENCY_CHF:
                return TrendGraphFragment.COL_RATE_CHF_RATE;
            case CURRENCY_JOD:
                return TrendGraphFragment.COL_RATE_JOD_RATE;
            case CURRENCY_EGP:
                return TrendGraphFragment.COL_RATE_EGP_RATE;

        }
        return -1;
    }

    public static String getPreferredWeightUnit(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return  prefs.getString(context.getString(R.string.pref_units_key), context.getString(R.string.pref_units_grams));
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

    public static String getFriendlyDayTimeString(Context context, long dateInMillis) {
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
            int formatId = R.string.format_full_friendly_time;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedDayWithTime(context, dateInMillis)));
        } else if (julianDay == yesterdayJulianDay) {
            String yesterday = context.getString(R.string.yesterday);
            int formatId = R.string.format_full_friendly_time;
            return String.format(context.getString(
                    formatId,
                    yesterday,
                    getFormattedDayWithTime(context, dateInMillis)));
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

    public static String getFormattedDayWithTime(Context context, long dateInMillis ) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("HH:mm:ss");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static int getTabIdxForMetal(String metalId) {

        switch (metalId) {
            case GOLD:
                return GOLD_IDX;
            case SILVER:
                return SILVER_IDX;
            case PLATINUM:
                return PLATINUM_IDX;
            case PALLADIUM:
                return PALLADIUM_IDX;
        }
        return -1;
    }

    public static int getArtResourceForMetal(String metalId) {
        switch (metalId) {
            case GOLD:
                return R.drawable.gold;
            case SILVER:
                return R.drawable.silver;
            case PLATINUM:
                return R.drawable.platinum;
            case PALLADIUM:
                return R.drawable.palladium;
        }
        return -1;
    }

    public static String getWeightName(Context context) {
        String weightUnitKey = getPreferredWeightUnit(context);
        if(weightUnitKey.equals(context.getString(R.string.pref_units_grams))){
            return context.getString(R.string.pref_units_label_grams);
        } else if(weightUnitKey.equals(context.getString(R.string.pref_units_ounce))){
            return context.getString(R.string.pref_units_label_ounce);
        } else if(weightUnitKey.equals(context.getString(R.string.pref_units_grain))){
            return context.getString(R.string.pref_units_label_grain);
        } else if(weightUnitKey.equals(context.getString(R.string.pref_units_baht))){
            return context.getString(R.string.pref_units_label_baht);
        } else if(weightUnitKey.equals(context.getString(R.string.pref_units_pennyweight))){
            return context.getString(R.string.pref_units_label_pennyweight);
        } else if(weightUnitKey.equals(context.getString(R.string.pref_units_tola))){
            return context.getString(R.string.pref_units_label_tola);
        } else if(weightUnitKey.equals(context.getString(R.string.pref_units_dram))){
            return context.getString(R.string.pref_units_label_dram);
        }
        return "";
    }


    public static String getMetalName(String metalId, Context context) {
        switch (metalId) {
            case GOLD:
                return context.getString(R.string.GOLD);
            case SILVER:
                return context.getString(R.string.SILVER);
            case PLATINUM:
                return context.getString(R.string.PLATINUM);
            case PALLADIUM:
                return context.getString(R.string.PALLADIUM);
        }
        return "";
    }

    public static int getTabPositionForMetalId(String metalId){
        switch (metalId) {
            case GOLD:
                return 0;
            case SILVER:
                return 1;
            case PLATINUM:
                return 2;
            case PALLADIUM:
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


//    public static int getIconResourceForMetal(String metalId) {
//        switch (metalId) {
//            case GOLD:
//                return R.drawable.ic_gold;
//            case SILVER:
//                return R.drawable.ic_silver;
//            case PLATINUM:
//                return R.drawable.ic_platinum;
//            case PALLADIUM:
//                return R.drawable.ic_palladium;
//        }
//        return -1;
//    }

    public static int getColorResourceForMetal(String metalId) {
        switch (metalId) {
            case GOLD:
                return R.color.gold;
            case SILVER:
                return R.color.silver;
            case PLATINUM:
                return R.color.platinum;
            case PALLADIUM:
                return R.color.palladium;
        }
        return -1;
    }

    public static String getFormattedCurrency(Double price, String currencyId, Context context, boolean convertIfNeeded/*, boolean appendWeightUnit*/){
        if(convertIfNeeded){
            String weightUnitKey = getPreferredWeightUnit(context);
            if(weightUnitKey.equals(context.getString(R.string.pref_units_ounce))){
                price *= GRAMS_IN_OUNCE;
            } else if(weightUnitKey.equals(context.getString(R.string.pref_units_grain))){
                price *= GRAMS_IN_GRAIN;
            } else if(weightUnitKey.equals(context.getString(R.string.pref_units_baht))){
                price *= GRAMS_IN_BAHT;
            } else if(weightUnitKey.equals(context.getString(R.string.pref_units_pennyweight))){
                price *= GRAMS_IN_PENNYWEIGHT;
            } else if(weightUnitKey.equals(context.getString(R.string.pref_units_tola))){
                price *= GRAMS_IN_TOLA;
            } else if(weightUnitKey.equals(context.getString(R.string.pref_units_dram))){
                price *= GRAMS_IN_DRAM;
            }

        }
        Currency curr = Currency.getInstance(currencyId);
        NumberFormat defaultFormat = NumberFormat.getCurrencyInstance();
        defaultFormat.setCurrency(curr);
        return defaultFormat.format(price);
    }

    public static List<String> getAllMetalIds(){
        return Arrays.asList(GOLD, SILVER, PLATINUM, PALLADIUM);
    }

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =  (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if(activeNetwork == null || !activeNetwork.isConnected() || !activeNetwork.isAvailable()){
            Toast.makeText(c, c.getResources().getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
            return false;
        }

        return activeNetwork.isConnectedOrConnecting();
    }


    /**
     *
     * @param c Context used to get the SharedPreferences
     * @return the rates status integer type
     */
    @SuppressWarnings("ResourceType")
    static public @MetalsExchangeSyncAdapter.RatesStatus
    int getRatesStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_rates_status_key), MetalsExchangeSyncAdapter.RATES_STATUS_UNKNOWN);
    }

    static public void resetRatesStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_rates_status_key), MetalsExchangeSyncAdapter.RATES_STATUS_UNKNOWN);
        spe.apply();
    }

    /**
     * Sets the rates status into shared preference.  This function should not be called from
     * the UI thread because it uses commit to write to the shared preferences.
     * @param c Context to get the PreferenceManager from.
     * @param locationStatus The IntDef value to set
     */
    static public void setRatesStatus(Context c, @MetalsExchangeSyncAdapter.RatesStatus int locationStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_rates_status_key), locationStatus);
        spe.commit();
    }

    static public boolean syncAllAvailableDataOfThisYear(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getBoolean("sync-allData", true);
    }

    static public void setSyncAllAvailableDataOfThisYear(Context c, boolean syncAll){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putBoolean("sync-allData", syncAll);
        spe.apply();
    }

    static public void resetSyncAllAvailableDataOfThisYear(Context c){
        setSyncAllAvailableDataOfThisYear(c, false);
    }

    static public int getHistoryCount(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        int historyCount = prefs.getInt(c.getString(R.string.pref_history_business_days_key), 60);
        return historyCount;
    }


    static public CharSequence[] getFreeCurrenciesEntries(Context c){
        CharSequence[] entries = {
                c.getResources().getString(R.string.pref_main_currency_label_usd),
                c.getResources().getString(R.string.pref_main_currency_label_eur),
                c.getResources().getString(R.string.pref_main_currency_label_gbp),
                c.getResources().getString(R.string.pref_main_currency_label_ils)
        };
        return entries;
    }

    static public CharSequence[] getFreeCurrenciesValues(Context c){
        CharSequence[] entryValues = {
                c.getResources().getString(R.string.pref_main_currency_usd),
                c.getResources().getString(R.string.pref_main_currency_eur),
                c.getResources().getString(R.string.pref_main_currency_gbp),
                c.getResources().getString(R.string.pref_main_currency_ils)
        };
        return entryValues;
    }

    static public CharSequence[] getPremiumCurrenciesEntries(Context c){
        CharSequence[] entries = {

                c.getResources().getString(R.string.pref_main_currency_label_usd),
                c.getResources().getString(R.string.pref_main_currency_label_eur),
                c.getResources().getString(R.string.pref_main_currency_label_gbp),
                c.getResources().getString(R.string.pref_main_currency_label_cad),
                c.getResources().getString(R.string.pref_main_currency_label_dkk),
                c.getResources().getString(R.string.pref_main_currency_label_nok),
                c.getResources().getString(R.string.pref_main_currency_label_sek),
                c.getResources().getString(R.string.pref_main_currency_label_chf),
                c.getResources().getString(R.string.pref_main_currency_label_ils),
                c.getResources().getString(R.string.pref_main_currency_label_jod),
                c.getResources().getString(R.string.pref_main_currency_label_egp)
        };
        return entries;
    }

    static public CharSequence[] getPremiumCurrenciesValues(Context c){
        CharSequence[] entryValues = {
                c.getResources().getString(R.string.pref_main_currency_usd),
                c.getResources().getString(R.string.pref_main_currency_eur),
                c.getResources().getString(R.string.pref_main_currency_gbp),
                c.getResources().getString(R.string.pref_main_currency_cad),
                c.getResources().getString(R.string.pref_main_currency_dkk),
                c.getResources().getString(R.string.pref_main_currency_nok),
                c.getResources().getString(R.string.pref_main_currency_sek),
                c.getResources().getString(R.string.pref_main_currency_chf),
                c.getResources().getString(R.string.pref_main_currency_ils),
                c.getResources().getString(R.string.pref_main_currency_jod),
                c.getResources().getString(R.string.pref_main_currency_egp)

        };
        return entryValues;
    }


    static public CharSequence[] getFreeWeightUnitsEntries(Context c){
        CharSequence[] entries = {
                c.getResources().getString(R.string.pref_units_label_grams),
                c.getResources().getString(R.string.pref_units_label_ounce)
        };
        return entries;
    }

    static public CharSequence[] getFreeWeightUnitsValues(Context c){
        CharSequence[] entryValues = {
                c.getResources().getString(R.string.pref_units_grams),
                c.getResources().getString(R.string.pref_units_ounce)

        };
        return entryValues;
    }

    static public CharSequence[] getPremiumWeightUnitsEntries(Context c){
        CharSequence[] entries = {
                c.getResources().getString(R.string.pref_units_label_grams),
                c.getResources().getString(R.string.pref_units_label_ounce),
                c.getResources().getString(R.string.pref_units_label_grain),
                c.getResources().getString(R.string.pref_units_label_baht),
                c.getResources().getString(R.string.pref_units_label_pennyweight),
                c.getResources().getString(R.string.pref_units_label_tola),
                c.getResources().getString(R.string.pref_units_label_dram)
        };
        return entries;
    }

    static public CharSequence[] getPremiumWeightUnitsValues(Context c){
        CharSequence[] entryValues = {
                c.getResources().getString(R.string.pref_units_grams),
                c.getResources().getString(R.string.pref_units_ounce),
                c.getResources().getString(R.string.pref_units_grain),
                c.getResources().getString(R.string.pref_units_baht),
                c.getResources().getString(R.string.pref_units_pennyweight),
                c.getResources().getString(R.string.pref_units_tola),
                c.getResources().getString(R.string.pref_units_dram)
        };
        return entryValues;
    }

}