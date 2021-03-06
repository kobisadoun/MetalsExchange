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

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                                       public void onIabSetupFinished(IabResult result)
                                       {
                                           if (!result.isSuccess()) {
                                               Log.d(LOG_TAG, "In-app Billing setup failed: " +
                                                       result);
                                           } else {
                                               Log.d(LOG_TAG, "In-app Billing is set up OK");
                                           }
                                       }
                                   });
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