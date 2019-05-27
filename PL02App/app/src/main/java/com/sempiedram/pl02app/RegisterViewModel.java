package com.sempiedram.pl02app;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class RegisterViewModel extends ViewModel {
    public MutableLiveData<String> queryResult;


    public LiveData<String> getQueryResult() {
        if(queryResult == null) {
            queryResult = new MutableLiveData<>();
        }

        return queryResult;
    }
}
