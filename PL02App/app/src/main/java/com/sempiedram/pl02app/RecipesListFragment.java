package com.sempiedram.pl02app;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RecipesListFragment extends Fragment {


    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipesListFragment() {
    }


//    @SuppressWarnings("unused")
//    public static RecipesListFragment newInstance(int columnCount) {
//        RecipesListFragment fragment = new RecipesListFragment();
////        Bundle args = new Bundle();
////        args.putInt(ARG_COLUMN_COUNT, columnCount);
////        fragment.setArguments(args);
//        return fragment;
//    }

    RecyclerView recyclerView;
    List<Recipe> allRecipes = new ArrayList<>();
    public MutableLiveData<String> queryResult;

    public void reloadRecipes() {
        Map<String, String> parameters = new HashMap<>();

        parameters.put("session_token", mListener.getSessionToken());

        String parametersString = URLUtils.composeQueryParameters(parameters);

        new APIRequestTask(null, queryResult).execute("GET", getResources().getString(R.string.api_url) + "/recipes/all", parametersString);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
//        }

        queryResult = new MutableLiveData<>();
        queryResult.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String allRecipesResponse) {
                System.out.println("Received response to query of all recipes: " + allRecipesResponse);

                try {
                    JSONObject json = new JSONObject(allRecipesResponse);

                    String outcome = json.getString(APIRequestTask.OUTCOME);
                    if(outcome.equals(APIRequestTask.SUCCESS)) {
                        JSONArray recipesIDs = json.getJSONArray("RecipesIDs");
                        allRecipes.clear();
                        for(int index = 0; index < recipesIDs.length(); index++) {
                            String recipeID = recipesIDs.getString(index);
                            allRecipes.add(new Recipe(recipeID, recipeID, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>()));
                        }
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }else if (outcome.equals(APIRequestTask.ERROR)) {
                        Snackbar.make(getView(), "Error login in: " + json.getString(APIRequestTask.ERROR_MESSAGE), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }else {
                        Snackbar.make(getView(), "Unknown outcome while logging in: " + queryResult, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        reloadRecipes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        Context context = view.getContext();

        // Set the adapter
        recyclerView = view.findViewById(R.id.recipes_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new RecipeRecyclerViewAdapter(allRecipes, mListener));

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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Recipe item);
        String getSessionToken();
    }
}
