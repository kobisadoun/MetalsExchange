package com.kobi.metalsexchange.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import com.kobi.metalsexchange.app.MainActivity;
import com.kobi.metalsexchange.app.R;
import com.kobi.metalsexchange.app.Utility;
import com.kobi.metalsexchange.app.data.MetalsContract;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

public class MetalsExchangeSyncAdapterOld extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MetalsExchangeSyncAdapterOld.class.getSimpleName();
    // Interval at which to sync with the rate site, in seconds.
    // 60 seconds (1 minute) * 600 = 10 hours
    public static final int SYNC_INTERVAL = 60 * 600;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int EXCHANGE_RATE_NOTIFICATION_ID = 9000;


    private static final String[] NOTIFY_METAL_PROJECTION = new String[] {
            MetalsContract.MetalsRateEntry.COLUMN_METAL_ID,
            MetalsContract.MetalsRateEntry.COLUMN_NIS_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_USD_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_GBP_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_EUR_RATE
    };

    // these indices must match the projection
    private static final int INDEX_METAL_ID = 0;
    private static final int INDEX_NIS_RATE = 1;
    private static final int INDEX_USD_RATE = 2;
    private static final int INDEX_GBP_RATE = 3;
    private static final int INDEX_EUR_RATE = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RATES_STATUS_OK, RATES_STATUS_SERVER_DOWN, RATES_STATUS_SERVER_INVALID, RATES_STATUS_UNKNOWN, RATES_STATUS_INVALID})
    public @interface RatesStatus {}

    public static final int RATES_STATUS_OK = 0;
    public static final int RATES_STATUS_SERVER_DOWN = 1;
    public static final int RATES_STATUS_SERVER_INVALID = 2;
    public static final int RATES_STATUS_UNKNOWN = 3;
    public static final int RATES_STATUS_INVALID = 4;


    public MetalsExchangeSyncAdapterOld(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");


        //index is for gold
        //silver  platinum  copper palladium lead nickel aluminum zinc tin

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String ratesStr = null;

        try {
            final String RATES_BASE_URL = "http://xn--ceben6b.xn--5dbff.net/";
            final String RATES_BASE_URL_POST = ".php?";
            final String QUERY_PARAM = "u";
            //we need to query all metals
            for (String metalQuery : Utility.getAllMetalIds()) {
                Uri builtUri = Uri.parse(RATES_BASE_URL + metalQuery + RATES_BASE_URL_POST).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, "alt").build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("User-Agent", "Chrome/41.0.2272.89");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    setRatesStatus(getContext(), RATES_STATUS_SERVER_DOWN);
                    return;
                }
                ratesStr = buffer.toString();
                getRates(ratesStr, metalQuery);
            }
            notifyRates();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            setRatesStatus(getContext(), RATES_STATUS_SERVER_DOWN);
//            Handler mainHandler = new Handler(getContext().getMainLooper());
//            mainHandler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_no_response_from_server), Toast.LENGTH_LONG).show();
//                }
//            });
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
            setRatesStatus(getContext(), RATES_STATUS_SERVER_INVALID);
