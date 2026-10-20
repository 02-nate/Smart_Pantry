# CLAUDE.md — Smart Pantry Manager (Mobile App Development 700)

## Mission

Build the complete **Smart Pantry Manager** Android app in **Java** inside this Android Studio project, in one run, by executing every phase below in order. After each phase:
1. Make sure the code compiles.
2. Make one git commit using the exact message given for that phase.

When you finish all phases, print a short summary covering:
- the files you created
- the result of the build and the tests
- anything the student must do by hand

## Hard rules

- **Java only.** Do not write any Kotlin source files. The Gradle Kotlin DSL (`build.gradle.kts`) and the version catalog (`gradle/libs.versions.toml`) are fine and should be kept.
- **No forbidden features.** Do not use Google Maps, any mapping SDK, location or GPS permissions, payments, or networking. The app is fully offline.
- **Database is SQLite only**, via `SQLiteOpenHelper`. Do not use Room, Firebase, or any ORM.
- **Keep the existing package name.** Read `namespace` / `applicationId` from `app/build.gradle.kts` and do not rename it. Below, `<pkg>` means that package.
- **Comments are for a marker, not a tutorial.** Use short Javadoc on public classes and methods, plus brief comments where a design decision is not obvious. Do not write step-by-step instructional comments, TODOs, or "Claude" references.
- **No hard-coded UI text in Java or XML.** Put every user-facing string in `res/values/strings.xml`, every colour in `colors.xml`, and every repeated dimension in `dimens.xml`.
- **Stay small.** Do not add third-party libraries beyond AndroidX, Material Components, and JUnit, all of which are already in the template. Add `androidx.recyclerview` through the version catalog only if it is not already available.
- **Honest git history.** Never rewrite git history, force-push, amend, or set fake commit dates.
- **Remove the template launcher.** Delete the template `MainActivity` (Java class and layout) and make `PantryListActivity` the launcher activity.

## Architecture

```
app/src/main/java/<pkg>/
├── model/
│   ├── PantryItem.java          id, name, quantity (double), unit (String), expiryDate (String yyyy-MM-dd or null)
│   ├── Recipe.java              id, name, steps (String, newline-separated), List<RecipeIngredient> ingredients
│   └── RecipeIngredient.java    id, recipeId, name, quantity (double), unit (String)
├── data/
│   ├── DatabaseHelper.java      singleton SQLiteOpenHelper: schema, pantry CRUD, recipe reads
│   └── RecipeSeeder.java        inserts the 20 seed recipes (called from onCreate)
├── matching/
│   ├── UnitType.java            enum: G, KG, ML, L, TSP, TBSP, CUP, PCS (each with Dimension and base factor)
│   ├── Dimension.java           enum: MASS, VOLUME, COUNT
│   ├── IngredientNormalizer.java  name normalisation + unit parsing
│   ├── MatchResult.java         recipe + list of missing/short ingredient names
│   └── RecipeMatcher.java       strict list + "Almost There" list (pure Java, no android.* imports)
├── ui/
│   ├── PantryListActivity.java       (LAUNCHER)
│   ├── AddEditIngredientActivity.java
│   ├── SuggestedRecipesActivity.java
│   ├── RecipeDetailActivity.java
│   ├── SettingsActivity.java
│   ├── PantryAdapter.java
│   └── RecipeAdapter.java
└── util/
    ├── NavHelper.java           wires BottomNavigationView between the 3 top-level screens
    ├── Prefs.java               SharedPreferences keys + getters/setters
    └── QuantityFormatter.java   display formatting (respects units preference)

app/src/test/java/<pkg>/matching/
├── IngredientNormalizerTest.java
└── RecipeMatcherTest.java
```

`matching/` must have **no Android dependencies**, so it can be unit-tested with plain JUnit.

## Database schema (`DatabaseHelper`, DB name `smart_pantry.db`, version 1)

```sql
CREATE TABLE pantry_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  quantity REAL NOT NULL CHECK (quantity > 0),
  unit TEXT NOT NULL,
  expiry_date TEXT
);
CREATE TABLE recipes (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  steps TEXT NOT NULL
);
CREATE TABLE recipe_ingredients (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  quantity REAL NOT NULL,
  unit TEXT NOT NULL
);
```

