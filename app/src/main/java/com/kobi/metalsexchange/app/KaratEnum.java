package com.kobi.metalsexchange.app;

public enum KaratEnum {
    K_24(R.string.karat_enum_24, 1d),
    K_22(R.string.karat_enum_22, 22f/24f),
    K_18(R.string.karat_enum_18, 18f/24f),
    K_14(R.string.karat_enum_14, 14f/24f),
    K_10(R.string.karat_enum_10, 10f/24f),
    K_9(R.string.karat_enum_9, 9f/24f),
    K_8(R.string.karat_enum_8, 8f/24f);

    private int resourceId;
    private double factor;

    KaratEnum(int resourceId, double factor){
        this.resourceId = resourceId;
        this.factor = factor;
    }

    @Override public String toString(){
        return ApplicationContextProvider.getContext().getString(resourceId);
    }

    public double getFactor(){
        return factor;
    }
}