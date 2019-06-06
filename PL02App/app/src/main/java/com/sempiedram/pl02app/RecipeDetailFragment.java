package com.sempiedram.pl02app;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sempiedram.pl02app.MainActivity.ARG_SESSION_TOKEN;
import static com.sempiedram.pl02app.MainActivity.ARG_USERNAME;

public class RecipeDetailFragment extends Fragment {
    private String recipeID;
    String sessionToken;
    String username;

    TextView recipeIDView;
    TextView recipeTypeView;
    TextView recipeIngredientsView;
    TextView recipeStepsView;
    ListView photosView;

    List<Bitmap> photos;

    public RecipeDetailFragment() {}

    public static RecipeDetailFragment newInstance(String sessionToken, String username, String recipeID) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_TOKEN, sessionToken);
        args.putString(ARG_USERNAME, username);
        args.putString("recipeID", recipeID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sessionToken = getArguments().getString(ARG_SESSION_TOKEN);
            username = getArguments().getString(ARG_USERNAME);
            recipeID = getArguments().getString("recipeID");
        }

        photos = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        recipeIDView = view.findViewById(R.id.recipeDetailID);
        recipeTypeView = view.findViewById(R.id.recipeDetailType);
        recipeIngredientsView = view.findViewById(R.id.recipeDetailIngredients);
        recipeStepsView = view.findViewById(R.id.recipeDetailSteps);
        photosView = view.findViewById(R.id.recipeDetailPictures);

        photosView.setAdapter(new PhotosListAdapter(photos));

        return view;
    }

    class PhotosListAdapter extends BaseAdapter {
        List<Bitmap> photos;

        PhotosListAdapter(List<Bitmap> photos) {
            this.photos = photos;
        }

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public Object getItem(int position) {
            return photos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.photo_item, parent, false);
            }

            TextView positionView = convertView.findViewById(R.id.item_position);
            positionView.setText("" + position);

            ImageView imageView = convertView.findViewById(R.id.photo_view);
            imageView.setImageBitmap(photos.get(position));

            return convertView;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadRecipeInfo();
    }

    private void loadRecipeInfo() {
        String apiURL = getView().getResources().getString(R.string.api_url);

        new LoadRecipeInfoAndPhotosTask(apiURL).execute();
    }

    public static String reformatStepString(String step) {
        if(step == null) {
            return ":)";
        }

        step = step.trim();

        if(step.equals("")) {
            return "";
        }

        if(step.startsWith("b'")) {
            return step.substring(2, step.length() - 1);
        }

        return step;
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().finish();
    }

    class LoadRecipeInfoAndPhotosTask extends AsyncTask<String, Void, JSONObject> {
        String apiURL;

        LoadRecipeInfoAndPhotosTask(String apiURL) {
            this.apiURL = apiURL;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected JSONObject doInBackground(String[] apiRequest) {
            try {
                System.out.println("RecipeDetail: loading recipe info.");
                JSONObject recipeInfoResponse = APIRequestTask.loadRecipeInfo(sessionToken, apiURL, recipeID);

                if(recipeInfoResponse == null) {
                    return null;
                }else if(recipeInfoResponse.getString(APIRequestTask.OUTCOME).equals(APIRequestTask.SUCCESS)) {
                    System.out.println("RecipeDetail: Recipe info loaded: " + recipeInfoResponse.toString() + ".");
                    JSONObject recipe = recipeInfoResponse.getJSONObject("recipe");
                    JSONArray recipePhotos = recipe.getJSONArray("photos");

                    for(int photoIndex = 0; photoIndex < recipePhotos.length(); photoIndex++) {
                        String photoID = recipePhotos.getString(photoIndex);
                        System.out.println("RecipeDetail: Loading picture: " + photoID + ".");
                        Bitmap recipePhoto = APIRequestTask.loadPhoto(sessionToken, apiURL, photoID);
                        System.out.println("RecipeDetail: Loaded picture: " + photoID + ".");
                        photos.add(recipePhoto);
                    }

                    return recipeInfoResponse;
                }else {
                    return null;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if(result == null) {
                Snackbar.make(getView(), "Could not load the recipe info.", 3000).show();
                return;
            }

            try {
                if (result.getString(APIRequestTask.OUTCOME).equals(APIRequestTask.SUCCESS)) {
                    System.out.println("RecipeDetail: Received recipe info: " + result.toString() + ".");
                    recipeIDView.setText(recipeID);
                    JSONObject recipe = result.getJSONObject("recipe");
                    recipeTypeView.setText(recipe.getString("type"));

                    JSONArray ingredientsArray = recipe.getJSONArray("ingredients");
                    StringBuilder ingredients = new StringBuilder();
                    for(int i = 0; i < ingredientsArray.length(); i++) {
                        ingredients.append(i);
                        ingredients.append(": ");
                        ingredients.append(ingredientsArray.getString(i)).append("\n");
                    }
                    recipeIngredientsView.setText(ingredients.toString());

                    JSONArray stepsArray = recipe.getJSONArray("steps");
                    StringBuilder steps = new StringBuilder();
                    for(int i = 0; i < stepsArray.length(); i++) {
                        JSONArray stepJSON = stepsArray.getJSONArray(i);
                        steps.append(stepJSON.getString(0) + ": " + reformatStepString(stepJSON.getString(1))).append("\n");
                    }
                    recipeStepsView.setText(steps.toString());

                }else if(result.getString(APIRequestTask.OUTCOME).equals(APIRequestTask.ERROR)) {
                    Snackbar.make(getView(), "Could not load the recipe information: " + result.getString("error_msg"), 3000).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
