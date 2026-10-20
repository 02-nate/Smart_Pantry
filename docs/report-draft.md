# Smart Pantry Manager — Report (draft)

> Draft for the student to edit. Replace every `[...]` placeholder, insert the screenshots and rewrite in your own words where needed.

---

## 1. Cover page

**Smart Pantry Manager**
Mobile App Development 700

Student: Zuziwe Shelembe
Student number: 402414481
Lecturer: [LECTURER NAME]
Submission date: [DATE]
Repository: [GITHUB URL]

---

## 2. Table of contents

1. Cover page
2. Table of contents
3. Introduction
4. System design
5. Screenshots
6. Key code snippets
7. Challenges and solutions
8. Conclusion and reflection
9. References

---

## 3. Introduction

Food waste often starts at home: ingredients are forgotten at the back of the cupboard, go past their expiry date and are thrown away. Smart Pantry Manager is an offline Android app, written in Java, that lets a user record what is in their pantry and then suggests South African recipes they can cook with those ingredients.

The core rule is **strict matching**. A recipe is suggested under "Cook now" only if every ingredient is present in at least the required quantity, after converting units. As a bonus feature, recipes missing exactly one ingredient appear in a separate "Almost there" list, which tells the user what single item to buy.

The target users are students and households who cook on a budget and want to use what they already have.

[STUDENT: add one or two sentences on why you chose this problem.]

---

## 4. System design

### 4.1 Architecture

The code is organised in layers:

| Package | Responsibility |
|---|---|
| `model` | Plain Java data classes: `PantryItem`, `Recipe`, `RecipeIngredient` |
| `data` | `DatabaseHelper` (SQLiteOpenHelper singleton) and `RecipeSeeder` (20 seed recipes) |
| `matching` | Pure-Java matching logic with no Android imports, so it is unit tested with JUnit |
| `ui` | Five Activities and two RecyclerView adapters |
| `util` | `NavHelper` (bottom navigation), `Prefs` (SharedPreferences), `QuantityFormatter` |

### 4.2 Screen flow

See `docs/screen-flow.md` (Mermaid flowchart). [Export the diagram as an image and insert it here.]

### 4.3 Database design

SQLite through `SQLiteOpenHelper`, with three tables: `pantry_items`, `recipes` and `recipe_ingredients`. Each recipe has many ingredients (a one-to-many relationship), and deleting a recipe cascades to its ingredients. Foreign keys are enabled in `onConfigure`. Every query uses `?` placeholder arguments. See `docs/er-diagram.md`. [Insert the exported ER diagram here.]

On first run, `onCreate` seeds the 20 recipes and a demo pantry in a single transaction. Because `onCreate` only runs when the database file is first created, the user's later edits and deletions are never overwritten.

**Why SQLite:** the data is stored on the device and works offline, needs no account or network, suits one user's small relational data set, matches the module's persistent-data content, and persists across app restarts.

### 4.4 Matching algorithm

1. Normalise names: lower-case, strip punctuation, singularise the last word (irregular plurals such as "cheeses" → "cheese" and "leaves" → "leaf" are looked up first, then suffix rules apply), then apply the alias map.
2. Convert every quantity to a base unit (g, ml or pcs) and sum duplicate pantry rows. Expired items are skipped.
3. Treat mass and volume as interchangeable (1 g ≈ 1 ml); compare count only with count.
4. Apply no exceptions: every listed ingredient must be in the pantry. Water is not listed as an ingredient; the recipes give the water amount in their method steps instead.
5. For each recipe, count the ingredients that are missing or insufficient: 0 → "Cook now", 1 → "Almost there", 2 or more → not shown.

### 4.5 Design decisions

- **1 g ≈ 1 ml.** Mass and volume are treated as interchangeable, so "500 ml milk" in the pantry satisfies "500 g milk" in a recipe. Real densities differ per ingredient, but storing a density for every ingredient would add a lot of data for little benefit in a household app. Counted items (pcs) are never converted, because there is no reliable way to turn "3 onions" into grams.
- **No special cases in strict matching.** Every listed ingredient is required, so the "4 of 5 ingredients is not enough" rule holds without exceptions. Water, which users do not track, is described in the method steps rather than listed as an ingredient.
- **Demo pantry on first run.** The database seeds a demo pantry when it is created, so the suggestions are visible straight away. It is never re-inserted, so user edits are kept.

---

## 5. Screenshots

[SCREENSHOT: Pantry list — pre-loaded demo pantry on first launch]
*Figure 1: The pantry list on first launch. The demo pantry is inserted when the database is created, so the items are sorted alphabetically with quantities formatted using auto-scale (for example 2 kg, 1 L).*

