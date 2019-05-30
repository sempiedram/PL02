package com.sempiedram.pl02app;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    Context mContext;

    public MainFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int i) {
        switch(i) {
            case 0:
                return new RecipesListFragment();
            case 1:
                return new UploadRecipeFragment();
            case 3:
                return new AccountInfoFragment();
        }

        return new UploadRecipeFragment(); // TODO: Change
    }

    @Override
    public int getCount() {
        return 4;
    }
}
