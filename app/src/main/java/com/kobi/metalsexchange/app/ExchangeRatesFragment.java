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

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kobi.metalsexchange.app.data.MetalsContract;
import com.kobi.metalsexchange.app.sync.MetalsExchangeSyncAdapter;

/**T
 * Encapsulates fetching the rates and displaying it as a {@link RecyclerView} layout.
 */
public class ExchangeRatesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> , SharedPreferences.OnSharedPreferenceChangeListener{
    private ExchangeRatesAdapter mExchangeRatesAdapter;

    private RecyclerView  mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mPosition = RecyclerView.NO_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int RATES_LOADER = 0;
    private static final String[] RATES_COLUMNS = {
            MetalsContract.MetalsRateEntry.TABLE_NAME + "." + MetalsContract.MetalsRateEntry._ID,
            MetalsContract.MetalsRateEntry.COLUMN_DATE,
            MetalsContract.MetalsRateEntry.COLUMN_METAL_ID,
            MetalsContract.MetalsRateEntry.COLUMN_ILS_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_USD_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_GBP_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_EUR_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_CAD_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_DKK_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_NOK_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_SEK_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_CHF_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_JOD_RATE,
            MetalsContract.MetalsRateEntry.COLUMN_EGP_RATE
    };

    // These indices are tied to RATES_COLUMNS.  If RATES_COLUMNS changes, these
    // must change.
    public static final int COL_RATE_ID = 0;
    public static final int COL_RATE_DATE = 1;
    public static final int COL_RATE_METAL_ID = 2;
    public static final int COL_RATE_ILS = 3;
    public static final int COL_RATE_USD = 4;
    public static final int COL_RATE_GBP = 5;
    public static final int COL_RATE_EUR = 6;
    public static final int COL_RATE_CAD = 7;
    public static final int COL_RATE_DKK= 8;
    public static final int COL_RATE_NOK = 9;
    public static final int COL_RATE_SEK = 10;
    public static final int COL_RATE_CHF = 11;
    public static final int COL_RATE_JOD = 12;
    public static final int COL_RATE_EGP = 13;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
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
//        if (id == R.id.action_refresh) {
//            updateRates();
//            return true;
//        }
        if (id == R.id.action_calculate) {
            ExchangeRatesAdapter adapter = (ExchangeRatesAdapter)mRecyclerView.getAdapter();
            int position = mPosition;
            if(position != RecyclerView.NO_POSITION) {
                adapter.getCursor().moveToPosition(position);
                // Read date from cursor
                long dateInMillis = adapter.getCursor().getLong(ExchangeRatesFragment.COL_RATE_DATE);
                double rateRaw = adapter.getCursor().getDouble(Utility.getPreferredCurrencyColumnId(Utility.getPreferredCurrency(getActivity())));

                Bundle b = new Bundle();
                b.putString("METAL_ID", Utility.getCurrentMetalId(getActivity()));
                b.putDouble("CURRENT_VALUE", rateRaw);
                b.putLong("CURRENT_DATE", dateInMillis);


                FragmentManager fm = getActivity().getSupportFragmentManager();
                CalculatorDialogFragment myDialogFragment = new CalculatorDialogFragment();
                myDialogFragment.setArguments(b);
                //myDialogFragment.getDialog().setTitle(getResources().getString(R.string.calculator_fragment_name));
                myDialogFragment.show(fm, "dialog_fragment");
            }

//            ArrayList<HashMap<String,Object>> items =new ArrayList<HashMap<String,Object>>();
//            final PackageManager pm = getActivity().getPackageManager();
//            List<PackageInfo> packs = pm.getInstalledPackages(0);
//            for (PackageInfo pi : packs) {
//                if( pi.packageName.toString().toLowerCase().contains("calcul")){
//                    HashMap<String, Object> map = new HashMap<String, Object>();
//                    map.put("appName", pi.applicationInfo.loadLabel(pm));
//                    map.put("packageName", pi.packageName);
//                    items.add(map);
//                }
//            }
//            if(items.size()>=1){
//                String packageName = (String) items.get(0).get("packageName");
//                Intent i = pm.getLaunchIntentForPackage(packageName);
//                if (i != null)
//                    startActivity(i);
//            }
//            else{
//                // Application not found
//            }
//
//
//

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(R.color.primary_dark, R.color.primary_light, R.color.primary);
        //mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateRates();
                //mSwipeRefreshLayout.setRefreshing(false);
            }
        });


        // Get a reference to the ListView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_rates);

        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        View emptyView = rootView.findViewById(R.id.recyclerview_rates_empty);
        mRecyclerView.setAdapter(mExchangeRatesAdapter);


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        mExchangeRatesAdapter = new ExchangeRatesAdapter(getActivity(), mMetalId, new ExchangeRatesAdapter.ExchangeRatesAdapterOnClickHandler() {

            @Override
            public void onClick(Long date, ExchangeRatesAdapter.RatesAdapterViewHolder vh) {

                ((Callback) getActivity())
                        .onItemSelected(MetalsContract.MetalsRateEntry.buildMetalRatesWithDate(
                                mMetalId, date)
                        );
                mPosition = vh.getAdapterPosition();
            }

            @Override
            public void onRefreshCurrent() {
                if(Utility.isNetworkAvailable(getActivity())) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    MetalsExchangeSyncAdapter.syncImmediately(getActivity(), true);
                }
            }
        }, emptyView);
        mRecyclerView.setAdapter(mExchangeRatesAdapter);
        // We'll call our MainActivity
