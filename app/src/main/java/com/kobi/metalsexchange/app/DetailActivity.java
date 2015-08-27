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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kobi.metalsexchange.app.data.MetalsContract;
import com.software.shell.fab.ActionButton;


public class DetailActivity extends AppCompatActivity implements FABHideable {

    private ActionButton mFloatingActionButton;
    public final String LOG_TAG = DetailActivity.class.getSimpleName();
    static final int RC_REQUEST = 10001;

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


    @Override
    protected void onResume() {
        super.onResume();
        if(mFloatingActionButton != null && MainActivity.mIsPremium) {
            mFloatingActionButton.playShowAnimation();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        mFloatingActionButton = (ActionButton) findViewById(R.id.action_button);
        mFloatingActionButton.setVisibility(MainActivity.mIsPremium ? View.VISIBLE : View.GONE);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DetailFragment.class.getSimpleName());
                Bundle b = new Bundle();
                b.putString("METAL_ID", Utility.getCurrentMetalId(DetailActivity.this));
                b.putDouble("CURRENT_VALUE", df.getRawRate());
                b.putLong("CURRENT_DATE", df.getDate());
                Intent calculateIntent = new Intent(DetailActivity.this, CalculateActivity.class);
                calculateIntent.putExtras(b);
                startActivity(calculateIntent);
            }
        });

        String metalId = MetalsContract.MetalsRateEntry.getMetalIdFromUri(getIntent().getData());

        setTitle(Utility.getMetalName(metalId, this));
        //  getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        Bitmap bm = BitmapFactory.decodeResource(getResources(),Utility.getIconResourceForMetal(metalId));
//        RoundImage roundedImage = new RoundImage(bm);
//        getSupportActionBar().setIcon(roundedImage);
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rate_detail_container, fragment, DetailFragment.class.getSimpleName())
                    .commit();



            arguments = new Bundle();
            arguments.putParcelable(TrendGraphFragment.TREND_GRAPH_URI, getIntent().getData());

            TrendGraphFragment fragmentChart = new TrendGraphFragment();
            fragmentChart.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rate_graph_container, fragmentChart)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}