//            Handler mainHandler = new Handler(getContext().getMainLooper());
//            mainHandler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_server_response_not_valid), Toast.LENGTH_LONG).show();
//                }
//            });
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    private void getRates(String ratesStr, String metalQuery){
        Document doc = Jsoup.parse(ratesStr);
        Element table = doc.getElementsByTag("table").first();
        Iterator<Element> rows = table.select("tr").iterator();
        rows.next();//we skip the header

        // Insert the new rate information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(5);

        while (rows.hasNext()){
            Iterator<Element> values = rows.next().select("td").iterator();
            String eur = values.next().text();
            String gbp = values.next().text();
            String usd = values.next().text();
            usd = usd.substring(0, usd.indexOf(" "));
            String nis = values.next().text();
            String date = values.next().text();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date convertedDate = new Date();
            try{
                convertedDate = dateFormat.parse(date);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            double usdVal = parsePrice(usd);


            double nisVal = parsePrice(nis);
            double gbpVal = parsePrice(gbp);
            double eurVal = parsePrice(eur);

            ContentValues metalRatesValues = new ContentValues();
            metalRatesValues.put(MetalsContract.MetalsRateEntry.COLUMN_DATE, MetalsContract.normalizeDate(convertedDate.getTime()));
            metalRatesValues.put(MetalsContract.MetalsRateEntry.COLUMN_METAL_ID, metalQuery);
            metalRatesValues.put(MetalsContract.MetalsRateEntry.COLUMN_NIS_RATE, nisVal);
            metalRatesValues.put(MetalsContract.MetalsRateEntry.COLUMN_USD_RATE, usdVal);
            metalRatesValues.put(MetalsContract.MetalsRateEntry.COLUMN_GBP_RATE, gbpVal);
            metalRatesValues.put(MetalsContract.MetalsRateEntry.COLUMN_EUR_RATE, eurVal);
            cVVector.add(metalRatesValues);
        }
        int inserted = 0;
        // add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(MetalsContract.MetalsRateEntry.CONTENT_URI, cvArray);


            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // delete old data so we don't build up an endless history
            getContext().getContentResolver().delete(MetalsContract.MetalsRateEntry.CONTENT_URI,
                    MetalsContract.MetalsRateEntry.COLUMN_DATE + " <= ?",
                    new String[] {Long.toString(dayTime.setJulianDay(julianStartDay-100))});
        }

        Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");
        setRatesStatus(getContext(), RATES_STATUS_OK);

    }

    private double parsePrice(String stringPrice){
        double doubleVal = 0;
        try {
            doubleVal = Double.parseDouble(filterNonDigitChars(stringPrice));
            return doubleVal;
        }
        catch (Exception e){
           return 0;
        }
    }

    private String filterNonDigitChars(String raw){
        String numberRefined= raw.replaceAll("[^0-9.]", "");
        return numberRefined;
    }

    public static int getPreferredCurrencyColumnId(String currencyId) {
        if(currencyId.equals(Utility.CURRENCY_NIS)){
            return INDEX_NIS_RATE;
        } else if(currencyId.equals(Utility.CURRENCY_USD)){
            return INDEX_USD_RATE;
        }else if(currencyId.equals(Utility.CURRENCY_GBP)){
            return INDEX_GBP_RATE;
        }else if(currencyId.equals(Utility.CURRENCY_EUR)){
            return INDEX_EUR_RATE;
        }
        return INDEX_NIS_RATE;

    }

    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    private void notifyRates() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {

            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);
            long lastJulianDaySync = normalizeDate(lastSync);
            StringBuilder contentText = new StringBuilder();
            if (normalizeDate(System.currentTimeMillis()) > lastJulianDaySync) {
                String dateStr = Utility.getFriendlyDayString(context, System.currentTimeMillis()-DAY_IN_MILLIS);
                contentText.append(dateStr+" ["+Utility.getWeightName(Utility.isGrams(context), context)+"]"+":\n");
                // Last sync was more than 1 day ago, let's send a notification with the rates.
                boolean newRatesForToday = appendMetalRatesToNotification(Utility.GOLD, contentText, context);
                newRatesForToday |= appendMetalRatesToNotification(Utility.SILVER, contentText, context);
                newRatesForToday |= appendMetalRatesToNotification(Utility.PLATINUM, contentText, context);
                newRatesForToday |= appendMetalRatesToNotification(Utility.PALLADIUM, contentText, context);

                if(newRatesForToday) {
                    int iconId = R.drawable.ic_notification;
                    Resources resources = context.getResources();
                    Bitmap largeIcon = BitmapFactory.decodeResource(resources,
                            R.drawable.art_notification);
                    String title = context.getString(R.string.app_name);


                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setColor(resources.getColor(R.color.primary_light))
                                    .setSmallIcon(iconId)
                                    .setAutoCancel(true)
                                    .setLargeIcon(largeIcon)
                                    .setContentTitle(title)
                                    .setContentText(contentText.toString())
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText.toString()))
                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            ;

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // EXCHANGE_RATE_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(EXCHANGE_RATE_NOTIFICATION_ID, mBuilder.build());
                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, normalizeDate(System.currentTimeMillis()));
                    editor.commit();
                }
            }
        }
    }

    private boolean appendMetalRatesToNotification(String metalId, StringBuilder contentText, Context context){
        Uri metalUri = MetalsContract.MetalsRateEntry.buildMetalRatesWithDate(metalId, System.currentTimeMillis()-DAY_IN_MILLIS);
        Cursor cursor = context.getContentResolver().query(metalUri, NOTIFY_METAL_PROJECTION, null, null, null);
        boolean newRatesForToday = false;
        if (cursor.moveToFirst()) {
            newRatesForToday = true;
            // Read rate from cursor
            String rateNIS = Utility.getFormattedCurrency( cursor.getDouble(getPreferredCurrencyColumnId(Utility.CURRENCY_NIS)), Utility.CURRENCY_NIS, context, true);
            String rateUSD = Utility.getFormattedCurrency( cursor.getDouble(getPreferredCurrencyColumnId(Utility.CURRENCY_USD)), Utility.CURRENCY_USD, context, true);
            contentText.append(Utility.getMetalName(metalId, context)+": "+rateNIS+"/"+rateUSD+"\n");
        }
        cursor.close();
        return newRatesForToday;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MetalsExchangeSyncAdapterOld.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Sets the rates status into shared preference.  This function should not be called from
     * the UI thread because it uses commit to write to the shared preferences.
     * @param c Context to get the PreferenceManager from.
     * @param locationStatus The IntDef value to set
     */
    static private void setRatesStatus(Context c, @RatesStatus int locationStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_rates_status_key), locationStatus);
        spe.commit();
    }
}