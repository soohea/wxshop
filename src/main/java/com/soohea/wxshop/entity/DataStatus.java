package com.soohea.wxshop.entity;

public enum DataStatus {
    OK(),
    DELETED();

    public String getName(){
        return name().toLowerCase();
    }

}
