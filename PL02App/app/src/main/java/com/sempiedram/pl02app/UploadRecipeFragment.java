
package com.sempiedram.pl02app;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.sempiedram.pl02app.MainActivity.ARG_SESSION_TOKEN;
import static com.sempiedram.pl02app.MainActivity.ARG_USERNAME;


public class UploadRecipeFragment extends Fragment {

    private String sessionToken;
    private String username;

    EditText recipeIDTextEdit;
    EditText recipeTypeTextEdit;
    private List<String> recipeIngredientsList;
    private List<String> recipeStepsList;
    private List<Bitmap> recipePhotosList;
    RecyclerView photosListView;


    private int selectedIngredient = 0;
    private int selectedStep = 0;
    private int selectedPhoto = 0;

    private static final int ACTIVITY_IMPORT_PHOTO = 1;

    private ProgressBar progressIcon;
    private MutableLiveData<String> resultVariable;

    public UploadRecipeFragment() {}

    public static UploadRecipeFragment newInstance(String sessionToken, String username) {
        Bundle args = new Bundle();

        args.putString(ARG_SESSION_TOKEN, sessionToken);
        args.putString(ARG_USERNAME, username);

        UploadRecipeFragment fragment = new UploadRecipeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionToken = getArguments().getString(ARG_SESSION_TOKEN);
        username = getArguments().getString(ARG_USERNAME);

        recipeIngredientsList = new ArrayList<>();
        recipeStepsList = new ArrayList<>();
        recipePhotosList = new ArrayList<>();

        resultVariable = new MutableLiveData<>();
    }

