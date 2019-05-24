package com.sempiedram.pl02app.ui.main;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sempiedram.pl02app.LoginFragment;
import com.sempiedram.pl02app.R;
import com.sempiedram.pl02app.RegisterFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
//        return PlaceholderFragment.newInstance(position + 1);

        if(position == 0) return new LoginFragment();

        return new RegisterFragment();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) return mContext.getResources().getString(R.string.login_tab_text);

        return mContext.getResources().getString(R.string.register_tab_text);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}