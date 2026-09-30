package com.aspire.ecoplate;

public final class Config {

    private Config() {}

    // Pulls securely from BuildConfig (loaded from git-ignored local.properties)
    public static final String GROQ_API_KEY = BuildConfig.GROQ_API_KEY;

    // Free-tier model available on all Groq API keys
    public static final String GROQ_MODEL = "llama-3.1-8b-instant";

    public static boolean isGroqConfigured() {
        return GROQ_API_KEY != null
                && !GROQ_API_KEY.trim().isEmpty()
                && !GROQ_API_KEY.startsWith("YOUR_");
    }

    // ---- Option 2: IBM Granite on watsonx.ai (disabled) ----
    public static final String IBM_API_KEY = "";
    public static final String IBM_PROJECT_ID = "";
    public static final String IBM_REGION = "us-south";
    public static final String MODEL_ID = "ibm/granite-3-8b-instruct";

    public static boolean isConfigured() {
        return !IBM_API_KEY.trim().isEmpty() && !IBM_PROJECT_ID.trim().isEmpty();
    }
}