package com.sempiedram.pl02app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DetailActivity extends AppCompatActivity {

    String username;
    String sessionToken;
    String recipe_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionToken = getIntent().getStringExtra("sessionToken");
        username = getIntent().getStringExtra("username");
        recipe_id = getIntent().getStringExtra("recipe_id");

        setContentView(R.layout.activity_detail);


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_detail_fragment_layout, RecipeDetailFragment.newInstance(sessionToken, username, recipe_id), "recipeDetail")
                .addToBackStack(null)
                .commit();
    }
}