//        mRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                // CursorAdapter returns a cursor at the correct position for getItem(), or null
//                // if it cannot seek to that position.
//                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
//                if (cursor != null) {
//                    ((Callback) getActivity())
//                            .onItemSelected(MetalsContract.MetalsRateEntry.buildMetalRatesWithDate(
//                                    mMetalId, cursor.getLong(COL_RATE_DATE)
//                            ));
//                }
//                mPosition = position;
//            }
//        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The RecyclerView  probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mExchangeRatesAdapter.setUseTodayLayout(!Utility.isTwoPanesView());

        return rootView;
    }

    @Override
       public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
        getLoaderManager().restartLoader(RATES_LOADER, null, this);
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(RATES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateRates() {
        if(Utility.isNetworkAvailable(getActivity())) {
            MetalsExchangeSyncAdapter.syncImmediately(getActivity(), false);
        }
        else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to RecyclerView.NO_POSITION,
        // so check for that before storing.
        if (mPosition != RecyclerView.NO_POSITION) {
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
        if (mPosition != RecyclerView.NO_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
        updateEmptyView();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void onCurrencyOrWeightChanged() {
        getLoaderManager().restartLoader(RATES_LOADER, null, this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mExchangeRatesAdapter.swapCursor(null);
    }

    /*
    Updates the empty list view with contextually relevant information that the user can
    use to determine why they aren't seeing weather.
    */
    private void updateEmptyView() {
        if ( mExchangeRatesAdapter.getItemCount() == 0 ) {
            TextView tv = (TextView) getView().findViewById(R.id.recyclerview_rates_empty);
            if ( null != tv ) {
                // if cursor is empty, why? do we have an invalid location
                //String message =  getActivity().getResources().getString(R.string.empty_rates_list);
                String message = "";
                if(Utility.syncAllAvailableDataOfThisYear(getActivity())){//on first launch --> no data
                    message =  getActivity().getResources().getString(R.string.empty_rates_list_startup);
                }
                @MetalsExchangeSyncAdapter.RatesStatus int location = Utility.getRatesStatus(getActivity());
                switch (location) {
                    case MetalsExchangeSyncAdapter.RATES_STATUS_SERVER_DOWN:
                        message =  getActivity().getResources().getString(R.string.empty_rates_list_server_down);
                        break;
                    case MetalsExchangeSyncAdapter.RATES_STATUS_SERVER_INVALID:
                        message =  getActivity().getResources().getString(R.string.empty_rates_list_server_error);
                        break;
                    default:
                        if (!Utility.isNetworkAvailable(getActivity()) ) {
                            message =  getActivity().getResources().getString(R.string.empty_rates_list_no_network);
                        }
                }
                tv.setText(message);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ( key.equals(getString(R.string.pref_rates_status_key)) ) {
            updateEmptyView();
        }
        else{
            onCurrencyOrWeightChanged();
        }
    }

}