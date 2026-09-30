package com.aspire.ecoplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public final class RecipeFallback {

    private RecipeFallback() {}

    private static final List<String> SAUCE = Arrays.asList(
            "sauce", "vinegar", "paste", "gochujang", "ketchup", "mayo", "mayonnaise", "mustard",
            "chutney", "pickle", "honey", "jam", "oil", "syrup", "dressing", "miso", "sriracha",
            "masala", "salt", "sugar", "spice");

    private static final List<String> BASE = Arrays.asList(
            "pasta", "noodle", "rice", "bread", "roti", "chapati", "paratha", "oat", "oats",
            "macaroni", "spaghetti", "tortilla", "bun", "wrap", "poha", "flour", "couscous",
            "quinoa", "ramen", "maggi", "idli", "dosa");

    private static final List<String> PROTEIN = Arrays.asList(
            "egg", "chicken", "paneer", "tofu", "fish", "prawn", "shrimp", "meat", "mutton",
            "beef", "pork", "dal", "lentil", "bean", "chickpea", "chana", "rajma", "soya",
            "sausage", "ham", "salmon", "tuna");

    private static final List<String> DAIRY = Arrays.asList(
            "milk", "curd", "yogurt", "yoghurt", "cheese", "butter", "cream", "ghee",
            "buttermilk", "dahi", "lassi");

    private static final List<String> FRUIT = Arrays.asList(
            "banana", "apple", "mango", "berry", "strawberry", "blueberry", "orange", "grape",
            "papaya", "pear", "peach", "melon", "watermelon", "pineapple", "lemon", "fruit");

    public static List<Recipe> suggest(List<String> ingredients) {
        List<String> sauces = new ArrayList<>();
        List<String> bases = new ArrayList<>();
        List<String> proteins = new ArrayList<>();
        List<String> dairy = new ArrayList<>();
        List<String> fruits = new ArrayList<>();
        List<String> veggies = new ArrayList<>();

        for (String name : ingredients) {
            if (matches(name, SAUCE)) sauces.add(name);
            else if (matches(name, BASE)) bases.add(name);
            else if (matches(name, PROTEIN)) proteins.add(name);
            else if (matches(name, DAIRY)) dairy.add(name);
            else if (matches(name, FRUIT)) fruits.add(name);
            else veggies.add(name);
        }

        List<String> extras = merge(proteins, veggies);
        List<Recipe> out = new ArrayList<>();

        // Base plus sauce: saucy noodles, pasta or rice.
        if (!bases.isEmpty() && !sauces.isEmpty()) {
            int n = 1;
            StringBuilder s = new StringBuilder();
            s.append(n++).append(". Cook the ").append(list(bases))
                    .append(" as the pack says, then drain and keep a cup of the cooking water.\n");
            s.append(n++).append(". Mix the ").append(list(sauces))
                    .append(" with a spoon of oil and a splash of the cooking water. Taste and add salt or a pinch of sugar.\n");
            if (!extras.isEmpty()) {
                s.append(n++).append(". Stir-fry the ").append(list(extras))
                        .append(" in a hot pan until just cooked.\n");
            }
            s.append(n).append(". Toss everything together over low heat until glossy and serve hot.");
            out.add(new Recipe(
                    cap(first(sauces)) + " " + first(bases).toLowerCase(Locale.getDefault()),
                    20,
                    list(merge(merge(bases, sauces), extras)),
                    s.toString()));
        }

        // Base plus vegetables or protein, no sauce.
        if (!bases.isEmpty() && !extras.isEmpty() && sauces.isEmpty()) {
            String steps = "1. Cook the " + list(bases) + " as the pack says, then drain.\n"
                    + "2. Stir-fry the " + list(extras) + " in oil with garlic, salt and pepper until tender.\n"
                    + "3. Add the " + list(bases) + " to the pan and toss for 2 to 3 minutes.\n"
                    + "4. Taste, adjust the seasoning and serve hot.";
            out.add(new Recipe(cap(first(bases)) + " stir-fry", 20,
                    list(merge(bases, extras)), steps));
        }

        // Protein plus vegetables skillet.
        if (!proteins.isEmpty() && !veggies.isEmpty()) {
            String steps = "1. Cook the " + list(proteins) + " in a little oil until done, then set aside.\n"
                    + "2. In the same pan, cook the " + list(veggies) + " with garlic and salt until tender.\n"
                    + "3. Return the " + list(proteins) + " to the pan and season with pepper or your favourite spices.\n"
                    + "4. Serve with " + (bases.isEmpty() ? "rice, roti or bread" : list(bases)) + ".";
            out.add(new Recipe(cap(first(proteins)) + " and " + first(veggies).toLowerCase(Locale.getDefault()) + " skillet",
                    25, list(merge(proteins, veggies)), steps));
        }

        // Fruit plus dairy smoothie.
        if (!fruits.isEmpty() && !dairy.isEmpty()) {
            String steps = "1. Peel and chop the " + list(fruits) + ".\n"
                    + "2. Blend with the " + list(dairy) + " and a few ice cubes.\n"
                    + "3. Sweeten with a little honey or sugar if needed and drink right away.";
            out.add(new Recipe(cap(first(fruits)) + " smoothie", 5,
                    list(merge(fruits, dairy)), steps));
        }

        // Vegetables only: quick sauté.
        if (!veggies.isEmpty() && bases.isEmpty() && proteins.isEmpty()) {
            String steps = "1. Chop the " + list(veggies) + " into even pieces.\n"
                    + "2. Heat oil in a pan, add garlic, then the " + list(veggies) + ".\n"
                    + "3. Cook on medium-high heat for 5 to 8 minutes, then season with salt and pepper.\n"
                    + "4. Serve as a side, in a wrap or over rice.";
            out.add(new Recipe("Quick garlic sauté", 15, list(veggies), steps));
        }

        // Generic recipes that still name the real ingredients, used to fill up to three.
        String all = list(ingredients);
        String allUsed = list(ingredients);
        out.add(new Recipe(
                "Use-it-up skillet",
                20,
                allUsed,
                "1. Chop the " + all + " into small, even pieces.\n"
                        + "2. Heat a little oil in a pan and cook the firmer items first.\n"
                        + "3. Add the softer items, salt and your favourite spices.\n"
                        + "4. Cook until tender and serve with rice, roti or bread."));
        out.add(new Recipe(
                "Clean-out-the-fridge soup",
                30,
                allUsed,
                "1. Saute chopped onion or garlic in a pot.\n"
                        + "2. Add the " + all + " and cover with water or stock.\n"
                        + "3. Simmer for 15 to 20 minutes, then season.\n"
                        + "4. Blend for a smooth soup, or leave it chunky."));

        return new ArrayList<>(out.subList(0, Math.min(3, out.size())));
    }

    // ---- helpers ----

    /** True if any word in the item name matches a keyword (also matches simple plurals). */
    private static boolean matches(String name, List<String> keywords) {
        String[] tokens = name.toLowerCase(Locale.getDefault()).split("[^a-z]+");
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            for (String keyword : keywords) {
                if (token.equals(keyword) || token.equals(keyword + "s") || token.equals(keyword + "es")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> merge(List<String> a, List<String> b) {
        List<String> result = new ArrayList<>(a);
        result.addAll(b);
        return result;
    }

    private static String first(List<String> items) {
        return items.get(0);
    }

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return "";
        String lower = s.toLowerCase(Locale.getDefault());
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    /** "a", "a and b", or "a, b and c", all lower case. */
    private static String list(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(i == items.size() - 1 ? " and " : ", ");
            sb.append(items.get(i).toLowerCase(Locale.getDefault()));
        }
        return sb.toString();
    }
}