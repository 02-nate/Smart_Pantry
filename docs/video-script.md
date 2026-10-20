# Video script — about 6 minutes

> Narration outline for the student. Speak in your own words. Times are targets.

## 0:00–1:00 — GitHub commit history

- Open the repository on GitHub and show the **Commits** page.
- "I built the app in 13 phases, with one commit per phase, from project setup to documentation."
- Point out the order: models → database → seeding → normalisation → matching → tests → each screen → polish → docs.
- Open the commit **"Add unit tests for normaliser and matcher"** and say that the matching logic was tested before any UI was built.

## 1:00–3:30 — Live demo (emulator or phone)

> Before recording, uninstall the app (or clear its storage) so it starts with the pre-loaded demo pantry. Step 7 clears the pantry, so record it last.

1. **The pre-loaded demo pantry and its suggestions** (≈ 0:20)
   - Say that the app opens with a demo pantry pre-loaded on first run.
   - Pantry tab: show the 11 items. Point out "2 kg" mealie meal and "1 L" milk (auto-scale).
   - Suggestions tab: **Cook now** shows Chakalaka, Egg and Tomato Scramble, and Pap and Tomato Gravy. **Almost there** shows Cheese Omelette (cheese), Fried Cabbage (cabbage) and Morogo and Pap (spinach).
2. **Delete carrots, and Chakalaka moves to Almost There** (≈ 0:25)
   - Pantry → tap **carrots** → **Delete** → confirm.
   - Suggestions: Chakalaka is now under "Almost there" with "Missing: carrot".
3. **Add carrots back, and it returns** (≈ 0:25)
   - Pantry → **Add ingredient** → "Carrots", 2, pcs → Save.
   - Suggestions: Chakalaka is back under "Cook now". Mention that "Carrots" matched "carrot" because of normalisation.
4. **Edit a quantity below the required amount, and the recipe disappears** (≈ 0:25)
   - Pantry → tap **eggs** → change 6 to **3** → Save.
   - Suggestions: Egg and Tomato Scramble (needs 4 eggs) has left "Cook now" and moved to "Almost there". Open it to show ✗ next to egg.
5. **Trigger a validation error** (≈ 0:20)
   - Add ingredient → leave the name empty and enter quantity **0** → Save.
   - Show the inline errors, and that nothing was saved.
6. **Close the app fully and reopen it, and the data has persisted** (≈ 0:20)
   - Open Recents and swipe the app away, then relaunch it.
   - The pantry still shows the edited items, because they are stored in SQLite.
7. **Empty state** (≈ 0:15)
   - Settings → **Clear pantry** → confirm.
   - Pantry tab: show the empty-state message. Suggestions tab: show "No recipes match your pantry yet".

## 3:30–5:30 — Three concepts in the code

### Adapter (≈ 0:40)
- File: `app/src/main/java/com/example/smartpantrymanager/ui/PantryAdapter.java`
- Show `onCreateViewHolder` (inflates `item_pantry.xml`), the `PantryViewHolder` class (views found once) and `onBindViewHolder` (fills a row, resets and applies the expiry highlight).
- Mention the `OnItemClickListener` interface and DiffUtil via `ListAdapter`.

### Intents (≈ 0:40)
- File: `ui/PantryListActivity.java`, method `openEditor(long)`: an explicit Intent with `EXTRA_ITEM_ID`.
- File: `ui/AddEditIngredientActivity.java`, `onCreate`: reads `getLongExtra(EXTRA_ITEM_ID, -1)` to choose add or edit mode.
- File: `util/NavHelper.java`, method `setup`: tab switching with `FLAG_ACTIVITY_REORDER_TO_FRONT`.
- Mention `onResume()` reloading the lists after returning from another screen (the Activity lifecycle).

### Matching algorithm (≈ 0:40)
- File: `matching/IngredientNormalizer.java`, method `normalizeName`: lower-case, singularise, alias map.
- File: `matching/RecipeMatcher.java`, methods `buildStock` (base units, summing duplicates, skipping expired items) and `match` (counting missing ingredients: 0 → strict, 1 → almost there).
- Mention the 1 g ≈ 1 ml simplification and water as a staple.
- Briefly show `RecipeMatcherTest` passing in Android Studio.

## 5:30–6:15 — Database choice

- File: `data/DatabaseHelper.java`: three tables, a foreign key with `ON DELETE CASCADE`, and parameterised queries.
- Why SQLite with SQLiteOpenHelper:
  - on-device and fully offline
  - no account, server or network required
  - a single user's pantry is small, structured, relational data
  - matches the module's persistent-data content
  - data survives app restarts, as shown in the demo
- Close: "Thank you."
