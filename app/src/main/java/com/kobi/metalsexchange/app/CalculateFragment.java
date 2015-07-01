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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CalculateFragment extends Fragment {

    private static final String LOG_TAG = CalculateFragment.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;
    private String mExchangePrice;

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mRateView;
    private TextView mRateUnitView;
    private EditText mWeightEditText;
    private TextView metalPriceTextview;
    private TextView mGoldPurityTextView;
    private Spinner mGoldPuritySpinner;
//    private Spinner mWeightSpinner;

    private String mMetalId;
    private double mMetalPrice;
    private long mDate;

    public CalculateFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mMetalId = arguments.getString("METAL_ID");
            mMetalPrice = arguments.getDouble("CURRENT_VALUE");
            mDate = arguments.getLong("CURRENT_DATE");
        }

        View rootView = inflater.inflate(R.layout.fragment_calculate, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mRateView = (TextView) rootView.findViewById(R.id.detail_rate_textview);
        metalPriceTextview = (TextView) rootView.findViewById(R.id.metal_price_textview);
        mRateUnitView = (TextView) rootView.findViewById(R.id.detail_rate_unit_textview);
        mWeightEditText = (EditText) rootView.findViewById(R.id.weight_textview);
        mWeightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                calculate();
            }
        });

        mGoldPurityTextView = (TextView) rootView.findViewById(R.id.gold_purity_textview);
        mGoldPuritySpinner = (Spinner) rootView.findViewById(R.id.gold_purity_spinner);
        mGoldPuritySpinner.setAdapter(new ArrayAdapter<KaratEnum>(getActivity(), android.R.layout.simple_spinner_dropdown_item, KaratEnum.values()));
        mGoldPuritySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        calculate();
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

//        mWeightSpinner = (Spinner) rootView.findViewById(R.id.weight_unit_spinner);
//        mWeightSpinner.setAdapter(new ArrayAdapter<WeightUnitEnum>(getActivity(), android.R.layout.simple_spinner_dropdown_item, WeightUnitEnum.values()));
//        mWeightSpinner.setOnItemSelectedListener(
//                new AdapterView.OnItemSelectedListener() {
//                    public void onItemSelected(
//                            AdapterView<?> parent, View view, int position, long id) {
//                        calculate();
//                    }
//
//                    public void onNothingSelected(AdapterView<?> parent) {
//                    }
//                });


        mIconView.setImageResource(Utility.getArtResourceForMetal(mMetalId));
        String friendlyDateText = Utility.getFriendlyDayString(getActivity(), mDate);
        mFriendlyDateView.setText(friendlyDateText);

//        WeightUnitEnum unitEnum = Utility.isGrams(getActivity()) ? WeightUnitEnum.GRAM : WeightUnitEnum.TROY;
//        ArrayAdapter weightAdapter = (ArrayAdapter) mWeightSpinner.getAdapter();
//        int spinnerPosition = weightAdapter.getPosition(unitEnum);
//        mWeightSpinner.setSelection(spinnerPosition);


        String rate = Utility.getFormattedCurrency(mMetalPrice, Utility.getPreferredCurrency(getActivity()), getActivity(), false);
        mRateView.setText(rate);
        mRateUnitView.setText("("+Utility.getWeightName(Utility.isGrams(getActivity()), getActivity())+")");

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareExchangeRatesIntent());
        }


        if(mMetalId.equals(Utility.GOLD)){
            mGoldPurityTextView.setVisibility(View.VISIBLE);
            mGoldPuritySpinner.setVisibility(View.VISIBLE);
        }
        else{
            mGoldPurityTextView.setVisibility(View.GONE);
            mGoldPuritySpinner.setVisibility(View.GONE);
        }
        return rootView;
    }

    private void calculate(){
        //WeightUnitEnum unitEnum = (WeightUnitEnum)mWeightSpinner.getSelectedItem();
        double weight = 0;
        try {
            weight = Double.parseDouble(mWeightEditText.getText().toString());
        }
        catch (Exception e){
            metalPriceTextview.setText("");
            mExchangePrice = "";
            return;
        }

        double factor = 1;
        if(mMetalId.equals(Utility.GOLD)){
            KaratEnum karatEnum = (KaratEnum)mGoldPuritySpinner.getSelectedItem();
            factor = karatEnum.getFactor();
        }

        double priceResult = mMetalPrice * weight;
//        if(Utility.isGrams(getActivity()) != unitEnum.isGrams()){
//            if(unitEnum.isGrams()){
//                priceResult /= Utility.GRAMS_IN_OUNCE;
//            }
//            else{
//                priceResult *= Utility.GRAMS_IN_OUNCE;
//            }
//        }

        priceResult *= factor;
        String priceString = Utility.getFormattedCurrency(priceResult, Utility.getPreferredCurrency(getActivity()), getActivity(), false);
        metalPriceTextview.setText(priceString);

        //generate the share text
        String metalName = Utility.getMetalName(mMetalId, getActivity());
        mExchangePrice = getActivity().getString(R.string.app_name)+"("+mFriendlyDateView.getText()+")"+":\n"+
                "------"+metalName+"----"+"\n"+
                getActivity().getString(R.string.calculator_weight)+" ["+Utility.getWeightName(Utility.isGrams(getActivity()), getActivity())+"]" +"\n"+
                weight+"\n"+
                "================"+"\n"+
                priceString+"\n"+
                "================";
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.calculatefragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mExchangePrice != null) {
            mShareActionProvider.setShareIntent(createShareExchangeRatesIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_copy) {
            String rateNumberRaw = metalPriceTextview.getText().toString().replaceAll("[^0-9,.]", "");
            // Gets a handle to the clipboard service.
            ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("rate",rateNumberRaw);
            clipboard.setPrimaryClip(clip);

            Toast toast = Toast.makeText(getActivity().getBaseContext(), getString(R.string.copy_to_clipboard_toast, rateNumberRaw),
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent createShareExchangeRatesIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mExchangePrice);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("mGoldPuritySpinner_postion", mGoldPuritySpinner.getSelectedItemPosition());
//        outState.putInt("mWeightSpinner_postion", mWeightSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            if (mGoldPuritySpinner != null) {
                mGoldPuritySpinner.setSelection(savedInstanceState.getInt("mGoldPuritySpinner_postion"));
            }
//            if (mWeightSpinner != null) {
//                mWeightSpinner.setSelection(savedInstanceState.getInt("mWeightSpinner_postion"));
//            }
        }
        super.onViewStateRestored(savedInstanceState);
    }
}