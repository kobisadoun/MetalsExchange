package com.kobi.metalsexchange.app;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class CalculatorDialogFragment extends DialogFragment {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calculate, container);
        rootView.setPadding(24,24,24,24);
        Bundle arguments = getArguments();
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

        if(mMetalId.equals(Utility.GOLD)){
            mGoldPurityTextView.setVisibility(View.VISIBLE);
            mGoldPuritySpinner.setVisibility(View.VISIBLE);
        }
        else{
            mGoldPurityTextView.setVisibility(View.GONE);
            mGoldPuritySpinner.setVisibility(View.GONE);
        }
        getDialog().setTitle(getResources().getString(R.string.calculator_fragment_name));
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
    }

}