**Setup**
- Enable foreign keys in `onConfigure` with `db.setForeignKeyConstraintsEnabled(true)`.
- `onCreate` creates the tables, calls `RecipeSeeder.seed(db)`, then inserts `RecipeSeeder.demoPantry()`, all inside one transaction. Because `onCreate` only runs when the database file is first created, the demo pantry is pre-loaded on first run only and never overwrites later user edits.
- `onUpgrade` drops and recreates the tables.
- Use a thread-safe singleton: `getInstance(Context)` using the application context.
- Use table and column name constants. Always use parameterised queries (`?` args), never string concatenation. Close every Cursor in a try-with-resources or finally block.

**Methods**
- `long insertPantryItem(PantryItem)`
- `List<PantryItem> getAllPantryItems()`, ordered by name with `COLLATE NOCASE`
- `PantryItem getPantryItem(long id)`
- `int updatePantryItem(PantryItem)`
- `int deletePantryItem(long id)`
- `void clearPantry()`
- `List<Recipe> getAllRecipesWithIngredients()`
- `Recipe getRecipe(long id)`
- `int getRecipeCount()`

## Matching logic (the core of the assignment; the marker tests it directly)

### Name normalisation (`IngredientNormalizer.normalizeName`)

Apply these steps in order:
1. Convert to lowercase and trim.
2. Replace every character that is not a letter or a space with a space, then collapse repeated whitespace.
3. **Singularise the last word only**. First look the word up in an **irregular-plurals map** (cheeses→cheese, leaves→leaf, loaves→loaf, knives→knife, and molasses, hummus and couscous unchanged). If it is not there, apply these rules in order:
   - `ies` → `y`
   - `oes` → `o`
   - ends in `ses`, `xes`, `ches` or `shes` → strip `es`
   - ends in `s` but not in `ss`, `us` or `is` → strip `s`
4. Apply the **alias map** to the whole singular phrase. The table below maps each canonical name (left) to the phrases that become it (right).

| Canonical name | Aliases |
|---|---|
| `maize meal` | `mealie meal`, `mielie meal`, `maize` |
| `beef mince` | `mince`, `minced beef`, `ground beef` |
| `cake flour` | `flour`, `plain flour`, `all purpose flour` |
| `oil` | `cooking oil`, `sunflower oil`, `vegetable oil` |
| `tuna` | `tinned tuna`, `canned tuna` |
| `chicken` | `chicken piece`, `chicken breast`, `chicken thigh` |
| `garlic` | `garlic clove` |
| `instant yeast` | `yeast`, `dry yeast` |
| `sugar` | `white sugar`, `brown sugar` |
| `mayonnaise` | `mayo` |
| `green pepper` | `bell pepper`, `green bell pepper` |
| `tomato sauce` | `ketchup` |
| `boerewors` | `wors`, `sausage` |

The same normaliser is applied to **both** pantry names and recipe ingredient names, so "Tomatoes", "tomato" and " TOMATO " all match each other.

### Unit parsing (`IngredientNormalizer.parseUnit`)

These spellings are accepted for each unit (case-insensitive):

| Unit | Accepted spellings |
|---|---|
| G | g, gram, grams |
| KG | kg, kilogram, kilograms |
| ML | ml, millilitre(s), milliliter(s) |
| L | l, litre(s), liter(s) |
| TSP | tsp, teaspoon(s) |
| TBSP | tbsp, tablespoon(s) |
| CUP | cup, cups |
| PCS | pcs, pc, piece(s), each, unit(s), whole |

Base factors convert each unit to its base:

| Unit | Base factor | Dimension |
|---|---|---|
| G | 1 | MASS |
| KG | 1000 | MASS |
| ML | 1 | VOLUME |
| L | 1000 | VOLUME |
| TSP | 5 | VOLUME |
| TBSP | 15 | VOLUME |
| CUP | 250 | VOLUME |
| PCS | 1 | COUNT |

An unknown unit throws `IllegalArgumentException`. The UI only offers spinner values, so this is a defensive check.

### `RecipeMatcher.match(List<PantryItem> pantry, List<Recipe> recipes, LocalDate today)`

The method returns `MatchResults` holding two lists: `strict` and `almostThere`.

1. **Build pantry totals.** For each pantry item that is **not expired** (expiry date is before `today`):
   - key = normalised name
   - add `quantity × factor` to that key's total for its Dimension
   
   Duplicate pantry rows of the same ingredient are summed.
2. **Compatibility.**
   - COUNT is only compared with COUNT.
   - MASS and VOLUME are treated as interchangeable using the documented approximation **1 g ≈ 1 ml**. For a MASS or VOLUME requirement, the available amount is `mass + volume` for that name.
   - Document this in the class Javadoc as a deliberate simplification.
