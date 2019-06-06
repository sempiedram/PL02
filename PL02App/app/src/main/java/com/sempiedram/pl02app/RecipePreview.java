package com.sempiedram.pl02app;

import android.graphics.Bitmap;

public class RecipePreview {
    public Bitmap preview;
    public String recipeID;
    public String recipeType;

    RecipePreview(String recipeID, String recipeType, Bitmap preview) {
        this.preview = preview;
        this.recipeID = recipeID;
        this.recipeType = recipeType;
    }
}
