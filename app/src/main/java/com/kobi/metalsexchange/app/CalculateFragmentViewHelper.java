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

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class CalculateFragmentViewHelper  {

    private String mExchangePrice;
    private double mPriceResult;

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

    public CalculateFragmentViewHelper(View rootView, Bundle arguments, final Context context) {

        if (arguments != null) {
            mMetalId = arguments.getString("METAL_ID");
            mMetalPrice = arguments.getDouble("CURRENT_VALUE");
            mDate = arguments.getLong("CURRENT_DATE");
        }

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
                calculate(context);
            }
        });

        mGoldPurityTextView = (TextView) rootView.findViewById(R.id.gold_purity_textview);
        mGoldPuritySpinner = (Spinner) rootView.findViewById(R.id.gold_purity_spinner);
        mGoldPuritySpinner.setAdapter(new ArrayAdapter<KaratEnum>(context, android.R.layout.simple_spinner_dropdown_item, KaratEnum.values()));
        mGoldPuritySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        calculate(context);
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
        String friendlyDateText = Utility.getFriendlyDayString(context, mDate);
        mFriendlyDateView.setText(friendlyDateText);

//        WeightUnitEnum unitEnum = Utility.isGrams(getActivity()) ? WeightUnitEnum.GRAM : WeightUnitEnum.TROY;
//        ArrayAdapter weightAdapter = (ArrayAdapter) mWeightSpinner.getAdapter();
//        int spinnerPosition = weightAdapter.getPosition(unitEnum);
//        mWeightSpinner.setSelection(spinnerPosition);


        String rate = Utility.getFormattedCurrency(mMetalPrice, Utility.getPreferredCurrency(context),context, false);
        mRateView.setText(rate);
        mRateUnitView.setText("("+Utility.getWeightName(Utility.isGrams(context), context)+")");

        if(mMetalId.equals(Utility.GOLD)){
            mGoldPurityTextView.setVisibility(View.VISIBLE);
            mGoldPuritySpinner.setVisibility(View.VISIBLE);
        }
        else{
            mGoldPurityTextView.setVisibility(View.GONE);
            mGoldPuritySpinner.setVisibility(View.GONE);
        }
    }

    private void calculate(Context context){
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

        mPriceResult = mMetalPrice * weight;
//        if(Utility.isGrams(getActivity()) != unitEnum.isGrams()){
//            if(unitEnum.isGrams()){
//                mPriceResult /= Utility.GRAMS_IN_OUNCE;
//            }
//            else{
//                mPriceResult *= Utility.GRAMS_IN_OUNCE;
//            }
//        }

        mPriceResult *= factor;
        String priceString = Utility.getFormattedCurrency(mPriceResult, Utility.getPreferredCurrency(context), context, false);
        metalPriceTextview.setText(priceString);

        //generate the share text
        String metalName = Utility.getMetalName(mMetalId, context);
        mExchangePrice = context.getString(R.string.app_name)+"("+mFriendlyDateView.getText()+")"+":\n"+
                "------"+metalName+"----"+"\n"+
                context.getString(R.string.calculator_weight) + " [" +Utility.getWeightName(Utility.isGrams(context), context)+"]" +"\n"+
                weight+"\n"+
                "================"+"\n"+
                priceString+"\n"+
                "================";
    }

    public String getExchangeSharePrice(){
        return mExchangePrice;
    }

    public double getPriceResult(){
        return mPriceResult;
    }


    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("mGoldPuritySpinner_postion", mGoldPuritySpinner.getSelectedItemPosition());
//        outState.putInt("mWeightSpinner_postion", mWeightSpinner.getSelectedItemPosition());
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            if (mGoldPuritySpinner != null) {
                mGoldPuritySpinner.setSelection(savedInstanceState.getInt("mGoldPuritySpinner_postion"));
            }
//            if (mWeightSpinner != null) {
//                mWeightSpinner.setSelection(savedInstanceState.getInt("mWeightSpinner_postion"));
//            }
        }
    }


}