3. **No exceptions.** Every listed ingredient must be in the pantry; nothing is treated as always available. Tap water is not tracked, so recipes mention water in their method steps rather than their ingredient lists.
4. **Check each recipe.** For each recipe ingredient, compute the required amount in base units. It counts as **missing** if `available + 1e-6 < required`, which covers both "absent" and "not enough".
5. **Classify each recipe.**
   - A recipe with 0 missing ingredients goes into `strict`.
   - A recipe with exactly 1 missing ingredient goes into `almostThere`, recording the missing name.
   - Otherwise it is excluded.
   - A recipe with no ingredients is never suggested.
6. Sort both lists alphabetically by recipe name.

### Unit tests (JUnit 4, `app/src/test`)

All of the following must pass.

**Normaliser**
- tomatoes→tomato, potatoes→potato, berries→berry, " Onions "→onion
- "bread rolls"→"bread roll"
- "mealie meal"→"maize meal"
- "Sunflower Oil"→"oil"
- hummus unchanged
- cheeses→cheese, "Bay Leaves"→"bay leaf" (irregular plurals)
- KG parses with factor 1000
- "tbsp" parses as VOLUME 15
- unknown unit throws

**Matcher**
1. All ingredients present → recipe is in `strict`.
2. 4 of 5 ingredients present → recipe is **not** in `strict` and **is** in `almostThere`.
3. 3 of 5 ingredients present → recipe is in neither list.
4. Pantry "Tomatoes 3 pcs" satisfies recipe "tomato 2 pcs".
5. Pantry "1 kg maize meal" satisfies "500 g maize meal".
6. Pantry "400 g" does **not** satisfy "500 g".
7. Two pantry rows of 250 g sum to satisfy 500 g.
8. An expired item is ignored.
9. A PCS requirement is not satisfied by a gram quantity of the same name.
10. Every recipe ingredient must be present, with no exceptions (a recipe listing water is not strict when the pantry has no water).
11. A recipe with an empty ingredient list is never suggested.

## Screens

The theme is Material 3 DayNight, with `colorPrimary` #2E7D32 (green) and `colorSecondary` #F9A825 (amber).

**Layout rules**
- Use ConstraintLayout or LinearLayout.
- Use `TextInputLayout` for form fields.
- The three top-level screens (Pantry, Suggestions, Settings) share a `BottomNavigationView` defined in `res/menu/bottom_nav_menu.xml`, wired through `NavHelper`.
  - Switching tabs starts the target Activity with `FLAG_ACTIVITY_REORDER_TO_FRONT`, removes the transition animation, and highlights the current tab.
- Detail and edit screens show an Up arrow in the toolbar.
- Refresh lists in `onResume()`, which demonstrates the Activity lifecycle.

### 1. PantryListActivity (launcher)

**Layout**
- Toolbar titled "My Pantry"
- RecyclerView using `PantryAdapter`
- Extended FAB labelled "Add ingredient"
- An empty-state TextView ("Your pantry is empty — tap Add ingredient to start.") shown when the list is empty

**Each row (`item_pantry.xml`, a MaterialCardView)**
- Name
- Quantity and unit, formatted by `QuantityFormatter`
- Expiry line, when there is an expiry date

**Expiry highlighting**, when the "Highlight expiring items" preference is on:
- Expiring within 3 days: amber background tint and the label "Expires soon".
- Already expired: red text and the label "Expired — not used for suggestions".

**Interaction**
- Tapping a row opens AddEditIngredientActivity in edit mode, passing `EXTRA_ITEM_ID` (a `long`) through an Intent.
- `PantryAdapter` uses the ViewHolder pattern and exposes an `OnItemClickListener` interface.

### 2. AddEditIngredientActivity

**Fields**
- **Name:** TextInputEditText.
- **Quantity:** decimal input type.
- **Unit:** a dropdown (exposed `MaterialAutoCompleteTextView`) with g, kg, ml, L, tsp, tbsp, cup, pcs.
- **Expiry date:** optional. Shown as a read-only field that opens a `DatePickerDialog`, with a "Clear date" button.
- **Buttons:** Save and Delete.

**Modes**
- With `EXTRA_ITEM_ID` present, the screen is in edit mode. The title becomes "Edit ingredient" and the fields are pre-filled.
- Without it, the title is "Add ingredient" and the Delete button is hidden.

