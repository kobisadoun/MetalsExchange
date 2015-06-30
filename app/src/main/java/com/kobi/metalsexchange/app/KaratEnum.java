package com.kobi.metalsexchange.app;

public enum KaratEnum {
    K_24("24 K", 1d),
    K_22("22 K", 22f/24f),
    K_18("18 K", 18f/24f),
    K_14("14 K", 14f/24f),
    K_10("10 K", 10f/24f),
    K_9("9 K", 9f/24f),
    K_8("8 K", 8f/24f);

    private String displayString;
    private double factor;

    private KaratEnum(String displayString, double factor){
        this.displayString = displayString;
        this.factor = factor;
    }

    @Override public String toString(){
        return displayString;
    }

    public double getFactor(){
        return factor;
    }
}