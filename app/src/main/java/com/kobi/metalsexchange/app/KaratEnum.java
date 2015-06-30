package com.kobi.metalsexchange.app;

public enum KaratEnum {
    K_24("24 K", 1d),
    K_22("22 K", 22/24),
    K_18("18 K", 18/24),
    K_14("14 K", 14/24),
    K_10("10 K", 10/24),
    K_9("9 K", 9/24),
    K_8("8 K", 8/24);

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