[SCREENSHOT: Pantry list — empty state after Settings → Clear pantry]
*Figure 2: The empty-state message, reached through Settings → Clear pantry, which guides the user to the "Add ingredient" button. The demo data only returns after the app is reinstalled or its storage is cleared, so take this screenshot last.*

[SCREENSHOT: Add Ingredient form — empty]
*Figure 3: The Add Ingredient form with name, quantity, unit dropdown and optional expiry date.*

[SCREENSHOT: Add Ingredient validation error — empty name]
*Figure 4: Inline validation using TextInputLayout.setError when the name is left empty. Nothing is saved.*

[SCREENSHOT: Add Ingredient validation error — quantity 0 or above 100000]
*Figure 5: The quantity range check rejecting an invalid amount.*

[SCREENSHOT: Add Ingredient validation error — invalid characters in name]
*Figure 6: The name check rejecting digits and symbols.*

[SCREENSHOT: Expiry date picker dialog]
*Figure 7: The DatePickerDialog used to choose an optional expiry date.*

[SCREENSHOT: Edit Ingredient form — pre-filled]
*Figure 8: Edit mode, opened by tapping a pantry row. The title changes to "Edit ingredient" and the Delete button appears.*

[SCREENSHOT: Delete confirmation dialog]
*Figure 9: The MaterialAlertDialog asking the user to confirm before an ingredient is deleted.*

[SCREENSHOT: Suggested recipes — "Cook now" strict matches]
*Figure 10: Strict matches for the demo pantry: Chakalaka, Egg and Tomato Scramble, and Pap and Tomato Gravy.*

[SCREENSHOT: Suggested recipes — empty suggestions message]
*Figure 11: The message shown when no recipe matches the pantry.*

[SCREENSHOT: Suggested recipes — "Almost there" list]
*Figure 12: Recipes missing exactly one ingredient, shown below a divider with the missing item named.*

[SCREENSHOT: Recipe detail with ✓ / ✗ ingredient status]
*Figure 13: Recipe detail showing which ingredients are in the pantry (✓) and which are missing (✗), followed by the numbered method.*

[SCREENSHOT: Settings screen]
*Figure 14: Settings with the highlight switch, the display-units choice, the "Clear pantry" button, and the About line.*

[SCREENSHOT: Clear pantry confirmation dialog]
*Figure 15: Confirmation before every pantry item is removed.*

[SCREENSHOT: Expiry highlighting — "Expires soon" (amber) and "Expired" (red)]
*Figure 16: Expiry highlighting on the pantry list. Expired items are labelled as not used for suggestions.*

[SCREENSHOT: Dark theme (optional)]
*Figure 17: The same screen in dark mode.*

---

## 6. Key code snippets

### 6.1 Name normalisation — `IngredientNormalizer.normalizeName`

```java
public static String normalizeName(String raw) {
    if (raw == null) {
        return "";
    }
    String cleaned = raw.toLowerCase(Locale.ROOT).trim()
            .replaceAll("[^\\p{L} ]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    if (cleaned.isEmpty()) {
        return "";
    }

    int lastSpace = cleaned.lastIndexOf(' ');
    String head = cleaned.substring(0, lastSpace + 1);
    String singular = head + singularize(cleaned.substring(lastSpace + 1));

    String canonical = ALIASES.get(singular);
    return canonical != null ? canonical : singular;
}
```

The pantry stores names exactly as the user typed them, so "Tomatoes", "tomato" and " TOMATO " must all be compared as the same ingredient. This method lower-cases the name, removes non-letters, singularises only the last word (so "bread rolls" becomes "bread roll" but "baked beans" keeps its first word; `singularize` checks a small irregular-plurals map such as "cheeses" → "cheese" before its suffix rules), and then applies an alias map so that regional names such as "mealie meal" match the recipe's "maize meal". The same method runs on both the pantry and the recipe side, which keeps comparisons consistent.

### 6.2 Matching core loop — `RecipeMatcher.match`

```java
for (Recipe recipe : recipes) {
    if (recipe.getIngredients().isEmpty()) {
        continue;
    }
    List<String> missing = new ArrayList<>();
    for (RecipeIngredient ingredient : recipe.getIngredients()) {
        if (!stock.covers(ingredient)) {
            missing.add(ingredient.getName());
        }
    }
    if (missing.isEmpty()) {
        strict.add(new MatchResult(recipe, missing));
    } else if (missing.size() == 1) {
        almostThere.add(new MatchResult(recipe, missing));
    }
}
```

`stock` holds the non-expired pantry totals per normalised name, already converted to base units. `covers` returns true when the available amount plus a small tolerance (1e-6) is at least the required amount; there are no exceptions. Recipes with no missing ingredients are strict matches. Recipes with exactly one are "Almost there". Empty recipes are skipped, so they are never suggested. Both lists are then sorted by name.

