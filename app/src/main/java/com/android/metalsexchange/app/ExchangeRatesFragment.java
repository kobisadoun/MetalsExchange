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
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.metalsexchange.app.data.MetalsContract;
import com.android.metalsexchange.app.sync.MetalsExchangeSyncAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Encapsulates fetching the rates and displaying it as a {@link ListView} layout.
 */
public class ExchangeRatesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private ExchangeRatesAdapter mExchangeRatesAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int RATES_LOADER = 0;
    private static final String[] RATES_COLUMNS = {
            MetalsContract.MetalsRateEntry.TABLE_NAME + "." + MetalsContract.MetalsRateEntry._ID,
            MetalsContract.MetalsRateEntry.COLUMN_DATE,
            MetalsContract.MetalsRateEntry.COLUMN_METAL_ID,
            MetalsContract.MetalsRateEntry.COLUMN_NIS_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_USD_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_GBP_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_EUR_RATE
    };

    // These indices are tied to RATES_COLUMNS.  If RATES_COLUMNS changes, these
    // must change.
    static final int COL_RATE_ID = 0;
    static final int COL_RATE_DATE = 1;
    static final int COL_RATE_METAL_ID = 2;
    static final int COL_RATE_NIS = 3;
    static final int COL_RATE_USD = 4;
    static final int COL_RATE_GBP = 5;
    static final int COL_RATE_EUR = 6;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public static final String METAL_ID = "METAL_ID";

    private String mMetalId;

    public static ExchangeRatesFragment newInstance(String metalId) {
        Bundle args = new Bundle();
        args.putString(METAL_ID, metalId);
        ExchangeRatesFragment fragment = new ExchangeRatesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMetalId = getArguments().getString(METAL_ID);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ratesfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateRates();
            return true;
        }
        if (id == R.id.action_calculate) {
            ArrayList<HashMap<String,Object>> items =new ArrayList<HashMap<String,Object>>();
            final PackageManager pm = getActivity().getPackageManager();
            List<PackageInfo> packs = pm.getInstalledPackages(0);
            for (PackageInfo pi : packs) {
                if( pi.packageName.toString().toLowerCase().contains("calcul")){
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("appName", pi.applicationInfo.loadLabel(pm));
                    map.put("packageName", pi.packageName);
                    items.add(map);
                }
            }
            if(items.size()>=1){
                String packageName = (String) items.get(0).get("packageName");
                Intent i = pm.getLaunchIntentForPackage(packageName);
                if (i != null)
                    startActivity(i);
            }
            else{
                // Application not found
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The ExchangeRatesAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mExchangeRatesAdapter = new ExchangeRatesAdapter(getActivity(), null, 0, mMetalId);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_rates);
        mListView.setAdapter(mExchangeRatesAdapter);
        // We'll call our MainActivity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(MetalsContract.MetalsRateEntry.buildMetalRatesWithDate(
                                    mMetalId, cursor.getLong(COL_RATE_DATE)
                            ));
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mExchangeRatesAdapter.setUseTodayLayout(!Utility.isTwoPanesView());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(RATES_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(RATES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

//    void onCurrencyOrWeightChanged() {
//        updateRates();
//        getLoaderManager().restartLoader(RATES_LOADER, null, this);
//    }

    private void updateRates() {
        if(isOnline()) {
            MetalsExchangeSyncAdapter.syncImmediately(getActivity());
        }
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()){
            Toast.makeText(getActivity(), getResources().getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Sort order:  Descending, by date.
        String sortOrder = MetalsContract.MetalsRateEntry.COLUMN_DATE + " DESC";

        Uri ratesByMetalUri = MetalsContract.MetalsRateEntry.buildMetalsRateWithStartDate(
                mMetalId, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                ratesByMetalUri,
                RATES_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mExchangeRatesAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    void onCurrencyOrWeightChanged() {
        updateRates();
        getLoaderManager().restartLoader(RATES_LOADER, null, this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mExchangeRatesAdapter.swapCursor(null);
    }

}