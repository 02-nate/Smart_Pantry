package com.example.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton SQLite helper that owns the schema, pantry CRUD operations and recipe reads.
 * All queries use bound arguments and every Cursor is closed with try-with-resources.
 */
public final class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "smart_pantry.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_QUANTITY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    private static final String SQL_CREATE_PANTRY = "CREATE TABLE " + TABLE_PANTRY + " ("
            + COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_PANTRY_NAME + " TEXT NOT NULL, "
            + COL_PANTRY_QUANTITY + " REAL NOT NULL CHECK (" + COL_PANTRY_QUANTITY + " > 0), "
            + COL_PANTRY_UNIT + " TEXT NOT NULL, "
            + COL_PANTRY_EXPIRY + " TEXT)";

    private static final String SQL_CREATE_RECIPES = "CREATE TABLE " + TABLE_RECIPES + " ("
            + COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_RECIPE_NAME + " TEXT NOT NULL UNIQUE, "
            + COL_RECIPE_STEPS + " TEXT NOT NULL)";

    private static final String SQL_CREATE_RECIPE_INGREDIENTS = "CREATE TABLE "
            + TABLE_RECIPE_INGREDIENTS + " ("
            + COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_RI_RECIPE_ID + " INTEGER NOT NULL REFERENCES " + TABLE_RECIPES
            + "(" + COL_RECIPE_ID + ") ON DELETE CASCADE, "
            + COL_RI_NAME + " TEXT NOT NULL, "
            + COL_RI_QUANTITY + " REAL NOT NULL, "
            + COL_RI_UNIT + " TEXT NOT NULL)";

    private static volatile DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Returns the shared helper, created lazily with the application context to avoid leaks. */
    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(SQL_CREATE_PANTRY);
            db.execSQL(SQL_CREATE_RECIPES);
            db.execSQL(SQL_CREATE_RECIPE_INGREDIENTS);
            RecipeSeeder.seed(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ---- Pantry CRUD ----

    /** Inserts a pantry item and returns its new row id, or -1 on failure. */
    public long insertPantryItem(PantryItem item) {
        return getWritableDatabase().insert(TABLE_PANTRY, null, toValues(item));
    }

    /** Inserts several pantry items in one transaction, so either all or none are saved. */
    public void insertPantryItems(List<PantryItem> items) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (PantryItem item : items) {
                db.insertOrThrow(TABLE_PANTRY, null, toValues(item));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Returns every pantry item ordered by name, ignoring case. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_PANTRY, null, null, null, null, null,
                COL_PANTRY_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                items.add(readPantryItem(c));
            }
        }
        return items;
    }

    /** Returns the pantry item with the given id, or null if it does not exist. */
    public PantryItem getPantryItem(long id) {
        try (Cursor c = getReadableDatabase().query(TABLE_PANTRY, null,
                COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readPantryItem(c) : null;
        }
    }

    /** Updates an existing pantry item by id and returns the number of rows changed. */
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(TABLE_PANTRY, toValues(item),
                COL_PANTRY_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    /** Deletes the pantry item with the given id and returns the number of rows removed. */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(TABLE_PANTRY,
                COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /** Removes every pantry item. Recipes are left untouched. */
    public void clearPantry() {
        getWritableDatabase().delete(TABLE_PANTRY, null, null);
    }

    // ---- Recipe reads ----

    /** Returns all recipes, each with its ingredients, ordered by recipe name. */
    public List<Recipe> getAllRecipesWithIngredients() {
        SQLiteDatabase db = getReadableDatabase();
        Map<Long, Recipe> byId = new LinkedHashMap<>();
        try (Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null,
                COL_RECIPE_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                Recipe recipe = readRecipe(c);
                byId.put(recipe.getId(), recipe);
            }
        }
        // One query for all ingredients avoids an N+1 query per recipe.
        try (Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, null, null, null, null,
                COL_RI_ID + " ASC")) {
            while (c.moveToNext()) {
                RecipeIngredient ingredient = readIngredient(c);
                Recipe owner = byId.get(ingredient.getRecipeId());
                if (owner != null) {
                    owner.getIngredients().add(ingredient);
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    /** Returns one recipe with its ingredients, or null if the id is unknown. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] args = {String.valueOf(id)};
        Recipe recipe;
        try (Cursor c = db.query(TABLE_RECIPES, null, COL_RECIPE_ID + " = ?", args,
                null, null, null)) {
            if (!c.moveToFirst()) {
                return null;
            }
            recipe = readRecipe(c);
        }
        try (Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + " = ?", args,
                null, null, COL_RI_ID + " ASC")) {
            while (c.moveToNext()) {
                recipe.getIngredients().add(readIngredient(c));
            }
        }
        return recipe;
    }

    /** Returns the number of stored recipes. */
    public int getRecipeCount() {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_RECIPES, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    // ---- Row mapping ----

    private static ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, item.getName());
        values.put(COL_PANTRY_QUANTITY, item.getQuantity());
        values.put(COL_PANTRY_UNIT, item.getUnit());
        if (item.getExpiryDate() == null) {
            values.putNull(COL_PANTRY_EXPIRY);
        } else {
            values.put(COL_PANTRY_EXPIRY, item.getExpiryDate());
        }
        return values;
    }

    private static PantryItem readPantryItem(Cursor c) {
        int expiryIndex = c.getColumnIndexOrThrow(COL_PANTRY_EXPIRY);
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(COL_PANTRY_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_PANTRY_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_PANTRY_QUANTITY)),
                c.getString(c.getColumnIndexOrThrow(COL_PANTRY_UNIT)),
                c.isNull(expiryIndex) ? null : c.getString(expiryIndex));
    }

    private static Recipe readRecipe(Cursor c) {
        Recipe recipe = new Recipe();
        recipe.setId(c.getLong(c.getColumnIndexOrThrow(COL_RECIPE_ID)));
        recipe.setName(c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)));
        recipe.setSteps(c.getString(c.getColumnIndexOrThrow(COL_RECIPE_STEPS)));
        return recipe;
    }

    private static RecipeIngredient readIngredient(Cursor c) {
        return new RecipeIngredient(
                c.getLong(c.getColumnIndexOrThrow(COL_RI_ID)),
                c.getLong(c.getColumnIndexOrThrow(COL_RI_RECIPE_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_RI_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_RI_QUANTITY)),
                c.getString(c.getColumnIndexOrThrow(COL_RI_UNIT)));
    }
}