**Validation** uses `TextInputLayout.setError`. Nothing is saved while any error is shown.
- Name is required, 2–40 characters, letters, spaces and hyphens only.
- Quantity is required, numeric, greater than 0 and at most 100000.
- Unit is required and must be one of the listed values.

**Saving and deleting**
- **Save** inserts or updates the item, shows a Toast ("Ingredient saved"), and calls `finish()`.
- **Delete** shows a confirmation `MaterialAlertDialog`, then deletes the item, shows a Toast, and calls `finish()`.
- Store the name exactly as typed (trimmed). Normalisation happens only during matching.

### 3. SuggestedRecipesActivity

**Data**
- Load the pantry and the recipes from the database.
- Run `RecipeMatcher` with `LocalDate.now()`. `java.time` is fine on minSdk 26+. If the project's minSdk is below 26, enable core library desugaring through the version catalog.

**Section 1: "Cook now"** (the strict matches)
- Shown in a RecyclerView using `RecipeAdapter`.
- Each card shows the recipe name and "N ingredients • all in your pantry".
- If there are no strict matches, show: "No recipes match your pantry yet — add more ingredients."

**Section 2: "Almost there — missing 1 ingredient"** (the bonus feature)
- Shown in a second RecyclerView, below a clear divider and header, so it stays visually separate from the strict list.
- Each card shows "Missing: <ingredient>".
- If this list is empty, hide the whole section.
- Use a NestedScrollView with both RecyclerViews set to `nestedScrollingEnabled=false`.

**Interaction**
- Tapping any card opens RecipeDetailActivity with `EXTRA_RECIPE_ID`.

### 4. RecipeDetailActivity

**Data**
- Read `EXTRA_RECIPE_ID` from the Intent.
- If the recipe is missing, show a Toast and call `finish()`. The screen must never crash on a bad ID.

**Layout**
- **Header:** the recipe name.
- **"Ingredients" list:** quantity, unit and name for each ingredient. Each one has a ✓ (green) or ✗ (red) status, computed with the same normaliser and matcher helper so it stays consistent with the suggestions.
- **"Method":** the steps, numbered.

### 5. SettingsActivity

**Preferences** (stored in SharedPreferences via `Prefs`)
- **"Highlight expiring items":** a SwitchMaterial, default on.
- **"Display units":** a RadioGroup with two options:
  - **Base units** (g / ml): always shown in g and ml.
  - **Auto-scale** (kg / L): shows kg or L when the amount is 1000 g or 1000 ml or more. This is the default.

**Buttons**
- **"Clear pantry":** asks for confirmation with a `MaterialAlertDialog`, then calls `clearPantry()`. There is no button to load or reset the demo pantry; it is pre-loaded on first run only (see `DatabaseHelper.onCreate`), and coming back to it requires reinstalling the app or clearing its storage.

**Footer**
- An "About" line with the app name and version.

**Demo pantry** (defined in `RecipeSeeder.demoPantry()`, pre-loaded on first run, and chosen so that Chakalaka, Pap and Tomato Gravy, and Egg and Tomato Scramble match strictly, while several others are "almost there"):
- onion 3 pcs
- tomatoes 5 pcs
- carrots 2 pcs
- green pepper 1 pcs
- baked beans 410 g
- curry powder 50 g
- sunflower oil 750 ml
- eggs 6 pcs
- salt 500 g
- mealie meal 2 kg
- milk 1 L

## Seed recipes (`RecipeSeeder`): 20 South African-flavoured recipes

**Unit convention.** Use these exact names, quantities and units:
- dry ingredients → g
- liquids → ml
- countable ingredients → pcs

**Steps.** Write 3–5 short, practical preparation steps per recipe, joined with `\n`.

**Water is not an ingredient.** Where a recipe needs water, state the amount in its steps (for example "Bring 1 litre of salted water to the boil…"), not in the ingredient list.

