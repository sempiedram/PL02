package com.sempiedram.pl02app;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.sempiedram.pl02app.dummy.DummyContent;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements
            NavigationView.OnNavigationItemSelectedListener,
            UploadRecipeFragment.OnFragmentInteractionListener,
            RecipePreviewFragment.OnListFragmentInteractionListener,
            AccountInfoFragment.OnFragmentInteractionListener {

    ViewPager viewPager;

    String username; //TODO: Move to LiveData
    String sessionToken; //TODO: Move to LiveData

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionToken = getIntent().getStringExtra(this.getClass().getPackage().toString() + "." + APIRequestTask.SESSION_TOKEN);
        username = getIntent().getStringExtra(this.getClass().getPackage().toString() + ".username");

        System.out.println("Session token: " + sessionToken);
        System.out.println("Username: " + username);


        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        MainFragmentPagerAdapter adapter = new MainFragmentPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.main_view_pager);
        viewPager.setAdapter(adapter);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_all_recipes) {
            viewPager.setCurrentItem(0);
        } else if (id == R.id.menu_upload_recipe) {
            viewPager.setCurrentItem(1);
        } else if (id == R.id.menu_other) {
            viewPager.setCurrentItem(2);
        } else if (id == R.id.menu_account) {
            viewPager.setCurrentItem(3);
        } else if (id == R.id.menu_log_out) {
            logout(username, sessionToken);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout(String username, String sessionToken) {
        Map<String, String> parameters = new HashMap<>();

        parameters.put("username", username);
        parameters.put("session_token", sessionToken);

        String parametersString = URLUtils.composeQueryParameters(parameters);

        new APIRequestTask(null, null).execute("POST", "http://192.168.254.3:35000/users/logout", parametersString);
        finish();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        System.out.println("Interaction from fragment: " + uri.toString());
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        System.out.println("Touched item: " + item.content);
    }
}
