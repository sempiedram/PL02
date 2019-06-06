package com.sempiedram.pl02app;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    private String sessionToken;
    private String username;

    static final int FRAGMENT_ALL_RECIPES = 0;
    static final int FRAGMENT_UPLOAD_RECIPE = 1;

    MainFragmentPagerAdapter(String sessionToken, String username, FragmentManager fm) {
        super(fm);
        this.sessionToken = sessionToken;
        this.username = username;
    }

    @Override
    public Fragment getItem(int i) {
        switch(i) {
            case FRAGMENT_ALL_RECIPES:
                return RecipesListFragment.newInstance(sessionToken, username);
            case FRAGMENT_UPLOAD_RECIPE:
                return UploadRecipeFragment.newInstance(sessionToken, username);
        }

        System.err.println("Requested fragment at position " + i + " when there is only " + getCount() + " fragments.");
        return RecipesListFragment.newInstance(sessionToken, username);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
