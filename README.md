# Nutrition Tracker

## About the Project

Nutrition Tracker is a Java-based application connected to a MySQL database.

The application helps users record their food and meals and track their daily nutrition.

It calculates:
- Calories
- Protein
- Carbohydrates
- Fat

## Technologies Used

- Java
- MySQL
- JDBC
- VS Code

## Main Features

### 1. Create User Profile

The user can create a profile by entering:

- Name
- Age
- Gender
- Height
- Weight
- Daily calorie goal

The information is stored in the MySQL database.

### 2. Food Menu

The Food Menu displays the available food items along with their nutritional information.

It shows:

- Calories
- Protein
- Carbohydrates
- Fat

The nutritional values are stored per 100 grams of food.

### 3. Add Food

The user can add a new food item to the database.

The user enters:

- Food name
- Calories
- Protein
- Carbohydrates
- Fat

The nutritional values should be entered per 100 grams.

### 4. Log a Meal

The user can select a meal type:

- Breakfast
- Lunch
- Dinner
- Snack

The user then selects a food item and enters the quantity in grams.

The meal and food information are stored in the database.

### 5. View Today's Nutrition

The application calculates the total nutrition consumed today.

It displays:

- Total calories
- Total protein
- Total carbohydrates
- Total fat

The nutrition is calculated according to the quantity of food entered.

For example:

If a food contains 165 calories per 100 grams and the user enters 200 grams:

165 × 200 / 100 = 330 calories

### 6. Exit

The user can exit the application using the Exit option.

## Database Tables

The project uses four MySQL tables:

### users

Stores user profile information.

### foods

Stores food items and their nutritional information.

### meals

Stores meal information such as meal type and date.

### meal_foods

Connects foods with meals and stores the quantity of food consumed.

## How It Works

The Java program connects to the MySQL database using JDBC.

The user selects an option from the main menu.

The program then performs the required operation using SQL queries.

The data is stored and retrieved from the MySQL database.

The nutrition calculation uses the food's nutritional values per 100 grams and the quantity entered by the user.

## Project Structure

```text
Nutrition-Tracker
│
├── src
│   ├── Main.java
│   └── DBConnection.java
│
├── lib
│   └── mysql-connector-j-26.7.0.jar
│
└── README.md