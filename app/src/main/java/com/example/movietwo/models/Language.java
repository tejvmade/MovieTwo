package com.example.movietwo.models;



public class Language {

    LANGUAGE_EN("en"),
    LANGUAGE_Hi("hi");

    private String value;

    Language(String value) {
        this.value = value;
    }


    public String getValue() {
        return value;
    }
}