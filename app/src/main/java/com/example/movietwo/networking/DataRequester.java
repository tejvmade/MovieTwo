package com.example.movietwo.networking;


public interface DataRequester {

    void onFailure(Throwable error);


    void onSuccess(Object respObj);

}
