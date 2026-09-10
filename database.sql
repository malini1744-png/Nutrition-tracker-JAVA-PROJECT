CREATE DATABASE nutrition_tracker;
USE nutrition_tracker;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    age INT,
    gender VARCHAR(20),
    height DECIMAL(5,2),
    weight DECIMAL(5,2),
    calorie_goal INT
);

CREATE TABLE foods (
    food_id INT PRIMARY KEY AUTO_INCREMENT,
    food_name VARCHAR(100) NOT NULL,
    calories DECIMAL(8,2),
    protein DECIMAL(8,2),
    carbs DECIMAL(8,2),
    fat DECIMAL(8,2)
);

CREATE TABLE meals (
    meal_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    meal_date DATE,
    meal_type VARCHAR(30),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE meal_foods (
    meal_food_id INT PRIMARY KEY AUTO_INCREMENT,
    meal_id INT,
    food_id INT,
    quantity DECIMAL(8,2),
    FOREIGN KEY (meal_id) REFERENCES meals(meal_id),
    FOREIGN KEY (food_id) REFERENCES foods(food_id)
);

INSERT INTO foods (food_name, calories, protein, carbs, fat)
VALUES
('Chicken Breast', 165, 31, 0, 3.6),
('Rice', 130, 2.7, 28, 0.3),
('Egg', 143, 12.6, 0.7, 9.5),
('Banana', 89, 1.1, 22.8, 0.3),
('Apple', 52, 0.3, 14, 0.2),
('Oats', 389, 16.9, 66.3, 6.9),
('Milk', 61, 3.2, 4.8, 3.3);