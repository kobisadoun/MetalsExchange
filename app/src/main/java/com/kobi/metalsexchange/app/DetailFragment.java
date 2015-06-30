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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kobi.metalsexchange.app.data.MetalsContract;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private ShareActionProvider mShareActionProvider;
    private String mExchangeRate;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MetalsContract.MetalsRateEntry.TABLE_NAME + "." + MetalsContract.MetalsRateEntry._ID,
            MetalsContract.MetalsRateEntry.COLUMN_DATE,
            MetalsContract.MetalsRateEntry.COLUMN_METAL_ID,
            MetalsContract.MetalsRateEntry.COLUMN_NIS_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_USD_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_GBP_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_EUR_RATE,
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_RATE_ID = 0;
    public static final int COL_RATE_DATE = 1;
    public static final int COL_RATE_METAL_ID = 2;
    public static final int COL_RATE_NIS_RATE = 3;
    public static final int COL_RATE_USD_RATE = 4;
    public static final int COL_RATE_GBP_RATE = 5;
    public static final int COL_RATE_EUR_RATE = 6;

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mRateView;
    private TextView mRateUnitView;
    private TextView mDeltaView;
    private TextView mCurrency1View;
    private TextView mCurrency2View;
    private TextView mCurrency3View;
    private ImageButton mShekelButtonView;
    private ImageButton mPidionButtonView;
    private double oneGramPrice = 0;

    private long mDate;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mRateView = (TextView) rootView.findViewById(R.id.detail_rate_textview);
        mRateUnitView = (TextView) rootView.findViewById(R.id.detail_rate_unit_textview);

        //mDeltaView = (TextView) rootView.findViewById(R.id.detail_delta_textview);
        mCurrency1View = (TextView) rootView.findViewById(R.id.detail_currency1_textview);
        mCurrency2View = (TextView) rootView.findViewById(R.id.detail_currency2_textview);
        mCurrency3View = (TextView) rootView.findViewById(R.id.detail_currency3_textview);

        mShekelButtonView = (ImageButton) rootView.findViewById(R.id.detail_shekel_button);
        mShekelButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create(); //Read Update
                alertDialog.setTitle(getString(R.string.zecher_machatzit_hashekel));

                double zecherMachatzitHashekelRaw = oneGramPrice * 10;
                zecherMachatzitHashekelRaw = (double)Math.round(zecherMachatzitHashekelRaw);
                String zecherMachatzitHashekel = Utility.getFormattedCurrency(zecherMachatzitHashekelRaw, Utility.getPreferredCurrency(getActivity()), getActivity(), false);

                String message = getString(R.string.zecher_machatzit_hashekel_details, zecherMachatzitHashekel);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.zecher_machatzit_hashekel)
                        .setIcon(R.drawable.shekel)
                        .setMessage(message)
                        .setCancelable(true)
                        .setNegativeButton(R.string.about_zecher_machatzit_hashekel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String url = "http://he.wikipedia.org/wiki/%D7%9E%D7%97%D7%A6%D7%99%D7%AA_%D7%94%D7%A9%D7%A7%D7%9C";
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                                dialog.dismiss();
                            }
                        });

                AlertDialog welcomeAlert = builder.create();
                welcomeAlert.show();
            }
        });

        mPidionButtonView = (ImageButton) rootView.findViewById(R.id.detail_pidion_button);
        mPidionButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double pidionHabenRaw = oneGramPrice * 100;
                pidionHabenRaw = (double)Math.round(pidionHabenRaw);
                String pidionHaben = Utility.getFormattedCurrency(pidionHabenRaw, Utility.getPreferredCurrency(getActivity()), getActivity(), false);

                String message = getString(R.string.pidyon_haben_details, pidionHaben);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.pidyon_haben)
                        .setIcon(R.drawable.ben)
                        .setMessage(message)
                        .setCancelable(true)
                        .setNegativeButton(R.string.about_pidyon_haben, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String url = "http://he.wikipedia.org/wiki/%D7%A4%D7%93%D7%99%D7%95%D7%9F_%D7%94%D7%91%D7%9F";
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                                dialog.dismiss();
                            }
                        });

                AlertDialog welcomeAlert = builder.create();
                welcomeAlert.show();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mExchangeRate != null) {
            mShareActionProvider.setShareIntent(createShareExchangeRatesIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_calculate) {
            String rateNumberRaw= mRateView.getText().toString().replaceAll("[^0-9,.]", "");
            NumberFormat nf = NumberFormat.getInstance();
            Bundle b = new Bundle();
            b.putString("METAL_ID", Utility.getCurrentMetalId(getActivity()));
            try {
                Double t = (Double) nf.parse(rateNumberRaw);
                b.putDouble("CURRENT_VALUE", t);
            }catch (Exception e){
                System.out.print("error");
            }
            //b.putDouble("CURRENT_VALUE", Double.valueOf(rateNumberRaw));
            b.putLong("CURRENT_DATE", mDate);
            Intent calculateIntent = new Intent(getActivity(), CalculateActivity.class);
            calculateIntent.putExtras(b);
            startActivity(calculateIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent createShareExchangeRatesIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mExchangeRate);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onCurrencyOrWeightChanged() {
        Uri uri = mUri;
        if (null != uri) {
            long date = MetalsContract.MetalsRateEntry.getDateFromUri(uri);
            Uri updatedUri = MetalsContract.MetalsRateEntry.buildMetalRatesWithDate(Utility.getCurrentMetalId(getActivity()), date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read metal ID from cursor
            String metalId = data.getString(COL_RATE_METAL_ID);

            // Use currency art image
            mIconView.setImageResource(Utility.getArtResourceForMetal(metalId));
            // Read date from cursor and update views for day of week and date
            mDate = data.getLong(COL_RATE_DATE);
            String friendlyDateText = Utility.getFriendlyDayString(getActivity(), mDate);
            mFriendlyDateView.setText(friendlyDateText);

//            // For accessibility, add a content description to the icon field
//            mIconView.setContentDescription(description);
            String preferredCurrency = Utility.getPreferredCurrency(getActivity());
            String rate = getRateForCurrency(data, preferredCurrency);
            mRateView.setText(rate);
            mRateUnitView.setText("("+Utility.getWeightName(Utility.isGrams(getActivity()), getActivity())+")");

            oneGramPrice = data.getDouble(Utility.getPreferredCurrencyColumnId(preferredCurrency));


            List<String> otherCurrencies = new ArrayList(Arrays.asList("nis", "usd", "gbp", "eur"));
            otherCurrencies.remove(preferredCurrency);

            String otherRate1 = getRateForCurrency(data, otherCurrencies.get(0));
            mCurrency1View.setText(otherRate1);
            String otherRate2 = getRateForCurrency(data, otherCurrencies.get(1));
            mCurrency2View.setText(otherRate2);
            String otherRate3 = getRateForCurrency(data, otherCurrencies.get(2));
            mCurrency3View.setText(otherRate3);

            String metalName = Utility.getMetalName(metalId, getActivity());
            mExchangeRate = getActivity().getString(R.string.app_name)+"("+friendlyDateText+" ["+Utility.getWeightName(Utility.isGrams(getActivity()), getActivity())+"])"+":\n"+
                    "------"+metalName+"----"+"\n"+
                    rate +"\n"+
                    otherRate1 +"\n"+
                    otherRate2 +"\n"+
                    otherRate3;

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareExchangeRatesIntent());
            }

            String showJewishCustomsCurrencyKey = getActivity().getString(R.string.pref_show_jewish_customs_key);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean showJewishCustomsCurrency = prefs.getBoolean(showJewishCustomsCurrencyKey,
                   Boolean.parseBoolean(getActivity().getString(R.string.pref_show_jewish_customs_default)));

            if(Utility.getCurrentMetalId(getActivity()).equals(Utility.SILVER) && showJewishCustomsCurrency){
                mShekelButtonView.setVisibility(View.VISIBLE);
                mPidionButtonView.setVisibility(View.VISIBLE);
            }
            else{
                mShekelButtonView.setVisibility(View.GONE);
                mPidionButtonView.setVisibility(View.GONE);
            }
        }
    }

    private String getRateForCurrency(Cursor data, String currencyId){
        double rateRaw = data.getDouble(Utility.getPreferredCurrencyColumnId(currencyId));
        String rate = Utility.getFormattedCurrency( rateRaw, currencyId, getActivity(), true);
        return rate;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

}