| # | Recipe | Ingredients |
|---|---|---|
| 1 | Chakalaka | onion 1 pcs; tomato 2 pcs; carrot 2 pcs; green pepper 1 pcs; baked beans 410 g; curry powder 10 g; oil 30 ml |
| 2 | Pap and Tomato Gravy | maize meal 500 g; salt 5 g; tomato 3 pcs; onion 1 pcs; oil 30 ml |
| 3 | Vetkoek | cake flour 500 g; instant yeast 10 g; sugar 15 g; salt 5 g; oil 500 ml |
| 4 | Bobotie | beef mince 500 g; onion 1 pcs; bread slice 2 pcs; milk 250 ml; egg 2 pcs; curry powder 15 g; chutney 30 ml |
| 5 | Samp and Beans | samp 500 g; sugar beans 250 g; onion 1 pcs; salt 5 g |
| 6 | Egg and Tomato Scramble | egg 4 pcs; tomato 2 pcs; onion 1 pcs; oil 15 ml; salt 2 g |
| 7 | Morogo and Pap | spinach 300 g; onion 1 pcs; tomato 1 pcs; maize meal 300 g; oil 15 ml |
| 8 | Boerewors Rolls | boerewors 500 g; bread roll 4 pcs; onion 1 pcs; tomato sauce 60 ml |
| 9 | Chicken Curry | chicken 1000 g; onion 1 pcs; tomato 2 pcs; potato 3 pcs; curry powder 20 g; garlic 3 pcs; oil 30 ml |
| 10 | Potato Salad | potato 6 pcs; egg 3 pcs; mayonnaise 125 ml; onion 1 pcs |
| 11 | Pumpkin Fritters | pumpkin 500 g; cake flour 120 g; egg 2 pcs; sugar 15 g; cinnamon 5 g; oil 100 ml |
| 12 | French Toast | bread slice 4 pcs; egg 2 pcs; milk 125 ml; sugar 10 g; oil 30 ml |
| 13 | Spaghetti Bolognese | spaghetti 500 g; beef mince 500 g; onion 1 pcs; tomato 4 pcs; garlic 2 pcs |
| 14 | Vegetable Soup | potato 2 pcs; carrot 3 pcs; onion 1 pcs; cabbage 300 g; salt 5 g |
| 15 | Fried Cabbage | cabbage 500 g; onion 1 pcs; oil 30 ml; salt 3 g |
| 16 | Tuna Mayo Rice | rice 250 g; tuna 170 g; onion 1 pcs; mayonnaise 60 ml |
| 17 | Flapjacks | cake flour 250 g; milk 250 ml; egg 2 pcs; sugar 30 g; baking powder 10 g; oil 15 ml |
| 18 | Chicken Stew | chicken 1000 g; onion 2 pcs; carrot 2 pcs; potato 4 pcs; chicken stock 500 ml |
| 19 | Banana Bread | cake flour 250 g; banana 3 pcs; egg 2 pcs; sugar 150 g; baking powder 10 g; butter 100 g |
| 20 | Cheese Omelette | egg 3 pcs; cheese 50 g; onion 1 pcs; milk 30 ml |

## Phases and commits

Do the phases in this order. Commit after each one with the exact message shown. If the folder is not yet a git repo, run `git init` first.

1. **Setup.** Make `.gitignore` cover:
   - `build/`, `.gradle/`, `.idea/`, `local.properties`, `*.iml`
   - `captures/`, `.externalNativeBuild/`, `.cxx/`, `*.apk`, `*.aab`
   
   Also create a README stub containing the app name and a one-line description.
   Commit: `Configure project, .gitignore and README stub`
2. **Models.** Add the three model classes.
   Commit: `Add model classes for pantry items, recipes and ingredients`
3. **Database.** Add `DatabaseHelper` with the schema and pantry CRUD.
   Commit: `Add DatabaseHelper with schema and pantry CRUD`
4. **Seeding.** Add `RecipeSeeder` with all 20 recipes.
   Commit: `Seed 20 South African recipes on first run`
5. **Normalisation.** Add `Dimension`, `UnitType` and `IngredientNormalizer`.
   Commit: `Add ingredient name and unit normalisation`
6. **Matching.** Add `MatchResult` and `RecipeMatcher`.
   Commit: `Implement strict recipe matching with Almost There list`
7. **Tests.** Add both test classes and run them until they pass.
   Commit: `Add unit tests for normaliser and matcher`
8. **Pantry list.** Add PantryListActivity, PantryAdapter and their layouts, update the manifest launcher, and remove the template MainActivity.
   Commit: `Add Pantry List screen with RecyclerView adapter`
9. **Add/Edit.** Add AddEditIngredientActivity with validation, DatePicker and delete.
   Commit: `Add Add/Edit Ingredient screen with validation and delete`
10. **Suggestions and detail.** Add SuggestedRecipesActivity, RecipeAdapter and RecipeDetailActivity.
    Commit: `Add Suggested Recipes and Recipe Detail screens`
