-- Seed-Daten für H2; läuft beim Start (Hibernate import.sql)

-- Bestehende Daten leeren (Reihenfolge wegen FK)
DELETE FROM DishIngredient;
DELETE FROM Dish;
DELETE FROM FoodItem;
DELETE FROM app_user;

-- IDs zurücksetzen
ALTER TABLE app_user ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE FoodItem ALTER COLUMN foodItemId RESTART WITH 1;
ALTER TABLE Dish ALTER COLUMN dishId RESTART WITH 1;
ALTER TABLE DishIngredient ALTER COLUMN dishIngredientId RESTART WITH 1;

-- Benutzer
INSERT INTO app_user (user_id, username, email, password_hash) VALUES
  (1, 'alice', 'alice@example.com', '$2a$10$demoAliceHash'),
  (2, 'bob', 'bob@example.com', '$2a$10$demoBobHash');

-- Lebensmittel
INSERT INTO FoodItem (foodItemId, name, brand, packSize, packPrice, proteinPer100g, carbsPer100g, fatPer100g, caloriesPer100g) VALUES
  (1, 'Chicken Breast', 'FreshFarm', 1000, 8.00, 31.0, 0.0, 3.6, 165.0),
  (2, 'White Rice', 'PantryCo', 1000, 2.50, 7.0, 78.0, 0.6, 360.0),
  (3, 'Broccoli', 'Greens', 500, 1.50, 2.8, 7.0, 0.4, 35.0),
  (4, 'Olive Oil', 'Mediterraneo', 500, 4.00, 0.0, 0.0, 100.0, 884.0);

-- category ist Enum-Ordinal (BREAKFAST=0, LUNCH=1, DINNER=2, SNACK=3, DESSERT=4, OTHER=5)
INSERT INTO Dish (dishId, userId, name, category, totalCost, totalProtein, totalCarbs, totalFat, totalCalories, servingWeight, preparationTime, imageUrl) VALUES
  (1, 1, 'Chicken Rice Bowl', 2, 2.32, 72.6, 163.0, 18.1, 1140.4, 490, 20, 'https://example.com/chicken-bowl.jpg');

-- Zutaten zum Gericht
INSERT INTO DishIngredient (dishIngredientId, dish_id, food_item_id, weight) VALUES
  (1, 1, 1, 180.0),
  (2, 1, 2, 200.0),
  (3, 1, 3, 100.0),
  (4, 1, 4, 10.0);

-- Identitäten nach Inserts fortsetzen
ALTER TABLE app_user ALTER COLUMN user_id RESTART WITH 3;
ALTER TABLE FoodItem ALTER COLUMN foodItemId RESTART WITH 5;
ALTER TABLE Dish ALTER COLUMN dishId RESTART WITH 2;
ALTER TABLE DishIngredient ALTER COLUMN dishIngredientId RESTART WITH 5;
