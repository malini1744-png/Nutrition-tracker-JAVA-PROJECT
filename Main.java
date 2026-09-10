import java.sql.*;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {

        Connection con = DBConnection.getConnection();
        Statement st = con.createStatement();

        while (true) {
            System.out.println("\n--- NUTRITION TRACKER ---");
            System.out.println("1. Create User Profile");
            System.out.println("2. Food Menu");
            System.out.println("3. Add Food");
            System.out.println("4. Log a Meal");
            System.out.println("5. View Today's Nutrition");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1) {

                try {
                    System.out.print("Name: ");
                    String name = sc.nextLine();

                    System.out.print("Age: ");
                    int age = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Gender: ");
                    String gender = sc.nextLine();

                    System.out.print("Height (cm): ");
                    double height = sc.nextDouble();

                    System.out.print("Weight (kg): ");
                    double weight = sc.nextDouble();

                    System.out.print("Daily calorie goal: ");
                    int goal = sc.nextInt();

                    st.executeUpdate("INSERT INTO users (name, age, gender, height, weight, calorie_goal) VALUES ('"
                            + name + "', " + age + ", '" + gender + "', " + height + ", " + weight + ", " + goal + ")");

                    System.out.println("User saved!");

                } catch (Exception e) {
                    System.out.println("Error adding user!");
                }

            } else if (choice == 2) {

                try {
                    ResultSet rs = st.executeQuery("SELECT food_id, food_name, calories, protein, carbs, fat FROM foods");

                    System.out.println("\n--- FOOD MENU ---");
                    while (rs.next()) {
                        System.out.println(rs.getInt("food_id") + ". " + rs.getString("food_name")
                                + " - Calories: " + rs.getDouble("calories")
                                + " Protein: " + rs.getDouble("protein") + "g"
                                + " Carbs: " + rs.getDouble("carbs") + "g"
                                + " Fat: " + rs.getDouble("fat") + "g");
                    }

                } catch (Exception e) {
                    System.out.println("Error viewing food menu!");
                }

            } else if (choice == 3) {

                try {
                    System.out.print("Food name: ");
                    String name = sc.nextLine();

                    System.out.print("Calories: ");
                    double calories = sc.nextDouble();

                    System.out.print("Protein (g): ");
                    double protein = sc.nextDouble();

                    System.out.print("Carbs (g): ");
                    double carbs = sc.nextDouble();

                    System.out.print("Fat (g): ");
                    double fat = sc.nextDouble();

                    st.executeUpdate("INSERT INTO foods (food_name, calories, protein, carbs, fat) VALUES ('"
                            + name + "', " + calories + ", " + protein + ", " + carbs + ", " + fat + ")");

                    System.out.println("Food added!");

                } catch (Exception e) {
                    System.out.println("Error adding food!");
                }

            } else if (choice == 4) {

                try {
                    System.out.println("1. Breakfast  2. Lunch  3. Dinner  4. Snack");
                    System.out.print("Choose meal type: ");
                    int mealChoice = sc.nextInt();

                    String mealType;
                    if (mealChoice == 1) {
                        mealType = "Breakfast";
                    } else if (mealChoice == 2) {
                        mealType = "Lunch";
                    } else if (mealChoice == 3) {
                        mealType = "Dinner";
                    } else if (mealChoice == 4) {
                        mealType = "Snack";
                    } else {
                        System.out.println("Invalid choice!");
                        continue;
                    }

                    st.executeUpdate("INSERT INTO meals (user_id, meal_date, meal_type) VALUES (1, CURDATE(), '" + mealType + "')");

                    ResultSet idRS = st.executeQuery("SELECT MAX(meal_id) FROM meals WHERE user_id = 1");
                    int mealId = 0;
                    if (idRS.next()) {
                        mealId = idRS.getInt(1);
                    }

                    ResultSet foodRS = st.executeQuery("SELECT food_id, food_name FROM foods");
                    System.out.println("Available Foods:");
                    while (foodRS.next()) {
                        System.out.println(foodRS.getInt("food_id") + ". " + foodRS.getString("food_name"));
                    }

                    System.out.print("Enter food ID: ");
                    int foodId = sc.nextInt();

                    System.out.print("Enter quantity: ");
                    double quantity = sc.nextDouble();

                    st.executeUpdate("INSERT INTO meal_foods (meal_id, food_id, quantity) VALUES ("
                            + mealId + ", " + foodId + ", " + quantity + ")");

                    System.out.println(mealType + " logged successfully!");

                } catch (Exception e) {
                    System.out.println("Error logging meal!");
                }

            } else if (choice == 5) {

                try {
                    ResultSet rs = st.executeQuery(
                            "SELECT SUM(f.calories * mf.quantity / 100), SUM(f.protein * mf.quantity / 100), "
                            + "SUM(f.carbs * mf.quantity / 100), SUM(f.fat * mf.quantity / 100) "
                            + "FROM meals m "
                            + "JOIN meal_foods mf ON m.meal_id = mf.meal_id "
                            + "JOIN foods f ON mf.food_id = f.food_id "
                            + "WHERE m.user_id = 1 AND m.meal_date = CURDATE()");

                    if (rs.next()) {
                        System.out.println("Calories: " + rs.getDouble(1));
                        System.out.println("Protein : " + rs.getDouble(2) + " g");
                        System.out.println("Carbs   : " + rs.getDouble(3) + " g");
                        System.out.println("Fat     : " + rs.getDouble(4) + " g");
                    }

                } catch (Exception e) {
                    System.out.println("Error viewing nutrition!");
                }

            } else if (choice == 6) {
                System.out.println("Goodbye!");
                break;

            } else {
                System.out.println("Invalid choice!");
            }
        }

        con.close();
    }
}