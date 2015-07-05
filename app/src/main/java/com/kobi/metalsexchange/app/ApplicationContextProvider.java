package com.kobi.metalsexchange.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApplicationContextProvider extends Application {
    public final String LOG_TAG = ApplicationContextProvider.class.getSimpleName();
    /**
     * Keeps a reference of the application context
     */
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        if(!prefs.contains(sContext.getString(R.string.pref_main_currency_key))){
            new CountryLocator().execute("");
        }

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
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                if (!buffer.toString().isEmpty()) {
                    String json = buffer.toString();
                    try {
                        JSONObject  jObj = new JSONObject(json);
                        String country = jObj.getString("country");
                        String countryCode = jObj.getString("countryCode");

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
                        SharedPreferences.Editor spe = prefs.edit();
                        String defaultCurrency;

                        switch(countryCode){
                            case "IL":
                                defaultCurrency = Utility.CURRENCY_ILS;
                                break;
                            case "GB":
                                defaultCurrency = Utility.CURRENCY_GBP;
                                break;
                            case "FR":
                                defaultCurrency = Utility.CURRENCY_EUR;
                                break;

                            default:
                                defaultCurrency = Utility.CURRENCY_USD;
                        }

                        if(countryCode.equalsIgnoreCase("IL")){
                            defaultCurrency = Utility.CURRENCY_ILS;
                        }

                        spe.putString(sContext.getString(R.string.pref_main_currency_key), defaultCurrency);
                        spe.commit();

                    } catch (JSONException e) {
                        Log.e("JSON Parser", "Error parsing data " + e.toString());
                    }

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