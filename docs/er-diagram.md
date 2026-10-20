# Entity-relationship diagram

Database `smart_pantry.db`, version 1. `pantry_items` is independent of the recipe tables because pantry and recipe ingredients are linked by **normalised name** at matching time, not by a foreign key.

```mermaid
erDiagram
    RECIPES ||--o{ RECIPE_INGREDIENTS : "has"

    PANTRY_ITEMS {
        INTEGER id PK "AUTOINCREMENT"
        TEXT name "NOT NULL"
        REAL quantity "NOT NULL, CHECK > 0"
        TEXT unit "NOT NULL"
        TEXT expiry_date "yyyy-MM-dd, nullable"
    }

    RECIPES {
        INTEGER id PK "AUTOINCREMENT"
        TEXT name "NOT NULL, UNIQUE"
        TEXT steps "NOT NULL, newline-separated"
    }

    RECIPE_INGREDIENTS {
        INTEGER id PK "AUTOINCREMENT"
        INTEGER recipe_id FK "NOT NULL, ON DELETE CASCADE"
        TEXT name "NOT NULL"
        REAL quantity "NOT NULL"
        TEXT unit "NOT NULL"
    }
```
