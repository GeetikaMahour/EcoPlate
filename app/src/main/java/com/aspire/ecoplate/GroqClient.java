package com.aspire.ecoplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GroqClient {

    private static final String GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    public static List<Recipe> suggestRecipes(List<String> ingredients) throws Exception {
        URL url = new URL(GROQ_ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + Config.GROQ_API_KEY.trim());
        conn.setDoOutput(true);
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(20000);

        String ingList = String.join(", ", ingredients);

        String systemPrompt = "You are a home cooking assistant that helps reduce food waste. "
                + "The user provides ingredients. If the list contains non-food, hazardous, or inedible items "
                + "(e.g., plastics, electronics, chemicals, words like 'online'), respond ONLY with JSON: "
                + "{\"error\":\"inedible_items\",\"message\":\"Those items do not look like food! Please add valid pantry ingredients.\"} "
                + "Otherwise, suggest up to 3 simple recipes. Output ONLY a valid JSON array of objects with keys: "
                + "\"title\" (string), \"minutes\" (int), \"uses\" (string), and \"steps\" (array of strings). "
                + "Do not use markdown backticks or extra text.";

        String userPrompt = "Ingredients: " + ingList;

        JSONArray messages = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.put(systemMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.put(userMsg);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", Config.GROQ_MODEL);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        InputStream stream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Groq HTTP " + responseCode + ": " + sb.toString());
        }

        return parseGroqResponse(sb.toString(), ingList);
    }

    private static List<Recipe> parseGroqResponse(String jsonString, String fallbackUses) throws Exception {
        JSONObject root = new JSONObject(jsonString);
        JSONArray choices = root.getJSONArray("choices");
        if (choices.length() == 0) {
            throw new RuntimeException("Groq returned no choices");
        }

        String rawContent = choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        // Check if the AI detected non-food ingredients
        if (rawContent.contains("inedible_items")) {
            throw new IllegalArgumentException("Those items don't look like food! Please add valid groceries to get recipes.");
        }

        // Clean out any accidental markdown fences
        int start = rawContent.indexOf('[');
        int end = rawContent.lastIndexOf(']');
        if (start < 0 || end <= start) {
            throw new RuntimeException("No JSON array returned: " + rawContent);
        }

        JSONArray array = new JSONArray(rawContent.substring(start, end + 1));
        List<Recipe> recipes = new ArrayList<>();

        for (int i = 0; i < array.length() && recipes.size() < 3; i++) {
            JSONObject obj = array.getJSONObject(i);
            String title = obj.optString("title", "Quick Pantry Meal").trim();
            int minutes = obj.optInt("minutes", 15);
            String uses = obj.optString("uses", fallbackUses).trim();

            StringBuilder stepsBuilder = new StringBuilder();
            JSONArray stepsArray = obj.optJSONArray("steps");
            if (stepsArray != null) {
                for (int s = 0; s < stepsArray.length(); s++) {
                    if (s > 0) stepsBuilder.append("\n");
                    stepsBuilder.append(s + 1).append(". ").append(stepsArray.optString(s, "").trim());
                }
            } else {
                stepsBuilder.append("1. Prepare ingredients.\n2. Cook in a pan until done.\n3. Season and serve.");
            }

            recipes.add(new Recipe(title, minutes, uses, stepsBuilder.toString()));
        }

        if (recipes.isEmpty()) {
            throw new RuntimeException("No valid recipes found in Groq response.");
        }

        return recipes;
    }
}