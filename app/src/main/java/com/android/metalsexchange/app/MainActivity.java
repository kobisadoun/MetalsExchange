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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.metalsexchange.app.component.SlidingTabLayout;
import com.android.metalsexchange.app.sync.MetalsExchangeSyncAdapter;

public class MainActivity extends ActionBarActivity implements ExchangeRatesFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String CHARTFRAGMENT_TAG = "CFTAG";

    private String mCurrencyId;
    private boolean mGrams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrencyId = Utility.getPreferredCurrency(this);
        mGrams = Utility.isGrams(this);
        setContentView(R.layout.activity_main);

        Utility.setTwoPanesView(findViewById(R.id.rate_detail_container) != null);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new ExchangeRatesFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));

        // Give the SlidingTabLayout the ViewPager
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        // Set custom tab layout
        slidingTabLayout.setCustomTabView(R.layout.custom_tab, 0);
        // Center the tabs in the layout
        slidingTabLayout.setDistributeEvenly(true);

        // Customize tab color
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.RED;
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
                }
            }
        };
        slidingTabLayout.setOnPageChangeListener(pageChangeListener);
        viewPager.setCurrentItem(Utility.getTabIdxForMetal(Utility.getCurrentMetalId(this)));
        if (Utility.isTwoPanesView()) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
        else if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name)
                    .setIcon(R.drawable.ic_gold)
                    .setMessage(R.string.about)
                    .setCancelable(true)
                    .setNegativeButton(R.string.about_continue, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog welcomeAlert = builder.create();
            welcomeAlert.show();
            // Make the textview clickable. Must be called after show()
            ((TextView)welcomeAlert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String metalId = Utility.getCurrentMetalId(this);
        outState.putString("last_selected_metal", metalId);
    }


    @Override
    protected void onResume() {
        super.onResume();
        String currencyId = Utility.getPreferredCurrency(this);
        boolean grams = Utility.isGrams(this);
        if (currencyId != null && !currencyId.equals(mCurrencyId) ||
               grams != mGrams) {
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
            mGrams = grams;
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
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