11. **Settings and navigation.** Add SettingsActivity, Prefs, NavHelper, the bottom nav menu and its icons (use Material vector drawables created as XML in `res/drawable`).
    Commit: `Add Settings screen and bottom navigation`
12. **Polish.** Clean up `QuantityFormatter`, expiry highlighting, strings, colours, dimens and content descriptions. Fix any warnings, and check every screen handles empty data without crashing.
    Commit: `Polish layouts, strings and expiry highlighting`
13. **Docs.** Finish the README and the `docs/` folder (see below).
    Commit: `Complete README and add report and video drafts`

**Verification** (run after phase 7 and again at the end):
- On Windows: `gradlew.bat assembleDebug` and `gradlew.bat testDebugUnitTest`.
- On macOS/Linux: `./gradlew assembleDebug` and `./gradlew testDebugUnitTest`.

Fix every failure before moving on. If Gradle cannot run in this environment (no Android SDK), say so clearly in the final summary and carry on.

At the end, if a remote named `origin` exists, run `git push -u origin main` (or the current branch). If no remote exists, print the exact commands the student needs to add one and push.

## README.md (final content)

The README needs these sections:
1. **App name and description:** the problem (food waste), who it is for, and the strict-matching rule.
2. **Features:** a short bullet list.
3. **Database choice:** SQLite via SQLiteOpenHelper, and why:
   - on-device and offline
   - no account or network needed
   - fits a single-user pantry
   - matches the module's persistent-data content
   - data persists across app restarts
4. **How matching works:** a short paragraph covering normalisation, unit conversion, the 1 g ≈ 1 ml approximation, the no-exceptions rule (water is not an ingredient), irregular plurals, and expired items being excluded.
5. **Setup and run:**
   - Android Studio version, plus JDK 17 and the Android SDK
   - clone, open, sync Gradle, and run on an emulator or device (state the min SDK)
   - how to run the unit tests
6. **Project structure:** the folder tree.
7. **Author:** `[STUDENT NAME] — [STUDENT NUMBER]` (leave as placeholders).

## docs/ (to help the student write the report and video; the student edits these)

- **`docs/screen-flow.md`:** a Mermaid flowchart showing:
  - the bottom-nav links between Pantry, Suggestions and Settings
  - Pantry → Add/Edit
  - Suggestions → Recipe Detail
  
  Label each arrow with the Intent extra it carries.
- **`docs/er-diagram.md`:** a Mermaid `erDiagram` of the three tables with the one-to-many relationship.
- **`docs/report-draft.md`:** every report section in the required order:
  1. Cover page
  2. Table of contents
  3. Introduction
  4. System design
  5. Screenshots
  6. Key code snippets
  7. Challenges and solutions
  8. Conclusion and reflection
  9. References
  
  Notes on content:
  - **Screenshots:** for every screen and action, add a placeholder line such as `[SCREENSHOT: Add Ingredient validation error — empty name]` followed by a draft caption. Cover at least: empty pantry, populated pantry, add form, validation errors, edit form, delete confirmation, strict suggestions, empty suggestions message, Almost There list, recipe detail with ✓/✗, settings, and expiry highlighting.
  - **Code snippets:** pick 4 real snippets — `normalizeName`, the `RecipeMatcher` core loop, `DatabaseHelper.updatePantryItem`, and the `PantryAdapter` `onBindViewHolder` with the Intent to edit. Give each a short explanation.
  - **Challenges:** leave headings with `[STUDENT: describe the real problem you hit]` placeholders. Do not invent experiences.
  - **References:** Harvard-style entries for the Android Developers documentation pages used (SQLiteOpenHelper, RecyclerView, Intents, Activity lifecycle, Material Components). Use real URLs only.
- **`docs/video-script.md`:** a timed 6-minute narration outline:
  - **0:00–1:00** — GitHub commit history.
  - **1:00–3:30** — demo, in this order:
    1. Show the pre-loaded demo pantry and the suggestions.
    2. Delete carrots and show Chakalaka move to Almost There.
    3. Add carrots back and show it return.
    4. Edit a quantity below the required amount and show the recipe disappear.
    5. Trigger a validation error.
    6. Close the app fully and reopen it to show the data persisted.
    7. Last: Settings → Clear pantry to show the empty states (the demo pantry cannot be restored without reinstalling).
  - **3:30–5:30** — explain three concepts, each with the exact file and method to point at: the Adapter, Intents, and the matching algorithm.
  - **5:30–6:15** — justify the database choice.
