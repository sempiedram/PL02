package com.sempiedram.pl02app;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sempiedram.pl02app.RecipesListFragment.OnListFragmentInteractionListener;

import java.util.List;

public class RecipeRecyclerViewAdapter extends RecyclerView.Adapter<RecipeRecyclerViewAdapter.ViewHolder> {

    private final List<RecipePreview> recipesList;
    private final OnListFragmentInteractionListener recipeClickedListener;

    RecipeRecyclerViewAdapter(List<RecipePreview> items, OnListFragmentInteractionListener listener) {
        recipesList = items;
        recipeClickedListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_recipe_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if(recipesList == null || position >= recipesList.size()) {
            return;
        }

        RecipePreview recipePreview = recipesList.get(position);
        holder.recipePreview = recipePreview;

        holder.idTextView.setText(recipePreview.recipeID);
        holder.typeTextView.setText(recipePreview.recipeType);
        holder.imageView.setImageBitmap(recipePreview.preview);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != recipeClickedListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    recipeClickedListener.onListFragmentInteraction(holder.recipePreview);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView idTextView;
        public final TextView typeTextView;
        public final ImageView imageView;

        public RecipePreview recipePreview;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            idTextView = view.findViewById(R.id.recipe_id);
            typeTextView = view.findViewById(R.id.recipePreviewType);
            imageView = view.findViewById(R.id.recipePreviewImage);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + typeTextView.getText() + "'";
        }
    }
}