### 6.3 Updating a row — `DatabaseHelper.updatePantryItem`

```java
public int updatePantryItem(PantryItem item) {
    return getWritableDatabase().update(TABLE_PANTRY, toValues(item),
            COL_PANTRY_ID + " = ?", new String[]{String.valueOf(item.getId())});
}
```

The update uses a `ContentValues` object built by `toValues` and a parameterised `WHERE id = ?` clause. Because the id is passed as a bound argument rather than concatenated into the SQL, this avoids SQL injection. The method returns the number of rows changed, which the Add/Edit screen uses to decide whether to show "Ingredient saved" or an error.

### 6.4 Binding a row and opening the editor — `PantryAdapter.onBindViewHolder` and `PantryListActivity.openEditor`

```java
@Override
public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
    PantryItem item = getItem(position);
    Context context = holder.itemView.getContext();

    holder.name.setText(item.getName());
    holder.quantity.setText(
            QuantityFormatter.format(item.getQuantity(), item.getUnit(), autoScaleUnits));

    // Rows are recycled, so every highlight is reset before it is (maybe) reapplied.
    holder.resetHighlight();

    LocalDate expiry = RecipeMatcher.parseDate(item.getExpiryDate());
    if (expiry == null) {
        holder.expiry.setVisibility(View.GONE);
    } else {
        holder.expiry.setVisibility(View.VISIBLE);
        holder.expiry.setText(context.getString(R.string.expiry_on, DISPLAY_DATE.format(expiry)));
        if (highlightExpiring) {
            applyHighlight(holder, context, expiry, LocalDate.now());
        }
    }

    holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
}
```

```java
private void openEditor(long itemId) {
    Intent intent = new Intent(this, AddEditIngredientActivity.class);
    if (itemId >= 0) {
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, itemId);
    }
    startActivity(intent);
}
```

The adapter uses the ViewHolder pattern, so `findViewById` runs once per row view rather than on every scroll. Because RecyclerView reuses row views, any highlight left over from a previous item is reset before the new item is bound. A tap is passed to the Activity through the adapter's `OnItemClickListener` interface. The Activity then builds an explicit Intent carrying the item's id as `EXTRA_ITEM_ID`, which puts the Add/Edit screen into edit mode.

---

## 7. Challenges and solutions

### 7.1 [Challenge title]
[STUDENT: describe the real problem you hit]

**Solution:** [STUDENT: describe how you solved it]

### 7.2 [Challenge title]
[STUDENT: describe the real problem you hit]

**Solution:** [STUDENT: describe how you solved it]

### 7.3 [Challenge title]
[STUDENT: describe the real problem you hit]

**Solution:** [STUDENT: describe how you solved it]

---

## 8. Conclusion and reflection

[STUDENT: summarise what the app achieves against the brief.]

[STUDENT: reflect on what you learned, for example about RecyclerView adapters, Intents, the Activity lifecycle and SQLite.]

**What I would improve**

- **Move database work off the main thread.** All queries and writes currently run on the UI thread, in `onResume()` and in `AddEditIngredientActivity.save()`. That is fine for a small pantry, but a larger data set could make the UI stutter. I would run them on a background `Executor` and post the results back to the UI thread.
- **Replace the destructive `onUpgrade()`.** `DatabaseHelper.onUpgrade()` drops and recreates every table, which is acceptable at version 1 but would erase the user's pantry on any future schema change. I would write step-by-step migrations (for example `ALTER TABLE`) that keep existing data.
- [STUDENT: add your own ideas. Possible ideas: per-ingredient densities instead of 1 g ≈ 1 ml, a shopping list built from "Almost there", or deducting ingredients after cooking.]

---

## 9. References

Android Developers (n.d.) *Activity lifecycle*. Available at: https://developer.android.com/guide/components/activities/activity-lifecycle (Accessed: [DATE]).

Android Developers (n.d.) *Create dynamic lists with RecyclerView*. Available at: https://developer.android.com/develop/ui/views/layout/recyclerview (Accessed: [DATE]).

Android Developers (n.d.) *Intents and intent filters*. Available at: https://developer.android.com/guide/components/intents-filters (Accessed: [DATE]).

Android Developers (n.d.) *Save data using SQLite*. Available at: https://developer.android.com/training/data-storage/sqlite (Accessed: [DATE]).

Android Developers (n.d.) *SQLiteOpenHelper*. Available at: https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper (Accessed: [DATE]).

Android Developers (n.d.) *Save simple data with SharedPreferences*. Available at: https://developer.android.com/training/data-storage/shared-preferences (Accessed: [DATE]).

Material Design (n.d.) *Material Components for Android*. Available at: https://m3.material.io/develop/android/mdc-android (Accessed: [DATE]).
