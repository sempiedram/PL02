package com.sempiedram.pl02app;

import android.arch.lifecycle.ViewModelProviders;
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

import java.util.HashMap;
import java.util.Map;

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
        Map<String, String> parameters = new HashMap<>();

        if(!password.equals(passwordConfirm)) {
            registerPassword.setError("passwords don't match");
            registerConfirmPassword.setError("passwords don't match");
        }

        parameters.put("username", username);
        parameters.put("email", email);
        parameters.put("name", name);
        parameters.put("password", password);
        parameters.put("confirm_password", passwordConfirm);

        String parametersString = URLUtils.composeQueryParameters(parameters);

        new APIRequestTask(progressBar, mViewModel.queryResult).execute("POST", getResources().getString(R.string.api_url) + "/users/register", parametersString);
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

        progressBar = getActivity().findViewById(R.id.progressBar);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register(registerUsername.getText().toString(),
                        registerEmail.getText().toString(),
                        registerName.getText().toString(),
                        registerPassword.getText().toString(),
                        registerConfirmPassword.getText().toString());
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(RegisterViewModel.class);
        // TODO: Use the ViewModel
    }

}