    private void importPhoto() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, ACTIVITY_IMPORT_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ACTIVITY_IMPORT_PHOTO && resultCode == RESULT_OK) {
            Uri imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), imagePath);

                recipePhotosList.add(bitmap);
                RecyclerView.Adapter adapter = photosListView.getAdapter();
                if(adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isValidText(String ingredient) {
        if(ingredient == null) {
            return false;
        }

        ingredient = ingredient.trim();

        return !ingredient.isEmpty();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_upload_recipe, container, false);

        progressIcon = view.findViewById(R.id.upload_recipe_progress_bar);

        final Button uploadButton = view.findViewById(R.id.upload_recipe);


        // Ingredient related views:
        final EditText ingredientText = view.findViewById(R.id.recipe_ingredient);

        final RecyclerView ingredientsListView = view.findViewById(R.id.recipe_ingredients_list);
        ingredientsListView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        ingredientsListView.setAdapter(new TextRecyclerViewAdapter(recipeIngredientsList, new TextRecyclerViewAdapter.ItemSelectedListener() {
            @Override
            public void itemSelected(String itemText, int index) {
                selectedIngredient = index;
            }
        }));

        final Button removeIngredient = view.findViewById(R.id.recipe_remove_ingredient);
        removeIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recipeIngredientsList.isEmpty() || selectedIngredient < recipeIngredientsList.size()) {
                    recipeIngredientsList.remove(selectedIngredient);
                    ingredientsListView.getAdapter().notifyDataSetChanged();
                }else {
                    removeIngredient.setError("invalid list position");
                }
            }
        });
        Button addIngredient = view.findViewById(R.id.recipe_add_ingredient);
        addIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ingredient = ingredientText.getText().toString();

                if(isValidText(ingredient)) {
                    recipeIngredientsList.add(ingredient);
                    ingredientText.setText("");
                    ingredientsListView.getAdapter().notifyDataSetChanged();
                }else {
                    ingredientText.setError("invalid ingredient");
                }
            }
        });


        // Step related views:
        final EditText stepText = view.findViewById(R.id.recipe_step);

        final RecyclerView stepsListView = view.findViewById(R.id.recipe_steps_list);
        stepsListView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        stepsListView.setAdapter(new TextRecyclerViewAdapter(recipeStepsList, new TextRecyclerViewAdapter.ItemSelectedListener() {
            @Override
            public void itemSelected(String itemText, int index) {
                selectedStep = index;
            }
        }));

        final Button removeStep = view.findViewById(R.id.recipe_remove_step);
        removeStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recipeStepsList.isEmpty() || selectedStep < recipeStepsList.size()) {
                    recipeStepsList.remove(selectedStep);
                    stepsListView.getAdapter().notifyDataSetChanged();
                }else {
                    removeStep.setError("invalid list position");
                }
            }
        });
        Button addStep = view.findViewById(R.id.recipe_add_step);
        addStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String step = stepText.getText().toString();

                if(isValidText(step)) {
                    recipeStepsList.add(step);
                    stepText.setText("");
                    stepsListView.getAdapter().notifyDataSetChanged();
                }else {
                    stepText.setError("invalid step");
                }
            }
        });


        // Photos related views
        photosListView = view.findViewById(R.id.recipe_photos_list);
        photosListView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        photosListView.setAdapter(new PhotosRecyclerViewAdapter(recipePhotosList, new PhotosRecyclerViewAdapter.ItemSelectedListener() {
            @Override
            public void itemSelected(Bitmap photo, int index) {
                selectedPhoto = index;
            }
        }));

        final Button removePhoto = view.findViewById(R.id.recipe_remove_photo);
        removePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recipePhotosList.isEmpty() || selectedPhoto < recipePhotosList.size()) {
                    recipePhotosList.remove(selectedPhoto);
                    photosListView.getAdapter().notifyDataSetChanged();
                }else {
                    removePhoto.setError("invalid list position");
                }
            }
        });
        Button addPhoto = view.findViewById(R.id.recipe_add_photo);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importPhoto();
            }
        });

        recipeIDTextEdit = view.findViewById(R.id.upload_recipe_id);
        recipeTypeTextEdit = view.findViewById(R.id.upload_recipe_type);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadRecipe();
                uploadButton.setEnabled(false);

                new CountDownTimer(1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {}

                    @Override
                    public void onFinish() {
                        uploadButton.setEnabled(true);
                    }
                }.start();
            }
        });

        resultVariable.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String recipeUploadResponse) {
                try {
                    JSONObject response = new JSONObject(recipeUploadResponse);

                    String outcome = response.getString(APIRequestTask.OUTCOME);
                    if(outcome.equals(APIRequestTask.SUCCESS)) {
                        Snackbar.make(view, "Successfully uploaded the recipe: " + response,
                                Snackbar.LENGTH_LONG).show();
                        //TODO: Clear all fields.
                    }else {
                        String reason = "";

                        if(outcome.equals(APIRequestTask.ERROR)) {
                            reason = view.getResources().getString(R.string.error_reason)
                                    + response.get(APIRequestTask.ERROR_MESSAGE);
                        }else {
                            reason = "Unknown reason: \"\"\"" + recipeUploadResponse + "\"\"\".";
                        }

                        Snackbar.make(view, "Could not upload the recipe: " + reason, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    class UploadRecipeTask extends AsyncTask<String, Void, String> {

        String recipeID;
        String recipeType;

        UploadRecipeTask(String recipeID, String recipeType) {
            this.recipeID = recipeID;
            this.recipeType = recipeType;
        }

        @Override
        protected void onPreExecute() {
            if(progressIcon != null) {
                progressIcon.setVisibility(View.VISIBLE);
            }
        }

        String uploadPhoto(Bitmap photo) {
            String result = "";
            try {
                URL newPhoto = new URL(getView().getResources().getString(R.string.api_url) + "/photos/new");
                HttpURLConnection apiConnection = (HttpURLConnection) newPhoto.openConnection();
                apiConnection.setRequestMethod(APIRequestTask.HTTPMethod.POST.name());
                apiConnection.setRequestProperty("Authorization", sessionToken);

                apiConnection.setDoOutput(true);
                photo.compress(Bitmap.CompressFormat.PNG, 100, apiConnection.getOutputStream());

                InputStreamReader sr = new InputStreamReader(apiConnection.getInputStream());
                BufferedReader br = new BufferedReader(sr);

                StringBuilder resultBuilder = new StringBuilder();

                String line;
                while((line = br.readLine()) != null) {
                    resultBuilder.append(line);
                }

                result = resultBuilder.toString();
            }catch (MalformedURLException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }

            if(result.isEmpty()) {
                return null;
            }

            try {
                JSONObject response = new JSONObject(result);
                String outcome = response.getString("outcome");

                if(outcome.equals(APIRequestTask.SUCCESS)) {
                    return response.getString("photo_id");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected String doInBackground(String[] apiRequest) {
//            Map<Bitmap, String> recipePhotosIds = new HashMap<>();
            List<String> photos = new ArrayList<>();

            for(int photoIndex = 0; photoIndex < recipePhotosList.size(); photoIndex++) {
                Bitmap photo = recipePhotosList.get(photoIndex);
                String photoID = uploadPhoto(photo);
                if(photoID != null) {
                    photos.add(photoID);
                }else {
                    // TODO: Notify that this photo was not successfully uploaded.
                    System.err.println("Could not upload photo at index " + photoIndex);
                }
            }

            JSONObject recipeInfo = new JSONObject();
            try {
                recipeInfo.put("id", recipeID);
                recipeInfo.put("type", recipeType);

                List<String> ingredients = new ArrayList<>(recipeIngredientsList);
                JSONArray jsonIngredients = new JSONArray(ingredients);

                JSONArray jsonSteps = new JSONArray();

                for(int stepIndex = 0; stepIndex < recipeStepsList.size(); stepIndex++) {
                    JSONArray step = new JSONArray();
                    step.put("" + stepIndex);
                    step.put(recipeStepsList.get(stepIndex));

                    jsonSteps.put(step);
                }

                JSONArray jsonPhotos = new JSONArray(photos);

                recipeInfo.put("ingredients", jsonIngredients);
                recipeInfo.put("steps", jsonSteps);
                recipeInfo.put("photos", jsonPhotos);
            } catch (JSONException e) {
                e.printStackTrace();
                return "{\"outcome\":\"error\", \"error\":\"local_json_encoding_error\"," +
                        "\"error_msg\":\"Local error generating the JSON recipe request.\"}";
            }

            String recipeInfoString = recipeInfo.toString();

            String uploadRecipeResult = "";

            try {
                URL uploadRecipeURL = new URL(getView().getResources().getString(R.string.api_url) + "/recipes/new");
                HttpURLConnection apiConnection = (HttpURLConnection) uploadRecipeURL.openConnection();
                apiConnection.setRequestMethod(APIRequestTask.HTTPMethod.POST.name());
                apiConnection.setRequestProperty("Authorization", sessionToken);

                apiConnection.setDoOutput(true);
                apiConnection.getOutputStream().write(recipeInfoString.getBytes());

                InputStreamReader sr = new InputStreamReader(apiConnection.getInputStream());
                BufferedReader br = new BufferedReader(sr);

                StringBuilder resultBuilder = new StringBuilder();

                String line;
                while((line = br.readLine()) != null) {
                    resultBuilder.append(line);
                }

                uploadRecipeResult = resultBuilder.toString();
            }catch (MalformedURLException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }

            return uploadRecipeResult;
        }

        @Override
        protected void onPostExecute(String result) {
            if(progressIcon != null) {
                progressIcon.setVisibility(View.GONE);
            }

            if(resultVariable != null) {
                resultVariable.setValue(result);
            }
        }
    }

    private void uploadRecipe() {
        new UploadRecipeTask(recipeIDTextEdit.getText().toString(),
                             recipeTypeTextEdit.getText().toString()).execute();
    }
}
