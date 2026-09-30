package com.aspire.ecoplate;

public class Recipe {

    public final String title;
    public final int minutes;
    public final String uses;
    public final String steps;

    public Recipe(String title, int minutes, String uses, String steps) {
        this.title = title;
        this.minutes = minutes;
        this.uses = uses;
        this.steps = steps;
    }
}
