# 🍃 EcoPlate: AI-Powered Smart Pantry & Food Waste Prevention Assistant

> Transforming expiring kitchen groceries into actionable, delicious meals through responsive Generative AI and habit gamification. Built for the **1M1B – IBM SkillsBuild AI + Sustainability Virtual Internship**.

---

## 📌 Project Overview

Household food waste contributes significantly to municipal landfills and greenhouse gas emissions. In most homes, edible food is discarded simply because items are tucked out of sight in refrigerators, or cooks lack immediate recipe inspiration for disparate leftover items nearing expiration.

**EcoPlate** is a native Android application designed to close this loop. By pairing low-friction shelf-life tracking with generative AI recipe synthesis and habit-reinforcing streaks, EcoPlate ensures kitchen groceries are consumed in time, not thrown away.

---

## 🎯 UN Sustainable Development Goals (SDGs)

* **SDG 12: Responsible Consumption and Production (Primary Target 12.3):** Halving per capita global food waste at the retail and consumer levels.
* **SDG 2: Zero Hunger:** Fostering conscious household food utilization and resource awareness.
* **SDG 13: Climate Action:** Reducing domestic organic waste destined for landfills, directly curtailing associated methane emissions.

---

## ✨ Key Features

* **Smart Expiry Dashboard:** Local SQLite database queries and displays pantry inventory sorted strictly by urgency. Dynamic indicators flag items as *Safe* (green), *Attention Needed* (amber), or *Expiring Today* (bold red alert).
* **Frictionless Logging:** Fast item entry with quick-select shelf-life chips (*In 3 days*, *In a week*, *In 2 weeks*) and an integrated Material calendar picker.
* **AI Recipe Synthesis ("Cook with what expires first"):** Selects up to 8 items nearing expiration and leverages LLM inference (via Groq `llama-3.3-70b-versatile` or IBM Granite on watsonx.ai) to craft up to 3 custom, waste-reducing recipes complete with time estimates and numbered steps.
* **Non-Food Safety Guardrails:** Built-in semantic prompt filters intercept non-food or hazardous inputs, preventing unsafe culinary suggestions.
* **"I Cooked This!" Gamification:** Interactive confirmation prompts mark ingredients as used upon cooking, incrementing a weekly food-saving streak counter stored via `SharedPreferences`.
* **Impact & Trend Analytics:** Real-time metrics tracking total *Items Used in Time*, *Items Wasted*, overall *Food Save Rate %*, and historical 6-week consumption trends.
* **Closed-Loop Restock Shopping List:** Seamlessly prompts users to add consumed staples to a built-in shopping checklist with checkbox toggles and quick-clear actions.
* **Offline Resilience:** Includes local rule-based fallback generation ensuring the app remains fully functional even without internet connectivity.

---

## 🛠️ Architecture & Tech Stack

* **Platform:** Android (minSdk 26, targetSdk 34)
* **Language:** Java
* **UI/UX:** Material 3 Design, Edge-to-Edge layout, AndroidX Splashscreen
* **Local Persistence:** Android SQLite (`SQLiteOpenHelper`) & `SharedPreferences`
* **AI & LLM Integration:**
  * **Groq API** (`llama-3.3-70b-versatile`) — Primary high-speed recipe inference
  * **IBM Granite** (`ibm/granite-3-8b-instruct` on watsonx.ai) — Enterprise sustainability foundation model
* **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with secure `BuildConfig` injection from `local.properties`

---

## 🔒 Security & API Configuration

This repository adheres to strict security best practices: **no API keys are hardcoded or committed to version control.**

To configure your private API keys locally:

1. Open (or create) `local.properties` in your project root directory.
2. Add your Groq API key:
   ```properties
   GROQ_API_KEY=gsk_your_actual_groq_api_key_here
