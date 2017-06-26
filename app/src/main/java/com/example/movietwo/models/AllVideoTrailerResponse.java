package com.example.movietwo.models;


import java.util.ArrayList;

public class AllVideoTrailerResponse {

    private int id;
    private ArrayList<VideoTrailer> results;

    public int getId() {
        return id;
    }

    public ArrayList<VideoTrailer> getResults() {
        return results;
    }
}
