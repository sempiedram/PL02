package com.sempiedram.pl02app;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;
    private ProgressBar progressBar;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    String username; //TODO: Move to LiveData

    private void login(String username, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", hashPassword(password));

            new APIRequestTask(progressBar, mViewModel.queryResult,
                    APIRequestTask.HTTPMethod.POST,
                    null,
                    getView().getResources().getString(R.string.api_url) + "/users/login",
                    body.toString()
            ).execute();
        }catch (JSONException e) {
            // TODO: Inform the user that login failed.
            e.printStackTrace();
        }
    }

    public static String hashPassword(String password) {
        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }

        return new String(digest.digest(password.getBytes()));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.login_fragment, container, false);

        Button loginButton = view.findViewById(R.id.loginButton);
        final EditText loginUsername = view.findViewById(R.id.loginUsername);
        final EditText loginPassword = view.findViewById(R.id.loginPassword);

        progressBar = getActivity().findViewById(R.id.loginProgressBar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Username: " + loginUsername.getText().toString() + " - Password: " + loginPassword.getText().toString(), Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                login(loginUsername.getText().toString(), loginPassword.getText().toString());
                username = loginUsername.getText().toString();
            }
        });

        mViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        mViewModel.getQueryResult().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String queryResult) {
                System.out.println("Login result: '" + queryResult + "'.");
                if(queryResult == null || queryResult.isEmpty()) {
                    Snackbar.make(view, "Could not connect to log in.", Snackbar.LENGTH_LONG).show();
                    return;
                }
                try {
                    JSONObject json = new JSONObject(queryResult);

                    String outcome = json.getString(APIRequestTask.OUTCOME);
                    if(outcome.equals(APIRequestTask.SUCCESS)) {
                        String sessionToken = json.getString(APIRequestTask.SESSION_TOKEN);

                        Intent mainActivityIntent = new Intent(getActivity(), MainActivity.class);

                        mainActivityIntent.putExtra(MainActivity.ARG_SESSION_TOKEN, sessionToken);
                        mainActivityIntent.putExtra(MainActivity.ARG_USERNAME, username);

                        startActivity(mainActivityIntent);
                        getActivity().finish();
                        Snackbar.make(view, "Successfully logged in: " + json.getString(APIRequestTask.SESSION_TOKEN), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    }else if (outcome.equals(APIRequestTask.ERROR)) {
                        Snackbar.make(view, "Error login in: " + json.getString(APIRequestTask.ERROR_MESSAGE), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }else {
                        Snackbar.make(view, "Unknown outcome while logging in: " + queryResult, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}
