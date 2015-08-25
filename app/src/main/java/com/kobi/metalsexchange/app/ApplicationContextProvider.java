package com.kobi.metalsexchange.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kobi.metalsexchange.app.inappbilling.util.IabHelper;
import com.kobi.metalsexchange.app.inappbilling.util.IabResult;
import com.kobi.metalsexchange.app.inappbilling.util.Inventory;
import com.kobi.metalsexchange.app.inappbilling.util.Purchase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Currency;
import java.util.Locale;

public class ApplicationContextProvider extends Application {

    public static boolean EMULATOR_MODE = "google_sdk".equals(Build.PRODUCT) || "sdk".equals(Build.PRODUCT) || "sdk_x86".equals(Build.PRODUCT) || "vbox86p".equals(Build.PRODUCT);
    public static final String SKU_CALCULATOR = "com.kobi.metalsexchange.app.calculator";
    public static boolean mHasCalculatorPurchase = false;

    public final String LOG_TAG = ApplicationContextProvider.class.getSimpleName();
    /**
     * Keeps a reference of the application context
     */
    private static Context sContext;

    private static IabHelper mHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        if(!prefs.contains(sContext.getString(R.string.pref_main_currency_key))){
            new CountryLocator().execute("");
        }
        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvKlgSMygNFSYdPzMaPo1ETiTscWSCoaIQt/aClqcyZtuGcqDhKj+/REn8KKH8jvL5kBDN3/4TAjkeLvsVdINOM/D3w+jSdwL+DZXFVvqYHI/siv9hBfM/J4uBCoF3VGn5L5PHgVQm092ZmrEtMJncjnwp4lKVVuEKRHXFvl/b+tS1H9YnY5F4ps2tMlU07v28sUAxb6EL9t2yQrkofHLC6NrsP8WVq5wfxhCNelZI+ssE4yD6iIwnLUuRd/xw9tZ49Qmat+0NIA5MhXLNWzukL9Ln4V19p34QsSao67vn3SUrakt4nWRnOVBorlclKUikDjOXyMrUodkKRpiYughKwIDAQAB";
        if(!EMULATOR_MODE) {
            mHelper = new IabHelper(this, base64EncodedPublicKey);
            mHelper.enableDebugLogging(true); //TODO Remove in production
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (result != null && !result.isSuccess()) {
                        Log.d(LOG_TAG, "In-app Billing setup failed: " + result);
                    } else {
                        Log.d(LOG_TAG, "In-app Billing is set up OK");
                    }

                    // IAB is fully set up. Now, let's get an inventory of stuff we own.
                    Log.d(LOG_TAG, "Setup successful. Querying inventory.");
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            });
        }
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(LOG_TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Log.d(LOG_TAG, "Failed to query inventory: " + result);
                return;
            }

            Log.d(LOG_TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase calculatorPurchase = inventory.getPurchase(SKU_CALCULATOR);
            mHasCalculatorPurchase = (calculatorPurchase != null && verifyDeveloperPayload(calculatorPurchase));
            Log.d(LOG_TAG, "User has calculator: " + mHasCalculatorPurchase);

            Log.d(LOG_TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    /** Verifies the developer payload of a purchase. */
    public static boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }


    public static IabHelper getIabHelper(){
        return mHelper;
    }

    /**
     * Returns the application context
     *
     * @return application context
     */
    public static Context getContext() {
        return sContext;
    }

    private class CountryLocator extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                String urlSite = "http://ip-api.com/json";
                Uri builtUri = Uri.parse(urlSite).buildUpon().build();
                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (!buffer.toString().isEmpty()) {
                    String json = buffer.toString();
                    try {
                        JSONObject  jObj = new JSONObject(json);
//                        String country = jObj.getString("country");
                        String countryCode = jObj.getString("countryCode");

                        Locale locale = null;
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            locale = new Locale.Builder().setRegion(countryCode).build();
                        }else{
                            locale = new Locale("", countryCode);
                        }

                        Currency currency = Currency.getInstance(locale);
                        String defaultCurrency = currency.getCurrencyCode();
                        if(Utility.isFreeSupportedCurrency(defaultCurrency)){
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
                            SharedPreferences.Editor spe = prefs.edit();
                            spe.putString(sContext.getString(R.string.pref_main_currency_key), defaultCurrency);
                            spe.commit();
                        }

                    } catch (JSONException e) {
                        Log.e("JSON Parser", "Error parsing data " + e.toString());
                    }
                }
                else{
                    Log.w(LOG_TAG, "Failed to get current location of device");
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            }
            catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
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

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}