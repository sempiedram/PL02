package com.sempiedram.pl02app;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class APIRequestTask extends AsyncTask<String, Void, String> {

    enum HTTPMethod {
        POST,
        GET
    };

    public static final String OUTCOME = "outcome";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String ERROR_MESSAGE = "error_msg";
    public static final String SESSION_TOKEN = "session_token";


    private ProgressBar progressIcon;
    private MutableLiveData<String> resultVariable;

    HTTPMethod httpMethod;
    String sessionToken;
    String apiURL;
    String apiBody;

    APIRequestTask(ProgressBar progressBar,
                   MutableLiveData<String> resultVariable,
                   HTTPMethod httpMethod,
                   String sessionToken,
                   String apiURL,
                   String apiBody) {
        progressIcon = progressBar;
        this.resultVariable = resultVariable;

        this.httpMethod = httpMethod;
        this.sessionToken = sessionToken;
        this.apiURL = apiURL;
        this.apiBody = apiBody;
    }

    @Override
    protected void onPreExecute() {
        if(progressIcon != null) {
            progressIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected String doInBackground(String[] apiRequest) {

        String result = "";

        try {
            URL loginEndpoint = new URL(apiURL);
            HttpURLConnection apiConnection = (HttpURLConnection) loginEndpoint.openConnection();
            apiConnection.setRequestMethod(httpMethod.name());
            if(sessionToken != null) {
                apiConnection.setRequestProperty("Authorization", sessionToken);
            }
            apiConnection.setConnectTimeout(5000);

            if(httpMethod == HTTPMethod.POST) {
                apiConnection.setDoOutput(true);
                apiConnection.getOutputStream().write(apiBody.getBytes());
            }

            InputStream inputStream = null;

            int responseCode = apiConnection.getResponseCode();
            System.out.println("Response code: " + responseCode);

            switch(responseCode) {
                case 400:
                case 401:
                case 402:
                case 404:
                    inputStream = apiConnection.getErrorStream();
                    break;
                default:
                    inputStream = apiConnection.getInputStream();
                    break;
            }

            InputStreamReader sr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(sr);

            StringBuilder resultBuilder = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
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
