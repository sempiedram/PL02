package com.sempiedram.pl02app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.sempiedram.pl02app.MainActivity.ARG_SESSION_TOKEN;
import static com.sempiedram.pl02app.MainActivity.ARG_USERNAME;

public class AccountInfoFragment extends Fragment {

    // TODO: Rename and change types of parameters
    private String sessionToken;
    private String username;

    public AccountInfoFragment() {}

    public static AccountInfoFragment newInstance(String sessionToken, String username) {
        AccountInfoFragment fragment = new AccountInfoFragment();

        Bundle args = new Bundle();
        args.putString(ARG_SESSION_TOKEN, sessionToken);
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sessionToken = getArguments().getString(ARG_SESSION_TOKEN);
            username = getArguments().getString(ARG_USERNAME);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_info, container, false);

        //TODO: Maybe add a button with which to refresh the information being presented to the user.

        return view;
    }
}
