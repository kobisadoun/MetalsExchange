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
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kobi.metalsexchange.app.component.SlidingTabLayout;
import com.kobi.metalsexchange.app.inappbilling.util.IabHelper;
import com.kobi.metalsexchange.app.inappbilling.util.IabResult;
import com.kobi.metalsexchange.app.inappbilling.util.Inventory;
import com.kobi.metalsexchange.app.inappbilling.util.Purchase;
import com.kobi.metalsexchange.app.sync.MetalsExchangeSyncAdapter;
import com.software.shell.fab.ActionButton;

public class MainActivity extends AppCompatActivity implements ExchangeRatesFragment.Callback, FABHideable, SharedPreferences.OnSharedPreferenceChangeListener {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String CHARTFRAGMENT_TAG = "CFTAG";

    private String mCurrencyId;
    private String mWeightUnitId;

    private ActionButton mFloatingActionButton;
    private TextView mLastUpdatedTextView;


    public static boolean EMULATOR_MODE = /*"google_sdk".equals(Build.PRODUCT) || "sdk".equals(Build.PRODUCT) || "sdk_x86".equals(Build.PRODUCT) ||*/ "vbox86p".equals(Build.PRODUCT);
    public static final String SKU_PREMIUM = "com.kobi.metalsexchange.app.premium";
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    public static boolean mIsPremium = false;
    private IabHelper mHelper;

    @Override
    public void hideOrShowFloatingActionButton(){
        if(mFloatingActionButton != null && MainActivity.mIsPremium) {
            if (mFloatingActionButton.isHidden()) {
                mFloatingActionButton.show();
            } else {
                mFloatingActionButton.hide();
            }
        }
    }

    @Override
    public void hideFloatingActionButton(){
        if(mFloatingActionButton != null && MainActivity.mIsPremium) {
            mFloatingActionButton.hide();
        }
    }

