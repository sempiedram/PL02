package com.sempiedram.pl02app;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class RegisterFragment extends Fragment {

    private RegisterViewModel mViewModel;
    private ProgressBar progressBar;

    EditText registerUsername;
    EditText registerEmail;
    EditText registerName;
    EditText registerPassword;
    EditText registerConfirmPassword;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    private void register(String username, String email, String name, String password, String passwordConfirm) {
        try {
            JSONObject registrationInfo = new JSONObject();

            if(!isValidUsername(username)) {
                registerUsername.setError("invalid username");
                return;
            }

            if (!password.equals(passwordConfirm)) {
                registerPassword.setError("passwords don't match");
                registerConfirmPassword.setError("passwords don't match");
                return;
            }


            if (!isValidEmail(email)) {
                registerEmail.setError("invalid email");
                return;
            }

            registrationInfo.put("username", username);
            registrationInfo.put("email", email);
            registrationInfo.put("name", name);
            registrationInfo.put("password", password); //TODO: Send a hashed version of the password instead.

            new APIRequestTask(progressBar, mViewModel.queryResult,
                    APIRequestTask.HTTPMethod.POST,
                    null,
                    getResources().getString(R.string.api_url) + "/users/register",
                    registrationInfo.toString()
            ).execute();
        }catch (JSONException e) {
            e.printStackTrace();
            //TODO: Return information on what happened.
        }
    }

    public static boolean isValidUsername(String username) {
        Pattern p = Pattern.compile("[A-Za-z0-9\\._]+");
        return p.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        if(email == null || email.isEmpty()) {
            return false;
        }

        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_fragment, container, false);

        Button registerButton = view.findViewById(R.id.registerButton);

        registerUsername = view.findViewById(R.id.registerUsername);
        registerEmail = view.findViewById(R.id.registerEmail);
        registerName = view.findViewById(R.id.registerName);
        registerPassword = view.findViewById(R.id.registerPassword);
        registerConfirmPassword = view.findViewById(R.id.registerConfirmPassword);

        progressBar = getActivity().findViewById(R.id.loginProgressBar);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register(registerUsername.getText().toString().trim(),
                        registerEmail.getText().toString().trim(),
                        registerName.getText().toString().trim(),
                        LoginFragment.hashPassword(registerPassword.getText().toString().trim()),
                        LoginFragment.hashPassword(registerConfirmPassword.getText().toString().trim())
                );
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(RegisterViewModel.class);

        mViewModel.getQueryResult().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String response) {
                System.out.println("Register result: " + response);

                try {
                    JSONObject json = new JSONObject(response);

                    String outcome = json.getString(APIRequestTask.OUTCOME);

                    if(outcome.equals(APIRequestTask.SUCCESS)) {
                        Snackbar.make(getView(), "Successfully registered!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }else if (outcome.equals(APIRequestTask.ERROR)) {
                        Snackbar.make(getView(), "Error while registering " + json.getString(APIRequestTask.ERROR_MESSAGE), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }else {
                        Snackbar.make(getView(), "Unknown outcome while registering: " + response, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (JSONException e) {
                    Snackbar.make(getView(), "Unknown response while registering: " + response, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    e.printStackTrace();
                }
            }
        });
    }

}
