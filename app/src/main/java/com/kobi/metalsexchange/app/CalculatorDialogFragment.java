package com.kobi.metalsexchange.app;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CalculatorDialogFragment extends DialogFragment {

    private CalculateFragmentViewHelper calculateFragmentViewHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calculate, container);
        Bundle arguments = getArguments();
        calculateFragmentViewHelper = new CalculateFragmentViewHelper(rootView, arguments, getActivity(), null);
        rootView.setPadding(24,24,24,24);
        getDialog().setTitle(getResources().getString(R.string.calculator_fragment_name));
        return rootView;
    }

}