package com.kobi.metalsexchange.app;

public enum WeightUnitEnum {
    GRAM(R.string.pref_units_label_grams, true),
    TROY(R.string.pref_units_label_ounce, false);

    private int resourceId;
    private boolean isGrams;

    WeightUnitEnum(int resourceId, boolean grams){
        this.resourceId = resourceId;
        this.isGrams = grams;
    }

    @Override public String toString(){
        return ApplicationContextProvider.getContext().getString(resourceId);
    }

    public boolean isGrams(){
        return isGrams;
    }

}