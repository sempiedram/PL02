package com.sempiedram.pl02app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;

import static com.sempiedram.pl02app.MainFragmentPagerAdapter.FRAGMENT_ALL_RECIPES;
import static com.sempiedram.pl02app.MainFragmentPagerAdapter.FRAGMENT_UPLOAD_RECIPE;

public class MainActivity extends AppCompatActivity
        implements
            NavigationView.OnNavigationItemSelectedListener,
            RecipesListFragment.OnListFragmentInteractionListener {

    public static final String PACKAGE = LoginRegisterActivity.class.getPackage().toString();

    public static final String ARG_SESSION_TOKEN = PACKAGE + ".session_token";
    public static final String ARG_USERNAME = PACKAGE + ".username";


    //TODO: Make the option to change the server address.

    ViewPager viewPager;

    String username;
    String sessionToken;

    public void logout(String username, String sessionToken) {
        Map<String, String> parameters = new HashMap<>();

        parameters.put("username", username);
        parameters.put("session_token", sessionToken);

        String parametersString = URLUtils.composeQueryParameters(parameters);

        new APIRequestTask(null, null,
                APIRequestTask.HTTPMethod.POST,
                sessionToken,
                getResources().getString(R.string.api_url) + "/users/logout?" + parametersString,
                ""
        ).execute();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionToken = getIntent().getStringExtra(ARG_SESSION_TOKEN);
        username = getIntent().getStringExtra(ARG_USERNAME);

        System.out.println("MainActivity: Session token: " + sessionToken);
        System.out.println("MainActivity: Username: " + username);


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

        MainFragmentPagerAdapter adapter = new MainFragmentPagerAdapter(sessionToken, username, getSupportFragmentManager());
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_all_recipes) {
            viewPager.setCurrentItem(FRAGMENT_ALL_RECIPES);
        } else if (id == R.id.menu_upload_recipe) {
            viewPager.setCurrentItem(FRAGMENT_UPLOAD_RECIPE);
        } else if (id == R.id.menu_log_out) {
            logout(username, sessionToken);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListFragmentInteraction(RecipePreview recipePreview) {
        System.out.println("Touched recipe preview: " + recipePreview.toString());
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("recipe_id", recipePreview.recipeID);
        intent.putExtra("sessionToken", sessionToken);
        intent.putExtra("username", username);
        this.startActivity(intent);

//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.drawer_layout, RecipeDetailFragment.newInstance(), "recipeDetail")
//                .addToBackStack(null)
//                .commit();
    }
}
