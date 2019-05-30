
package com.sempiedram.pl02app;

import java.util.List;

class Recipe {
    public String name;
    public String type;
    public List<String> ingredients;
    public List<String> steps;

    public List<String> photos; // TODO: Maybe replace with the actual images.

    public Recipe(String name, String type, List<String> ingredients, List<String> steps, List<String> photos) {
        this.name = name;
        this.type = type;
        this.ingredients = ingredients;
        this.steps = steps;
        this.photos = photos;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: '");
        sb.append(name);
        sb.append("\'\n");

        sb.append("Type: '");
        sb.append(type);
        sb.append("\'\n");

        sb.append("Ingredients: '");
        for(String s : ingredients) {
            sb.append(s);
            sb.append(", ");
        }
        sb.append("\'\n");

        sb.append("Steps: '");
        for(String s : steps) {
            sb.append(s);
            sb.append(", ");
        }
        sb.append("\'\n");

        sb.append("Photos: '");
        for(String s : steps) {
            sb.append(s);
            sb.append(", ");
        }
        sb.append("\'\n");

        return sb.toString();
    }
}
