package com.sempiedram.pl02app;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class APIRequestTask extends AsyncTask<String, Void, String> {

    private ProgressBar progressIcon;
    private MutableLiveData<String> resultVariable;


    public static final String OUTCOME = "outcome";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String SESSION_TOKEN = "session_token";

    APIRequestTask(ProgressBar progressBar, MutableLiveData<String> resultVariable) {
        progressIcon = progressBar;
        this.resultVariable = resultVariable;
    }

    @Override
    protected void onPreExecute() {
        if(progressIcon != null) {
            progressIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected String doInBackground(String[] apiRequest) {
        String apiRequestMethod = apiRequest[0];
        String apiURL = apiRequest[1];
        String apiBody = apiRequest[2];

        String result = "";

        try {
            URL loginEndpoint = new URL(apiURL);
            HttpURLConnection apiConnection = (HttpURLConnection) loginEndpoint.openConnection();
            apiConnection.setRequestMethod(apiRequestMethod);

            if(apiRequestMethod.equals("POST")) {
                apiConnection.setDoOutput(true);
                apiConnection.getOutputStream().write(apiBody.getBytes());
            }

            InputStreamReader sr = new InputStreamReader(apiConnection.getInputStream());
            BufferedReader br = new BufferedReader(sr);

            StringBuilder resultBuilder = new StringBuilder();

            String line;
            while((line = br.readLine()) != null) {
                resultBuilder.append(line);
            }

            result = resultBuilder.toString();
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if(progressIcon != null) {
            progressIcon.setVisibility(View.GONE);
        }

        if(resultVariable != null) {
            resultVariable.setValue(result);
        }
    }
}
