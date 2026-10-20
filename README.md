# Smart Pantry Manager

An offline Android app that tracks the ingredients in your pantry and suggests South African recipes you can cook with what you already have.

Households often throw food away because they forget what is in the cupboard or cannot think of a meal that uses it. Smart Pantry Manager is aimed at students and families who want to cook from what they already own: you record your ingredients (with optional expiry dates) and the app shows the recipes you can make right now.

Suggestions follow a **strict matching rule**: a recipe appears under "Cook now" only when **every** ingredient is in the pantry in **at least** the required quantity. Recipes missing exactly one ingredient are listed separately under "Almost there", so you know what single item to buy.

## Features

- Opens with a pre-loaded demo pantry on first run, so suggestions appear straight away (your later edits and deletions are kept)
- Add, edit and delete pantry ingredients with quantity, unit and an optional expiry date
- Validation on every field (name, quantity range, unit)
- 20 built-in South African recipes (Chakalaka, Pap and Tomato Gravy, Bobotie, Vetkoek and more)
- Strict "Cook now" suggestions plus an "Almost there — missing 1 ingredient" list
- Recipe detail with a ✓ / ✗ status for each ingredient and a numbered method
- Expiry highlighting: amber for items expiring within 3 days, red for expired items (expired items are never used for suggestions)
- Settings: toggle highlighting, choose base units (g / ml) or auto-scale (kg / L), clear the pantry
- Bottom navigation between Pantry, Suggestions and Settings
- Material 3 design with light and dark themes
- Fully offline: no account, no network, no location

## Database choice

The app uses **SQLite through `SQLiteOpenHelper`** (`data/DatabaseHelper.java`), with three tables: `pantry_items`, `recipes` and `recipe_ingredients` (one recipe has many ingredients, with `ON DELETE CASCADE`).

Why SQLite:

- **On-device and offline.** The database lives inside the app's private storage, so the app works with no signal.
- **No account or network needed.** There is no sign-up, server or cloud cost, and no personal data leaves the phone.
- **Fits a single-user pantry.** One person's pantry is a small, structured, relational data set, which suits SQLite.
- **Matches the module content.** It applies the module's persistent-data topic directly: schema design, parameterised queries, cursors and transactions.
- **Data persists across restarts.** Pantry items survive closing the app and rebooting the device. Small user preferences are stored separately in `SharedPreferences`.

## How matching works

`RecipeMatcher` (pure Java, unit tested) first **normalises** every ingredient name on both sides: lower-case, punctuation removed, last word singularised ("Tomatoes" → "tomato", with a small irregular-plurals list such as "cheeses" → "cheese" and "leaves" → "leaf") and common aliases mapped ("mealie meal" → "maize meal", "sunflower oil" → "oil"). Quantities are then **converted to base units** (g, ml or pcs; for example 1 kg = 1000 g, 1 tbsp = 15 ml), and duplicate pantry rows of the same ingredient are added together. As a deliberate simplification, mass and volume are treated as interchangeable using **1 g ≈ 1 ml**, while counted items (pcs) are only compared with counted items. There are **no exceptions**: every listed ingredient must be in the pantry. Water is not an ingredient; recipes give the water amount in their method steps instead. **Expired items** (expiry date before today) are ignored. A recipe with no missing ingredients goes into "Cook now". One with exactly one missing or insufficient ingredient goes into "Almost there". Any other recipe is not shown.

## Setup and run

**Requirements**

- Android Studio 2025.3 or newer (the project uses Android Gradle Plugin 9.0)
- JDK 17 or newer (Android Studio's bundled JDK works)
- Android SDK Platform 36
- **Minimum Android version:** Android 8.0 (API 26). Target SDK 36.

**Run the app**

1. Clone the repository: `git clone <repository-url>`
2. In Android Studio choose **File → Open** and select the project folder.
3. Let Gradle sync finish.
4. Pick an emulator or a connected device running Android 8.0 or later and press **Run**.
5. The app opens with a demo pantry already loaded. To start from scratch use **Settings → Clear pantry**.

> The demo pantry is only added when the database is first created. To get it back after clearing or editing the pantry (or if an older build was already installed), uninstall the app or clear its storage, then run it again.

**Run the unit tests**

- In Android Studio: right-click `app/src/test/java` and choose **Run 'Tests in …'**.
- From a terminal in the project folder:
  - Windows: `gradlew.bat testDebugUnitTest`
  - macOS / Linux: `./gradlew testDebugUnitTest`

## Project structure

```
app/src/main/java/com/example/smartpantrymanager/
├── model/
│   ├── PantryItem.java
│   ├── Recipe.java
│   └── RecipeIngredient.java
├── data/
│   ├── DatabaseHelper.java          SQLiteOpenHelper singleton: schema, pantry CRUD, recipe reads
│   └── RecipeSeeder.java            20 seed recipes + demo pantry (pre-loaded on first run)
├── matching/                        pure Java, no Android imports
│   ├── Dimension.java
│   ├── UnitType.java
│   ├── IngredientNormalizer.java
│   ├── MatchResult.java
│   └── RecipeMatcher.java
├── ui/
│   ├── PantryListActivity.java      launcher
│   ├── AddEditIngredientActivity.java
│   ├── SuggestedRecipesActivity.java
│   ├── RecipeDetailActivity.java
│   ├── SettingsActivity.java
│   ├── PantryAdapter.java
│   └── RecipeAdapter.java
└── util/
    ├── NavHelper.java
    ├── Prefs.java
    └── QuantityFormatter.java

app/src/test/java/com/example/smartpantrymanager/
├── data/
│   └── RecipeSeederTest.java
├── matching/
│   ├── IngredientNormalizerTest.java
│   └── RecipeMatcherTest.java
└── util/
    └── QuantityFormatterTest.java

docs/
├── screen-flow.md
├── er-diagram.md
├── report-draft.md
└── video-script.md
```

## Author

Zuziwe Shelembe — 402414481
