package com.sempiedram.pl02app;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sempiedram.pl02app.MainActivity.ARG_SESSION_TOKEN;
import static com.sempiedram.pl02app.MainActivity.ARG_USERNAME;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RecipesListFragment extends Fragment {

    String sessionToken;
    String username;

    EditText filterEditText;
    RecyclerView recipesListRecyclerView;
    List<RecipePreview> allRecipesPreviews = new ArrayList<>();
    public MutableLiveData<String> recipesIDsListResponse;

    private OnListFragmentInteractionListener recipeClickedListener;

    public RecipesListFragment() {}

    public static RecipesListFragment newInstance(String sessionToken, String username) {
        RecipesListFragment fragment = new RecipesListFragment();

        Bundle args = new Bundle();
        args.putString(ARG_SESSION_TOKEN, sessionToken);
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionToken = getArguments().getString(ARG_SESSION_TOKEN);
        username = getArguments().getString(ARG_USERNAME);

        recipesIDsListResponse = new MutableLiveData<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        filterEditText = view.findViewById(R.id.filter_edit_text);
        recipesListRecyclerView = view.findViewById(R.id.recipes_list);
        recipesListRecyclerView.setLayoutManager(new NpaLinearLayoutManager(getActivity()));
        recipesListRecyclerView.setAdapter(new RecipeRecyclerViewAdapter(allRecipesPreviews, recipeClickedListener));
        recipesListRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        recipesIDsListResponse.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String allRecipesResponse) {
                System.out.println("Received response to query of all recipes: " + allRecipesResponse);

                try {
                    JSONObject json = new JSONObject(allRecipesResponse);

                    String outcome = json.getString(APIRequestTask.OUTCOME);
                    if(outcome.equals(APIRequestTask.SUCCESS)) {
                        JSONArray recipesIDs = json.getJSONArray("recipes_ids");
                        allRecipesPreviews.clear();
                        for(int index = 0; index < recipesIDs.length(); index++) {
                            String recipeID = recipesIDs.getString(index);
                            RecipePreview preview = new RecipePreview(recipeID,null,null);

                            allRecipesPreviews.add(preview);
                        }
                        recipesListRecyclerView.getAdapter().notifyDataSetChanged();
                    }else if (outcome.equals(APIRequestTask.ERROR)) {
                        Snackbar.make(view, "Could not load recipes: " + json.getString(APIRequestTask.ERROR_MESSAGE), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }else {
                        Snackbar.make(view, "Unknown outcome loading recipes: " + outcome, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button reloadRecipesListButton = view.findViewById(R.id.recipesRefreshListButton);
        reloadRecipesListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadRecipes();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reloadRecipes();
    }

    public void reloadRecipes() {
        // Load all recipes' IDs:
        Snackbar.make(getView(), "Realoading recipes...", 1000).show();

        new LoadRecipePreviewsTask(getView().getResources().getString(R.string.api_url), filterEditText.getText().toString()).execute();
    }

    class LoadRecipePreviewsTask extends AsyncTask<String, Void, JSONObject> {
        String apiURL;
        String filter;

        LoadRecipePreviewsTask(String apiURL, String filter) {
            this.apiURL = apiURL;
            this.filter = filter;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected JSONObject doInBackground(String[] apiRequest) {
            JSONObject allRecipesResponse = APIRequestTask.loadAllRecipesIDs(sessionToken, apiURL, filter);

            if(allRecipesResponse == null) {
                JSONObject result = new JSONObject();
                try {
                    result.put(APIRequestTask.OUTCOME, APIRequestTask.ERROR);
                    result.put(APIRequestTask.ERROR, "error_loading_recipes_ids");
                    result.put(APIRequestTask.ERROR_MESSAGE, "Could not load all recipes' ids.");
                    return result;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            try {
                String recipesIDsOutcome = allRecipesResponse.getString(APIRequestTask.OUTCOME);

                if(recipesIDsOutcome.equals(APIRequestTask.SUCCESS)) {
                    JSONArray recipesIDs = allRecipesResponse.getJSONArray("recipes_ids");
                    System.out.println("Received response for all recipes ids: " + recipesIDs.toString());

                    Set<String> recipesNotLoaded = new HashSet<>();

                    allRecipesPreviews.clear();
                    for(int index = 0; index < recipesIDs.length(); index++) {
                        String recipeID = recipesIDs.getString(index);

                        JSONObject recipeInfoResponse = APIRequestTask.loadRecipeInfo(sessionToken, apiURL, recipeID);

                        if(recipeInfoResponse == null) {
                            recipesNotLoaded.add(recipeID);
                        }else if(recipeInfoResponse.getString(APIRequestTask.OUTCOME).equals(APIRequestTask.SUCCESS)) {
                            System.out.println("Recipe[" + recipeID + "]: " + recipeInfoResponse.toString());
                            JSONObject recipe = recipeInfoResponse.getJSONObject("recipe");
                            JSONArray recipePhotos = recipe.getJSONArray("photos");

                            Bitmap recipePhoto = null;

                            if(recipePhotos.length() > 0) {
                                String photoID = recipePhotos.getString(0);
                                recipePhoto = APIRequestTask.loadPhoto(sessionToken, apiURL, photoID);
                            }

                            RecipePreview preview = new RecipePreview(recipeID, recipe.getString("type"), recipePhoto);
                            allRecipesPreviews.add(preview);
                        }else {
                            recipesNotLoaded.add(recipeID);
                        }
                    }

                    JSONArray recipesNotLoadedJSON = new JSONArray(Arrays.asList(recipesNotLoaded.toArray()));
                    JSONObject result = new JSONObject();
                    result.put("recipes_not_loaded", recipesNotLoadedJSON);
                    result.put(APIRequestTask.OUTCOME, APIRequestTask.SUCCESS);
                    return result;
                }

                return allRecipesResponse;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if(result == null) {
                Snackbar.make(getView(), "Could not load the recipes.", 1000);
            }else {
                recipesListRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            recipeClickedListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        recipeClickedListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(RecipePreview item);
    }

}
