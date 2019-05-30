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

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;
    private ProgressBar progressBar;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    String username; //TODO: Move to LiveData

    private void login(String username, String password) {
        Map<String, String> parameters = new HashMap<>();

        parameters.put("username", username);
        parameters.put("password", password);

        String parametersString = URLUtils.composeQueryParameters(parameters);

        new APIRequestTask(progressBar, mViewModel.queryResult).execute("POST",
                getView().getResources().getString(R.string.api_url) + "/users/login", parametersString);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.login_fragment, container, false);

        Button loginButton = view.findViewById(R.id.loginButton);
        final EditText loginUsername = view.findViewById(R.id.loginUsername);
        final EditText loginPassword = view.findViewById(R.id.loginPassword);
        progressBar = getActivity().findViewById(R.id.progressBar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Username: " + loginUsername.getText().toString() + " - Password: " + loginPassword.getText().toString(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                login(loginUsername.getText().toString(), loginPassword.getText().toString());
                username = loginUsername.getText().toString();
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        mViewModel.getQueryResult().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String queryResult) {
                System.out.println("Login result: " + queryResult);
                try {
                    JSONObject json = new JSONObject(queryResult);

                    String outcome = json.getString(APIRequestTask.OUTCOME);
                    if(outcome.equals(APIRequestTask.SUCCESS)) {
                        String sessionToken = json.getString(APIRequestTask.SESSION_TOKEN);

                        Intent mainActivityIntent = new Intent(getActivity(), MainActivity.class);

                        mainActivityIntent.putExtra(this.getClass().getPackage().toString() + "." + APIRequestTask.SESSION_TOKEN, sessionToken);
                        mainActivityIntent.putExtra(this.getClass().getPackage().toString() + ".username", username);

                        startActivity(mainActivityIntent);
                        getActivity().finish();

                    }else if (outcome.equals(APIRequestTask.ERROR)) {
                        Snackbar.make(getView(), "Error login in: " + json.getString(APIRequestTask.ERROR_MESSAGE), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }else {
                        Snackbar.make(getView(), "Unknown outcome while logging in: " + queryResult, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