    @Override
    public void showFloatingActionButton(){
        if(mFloatingActionButton != null && MainActivity.mIsPremium) {
            mFloatingActionButton.show();
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
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(LOG_TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

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

    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        Log.d(LOG_TAG, "Destroying helper.");
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvKlgSMygNFSYdPzMaPo1ETiTscWSCoaIQt/aClqcyZtuGcqDhKj+/REn8KKH8jvL5kBDN3/4TAjkeLvsVdINOM/D3w+jSdwL+DZXFVvqYHI/siv9hBfM/J4uBCoF3VGn5L5PHgVQm092ZmrEtMJncjnwp4lKVVuEKRHXFvl/b+tS1H9YnY5F4ps2tMlU07v28sUAxb6EL9t2yQrkofHLC6NrsP8WVq5wfxhCNelZI+ssE4yD6iIwnLUuRd/xw9tZ49Qmat+0NIA5MhXLNWzukL9Ln4V19p34QsSao67vn3SUrakt4nWRnOVBorlclKUikDjOXyMrUodkKRpiYughKwIDAQAB";
        if(!EMULATOR_MODE) {
            mHelper = new IabHelper(this, base64EncodedPublicKey);
           // mHelper.enableDebugLogging(true); //TODO Remove in production
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
        else{
            mIsPremium = true;
        }


        mCurrencyId = Utility.getPreferredCurrency(this);
        mWeightUnitId = Utility.getPreferredWeightUnit(this);
        setContentView(R.layout.activity_main);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        Utility.setTwoPanesView(findViewById(R.id.rate_detail_container) != null);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new ExchangeRatesFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));

        // Give the SlidingTabLayout the ViewPager
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        // Set custom tab layout
        // /*with icon*/ slidingTabLayout.setCustomTabView(R.layout.custom_tab, 0);
        // Center the tabs in the layout
        slidingTabLayout.setDistributeEvenly(false);

//        slidingTabLayout.setBackgroundColor(getResources().getColor(R.color.primary));
//
//        // Customize tab color
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.white);
            }
        });

        slidingTabLayout.setViewPager(viewPager);

        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Utility.setCurrentMetalId(Utility.getMetalIdForTabPosition(position), MainActivity.this);
                //pageSelected = position;
                if (Utility.isTwoPanesView()) {
                    DetailFragment detailFragment = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                    TrendGraphFragment trendGraphFragment = (TrendGraphFragment)getSupportFragmentManager().findFragmentByTag(CHARTFRAGMENT_TAG);
                    if(detailFragment != null) {
                        getSupportFragmentManager().beginTransaction().remove(detailFragment).commit();
                    }
                    if(trendGraphFragment != null) {
                        getSupportFragmentManager().beginTransaction().remove(trendGraphFragment).commit();
                    }
                    if(mFloatingActionButton != null) {
                        mFloatingActionButton.hide();
                    }
                }
            }
        };
        slidingTabLayout.setOnPageChangeListener(pageChangeListener);
        viewPager.setCurrentItem(Utility.getTabIdxForMetal(Utility.getCurrentMetalId(this)));

        mLastUpdatedTextView = (TextView) findViewById(R.id.last_updated_textview);
        updateTheLastUpdatedTime();
        if (Utility.isTwoPanesView()) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.


            mFloatingActionButton = (ActionButton) findViewById(R.id.action_button);
            if(mFloatingActionButton != null) {
                mFloatingActionButton.hide();
                mFloatingActionButton.setVisibility(MainActivity.mIsPremium ? View.VISIBLE : View.GONE);
                mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                        Bundle b = new Bundle();
                        b.putString("METAL_ID", Utility.getCurrentMetalId(MainActivity.this));
                        b.putDouble("CURRENT_VALUE", df.getRawRate());
                        b.putLong("CURRENT_DATE", df.getDate());
                        FragmentManager fm = MainActivity.this.getSupportFragmentManager();
                        CalculatorDialogFragment myDialogFragment = new CalculatorDialogFragment();
                        myDialogFragment.setArguments(b);
                        //myDialogFragment.getDialog().setTitle(getResources().getString(R.string.calculator_fragment_name));
                        myDialogFragment.show(fm, "dialog_fragment");
                    }
                });
            }

            if (savedInstanceState == null) {
                DetailFragment detailFragment = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                TrendGraphFragment trendGraphFragment = (TrendGraphFragment)getSupportFragmentManager().findFragmentByTag(CHARTFRAGMENT_TAG);
                if(detailFragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(detailFragment).commit();
                }
                if(trendGraphFragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(trendGraphFragment).commit();
                }
            }
        } else {
            getSupportActionBar().setElevation(0f);
        }
        MetalsExchangeSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if(key.equals("LAST_UPDATED")) {
            updateTheLastUpdatedTime();
        }
    }

    private void updateTheLastUpdatedTime(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long lastUpdatedVal = prefs.getLong("LAST_UPDATED", 0);
        if(lastUpdatedVal > 0) {
            mLastUpdatedTextView.setText(Utility.getFriendlyDayTimeString(this, lastUpdatedVal));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Gets called every time the user presses the menu button.
     * Use if your menu is dynamic.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mIsPremium) {
            menu.removeItem(R.id.action_upgrade_to_premium);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
//        else if (id == R.id.action_about) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle(R.string.app_name)
//                    .setIcon(R.drawable.ic_gold)
//                    .setMessage(R.string.about)
//                    .setCancelable(true)
//                    .setNegativeButton(R.string.about_continue, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
//                        }
//                    });
//
//            AlertDialog welcomeAlert = builder.create();
//            welcomeAlert.show();
//            // Make the textview clickable. Must be called after show()
//            ((TextView)welcomeAlert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
//            return true;
//        }
        else if (id == R.id.action_upgrade_to_premium) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(item.getTitle())
                    .setMessage(getResources().getString(R.string.upgrade_to_premium_description))
                    .setCancelable(true)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String payload = "";
                            mHelper.launchPurchaseFlow(MainActivity.this, SKU_PREMIUM, RC_REQUEST, mPurchaseFinishedListener, payload);
                            dialog.dismiss();
                        }
                    });

            AlertDialog premiumAlert = builder.create();
            premiumAlert.show();
            return true;
        }
        else if (id == R.id.action_rate) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.app_name)
                        .setMessage("Couldn't launch the market")
                        .setCancelable(true);

                AlertDialog errorAlert = builder.create();
                errorAlert.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(LOG_TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                return;
            }

            if (!verifyDeveloperPayload(purchase)) {
                return;
            }

            Log.d(LOG_TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_PREMIUM)) {
                Log.d(LOG_TAG, "Premium was purchased.");
                mIsPremium = true;
                if(mFloatingActionButton != null) {
                    mFloatingActionButton.setVisibility(MainActivity.mIsPremium ? View.VISIBLE : View.GONE);
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(LOG_TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String metalId = Utility.getCurrentMetalId(this);
        outState.putString("last_selected_metal", metalId);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);
        updateTheLastUpdatedTime();


        String currencyId = Utility.getPreferredCurrency(this);
        String weightUnitId = Utility.getPreferredWeightUnit(this);
        if (currencyId != null && !currencyId.equals(mCurrencyId) ||
                !weightUnitId.equals(mWeightUnitId)) {
//            ExchangeRatesFragment ff = (ExchangeRatesFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_rates);
//            if ( null != ff ) {
//                ff.onCurrencyOrWeightChanged();
//            }
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onCurrencyOrWeightChanged();
            }
            TrendGraphFragment dgf = (TrendGraphFragment)getSupportFragmentManager().findFragmentByTag(CHARTFRAGMENT_TAG);
            if ( null != dgf ) {
                dgf.onCurrencyOrWeightChanged();
            }
            mCurrencyId = currencyId;
            mWeightUnitId = weightUnitId;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (Utility.isTwoPanesView()) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rate_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();


            args = new Bundle();
            args.putParcelable(TrendGraphFragment.TREND_GRAPH_URI, contentUri);
            TrendGraphFragment fragmentChart = new TrendGraphFragment();
            fragmentChart.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rate_graph_container, fragmentChart, CHARTFRAGMENT_TAG)
                    .commit();
            if(MainActivity.mIsPremium) {
                mFloatingActionButton.show();
            }
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
