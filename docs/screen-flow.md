# Screen flow

Solid arrows are explicit Intents. Dotted arrows are bottom-navigation tab switches, which start the target Activity with `FLAG_ACTIVITY_REORDER_TO_FRONT`. The label on each arrow names the Intent extra it carries.

```mermaid
flowchart LR
    Pantry["PantryListActivity<br/>(launcher)"]
    AddEdit["AddEditIngredientActivity"]
    Suggestions["SuggestedRecipesActivity"]
    Detail["RecipeDetailActivity"]
    Settings["SettingsActivity"]

    Pantry -- "FAB: no extra (add mode)" --> AddEdit
    Pantry -- "row tap: EXTRA_ITEM_ID (long)" --> AddEdit
    AddEdit -- "Save / Delete / Up: finish()" --> Pantry

    Suggestions -- "card tap: EXTRA_RECIPE_ID (long)" --> Detail
    Detail -- "Up: finish()" --> Suggestions

    Pantry -. "bottom nav: no extra" .-> Suggestions
    Suggestions -. "bottom nav: no extra" .-> Pantry
    Pantry -. "bottom nav: no extra" .-> Settings
    Settings -. "bottom nav: no extra" .-> Pantry
    Suggestions -. "bottom nav: no extra" .-> Settings
    Settings -. "bottom nav: no extra" .-> Suggestions
```
