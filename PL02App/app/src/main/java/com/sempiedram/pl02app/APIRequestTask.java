package com.sempiedram.pl02app;

import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

    public static JSONObject loadRecipeInfo(String sessionToken, String apiURL, String recipeID) {
        String result = null;
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("recipe_id", recipeID);

            URL recipeInfoURL = new URL(apiURL + "/recipes/get?"
                    + URLUtils.composeQueryParameters(parameters));

            HttpURLConnection apiConnection = (HttpURLConnection) recipeInfoURL.openConnection();

            apiConnection.setRequestMethod(APIRequestTask.HTTPMethod.GET.name());
            apiConnection.setRequestProperty("Authorization", sessionToken);

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

        if(result == null) {
            return null;
        }

        try {
            return new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap loadPhoto(String sessionToken, String apiURL, String photoID) {
        System.out.println("Getting photo with id '" + photoID + "'.");
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("photo_id", photoID);

            URL recipeInfoURL = new URL(apiURL + "/photos/get?"
                    + URLUtils.composeQueryParameters(parameters));

            HttpURLConnection apiConnection = (HttpURLConnection) recipeInfoURL.openConnection();
            apiConnection.setRequestMethod(APIRequestTask.HTTPMethod.GET.name());
            apiConnection.setRequestProperty("Authorization", sessionToken);

            if(apiConnection.getResponseCode() == 404) {
                System.out.println("Could not load photo with id '" + photoID + "'.");
                return null;
            }

            Bitmap bmp = BitmapFactory.decodeStream(apiConnection.getInputStream());

            return bmp;
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject loadAllRecipesIDs(String sessionToken, String apiURL, String filter) {
        String result = null;
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("filter", filter);

            URL recipeInfoURL = new URL(apiURL + "/recipes/all?" + URLUtils.composeQueryParameters(parameters));

            HttpURLConnection apiConnection = (HttpURLConnection) recipeInfoURL.openConnection();
            apiConnection.setRequestMethod(APIRequestTask.HTTPMethod.GET.name());
            apiConnection.setRequestProperty("Authorization", sessionToken);

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

        if(result == null) {
            return null;
        }

        try {
            JSONObject jsonResult = new JSONObject(result);
            return jsonResult;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
