package com.kobi.metalsexchange.app;

public enum WeightUnitEnum {
    GRAM(R.string.pref_units_label_grams),
    TROY(R.string.pref_units_label_ounce);

    private int resourceId;

    private WeightUnitEnum(int resourceId){
        this.resourceId = resourceId;
    }

    @Override public String toString(){
        return ApplicationContextProvider.getContext().getString(resourceId);
    }

}