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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class CalculateFragment extends Fragment implements CalculateFragmentViewHelper.CalculateListener {

    private static final String LOG_TAG = CalculateFragment.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;
    private CalculateFragmentViewHelper calculateFragmentViewHelper;

    public CalculateFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void priceCalculated(String calcResult){
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareExchangeRatesIntent(calcResult));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        View rootView = inflater.inflate(R.layout.fragment_calculate, container, false);
        calculateFragmentViewHelper = new CalculateFragmentViewHelper(rootView, arguments, getActivity(), this);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.calculatefragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareExchangeRatesIntent(""));
        }
    }

    private Intent createShareExchangeRatesIntent(String calcResult) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, calcResult);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_copy) {
            // Gets a handle to the clipboard service.
            ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

            String price = Utility.getFormattedCurrency(calculateFragmentViewHelper.getPriceResult(), Utility.getPreferredCurrency(getActivity()), getActivity(), false);

            ClipData clip = ClipData.newPlainText("rate",price);
            clipboard.setPrimaryClip(clip);

            Toast toast = Toast.makeText(getActivity().getBaseContext(), getString(R.string.copy_to_clipboard_toast, price),
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        calculateFragmentViewHelper.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        calculateFragmentViewHelper.onViewStateRestored(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }
}