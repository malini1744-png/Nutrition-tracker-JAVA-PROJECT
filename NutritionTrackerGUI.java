import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class NutritionTrackerGUI extends JFrame {

    Connection con;
    Statement st;
    int currentUserId = 1;

    CardLayout cardLayout = new CardLayout();
    JPanel cards = new JPanel(cardLayout);

    public NutritionTrackerGUI() {
        setTitle("Nutrition Tracker");
        setSize(700, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not connect to database!");
        }

        cards.add(buildMenuPanel(), "menu");
        cards.add(buildCreateUserPanel(), "createUser");
        cards.add(buildViewUsersPanel(), "viewUsers");
        cards.add(buildChangeUserPanel(), "changeUser");
        cards.add(buildFoodMenuPanel(), "foodMenu");
        cards.add(buildAddFoodPanel(), "addFood");
        cards.add(buildLogMealPanel(), "logMeal");
        cards.add(buildNutritionPanel(), "nutrition");
        cards.add(buildAdvicePanel(), "advice");

        add(cards);
        cardLayout.show(cards, "menu");
    }

    private JButton navButton(String label, String targetCard, Runnable beforeShow) {
        JButton btn = new JButton(label);
        btn.addActionListener(e -> {
            if (beforeShow != null) beforeShow.run();
            cardLayout.show(cards, targetCard);
        });
        return btn;
    }

    private JButton backButton() {
        return navButton("Back to Menu", "menu", null);
    }

    private JPanel buildForm(java.util.LinkedHashMap<String, JTextField> fields) {
        JPanel form = new JPanel(new GridLayout(fields.size(), 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        for (var entry : fields.entrySet()) {
            form.add(new JLabel(entry.getKey() + ":"));
            form.add(entry.getValue());
        }
        return form;
    }

    
    private JPanel bottomRow(JButton... buttons) {
        JPanel bottom = new JPanel();
        for (JButton b : buttons) bottom.add(b);
        bottom.add(backButton());
        return bottom;
    }

    private JPanel screen(String titleText, JComponent center, JPanel bottom) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(titleText, SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    
    private void fillTable(DefaultTableModel model, String sql, String... columns) {
        try {
            model.setRowCount(0);
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Object[] row = new Object[columns.length];
                for (int i = 0; i < columns.length; i++) row[i] = rs.getObject(columns[i]);
                model.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data!");
        }
    }

    private void fillIdLabelList(DefaultListModel<String> model, String sql, String idCol, String labelCol) {
        try {
            model.clear();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                model.addElement(rs.getInt(idCol) + " - " + rs.getString(labelCol));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading list!");
        }
    }

    private void clearFields(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }

    // =========================
    // MAIN MENU
    // =========================
    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("NUTRITION TRACKER", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel buttonPanel = new JPanel(new GridLayout(9, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 30, 100));

        buttonPanel.add(navButton("Create User Profile", "createUser", null));
        buttonPanel.add(navButton("View Accounts", "viewUsers", this::refreshUserTable));
        buttonPanel.add(navButton("Change User", "changeUser", this::refreshChangeUserList));
        buttonPanel.add(navButton("Food Menu", "foodMenu", this::refreshFoodTable));
        buttonPanel.add(navButton("Add Food", "addFood", null));
        buttonPanel.add(navButton("Log a Meal", "logMeal", this::refreshMealFoodList));
        buttonPanel.add(navButton("View Today's Nutrition", "nutrition", this::loadNutritionTotals));
        buttonPanel.add(navButton("Daily Advice", "advice", this::loadAdvice));

        JButton btnExit = new JButton("Exit");
        btnExit.addActionListener(e -> System.exit(0));
        buttonPanel.add(btnExit);

        panel.add(title, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }

   
    private JPanel buildCreateUserPanel() {
        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField genderField = new JTextField();
        JTextField heightField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField goalField = new JTextField();

        var fields = new java.util.LinkedHashMap<String, JTextField>();
        fields.put("Name", nameField);
        fields.put("Age", ageField);
        fields.put("Gender", genderField);
        fields.put("Height (cm)", heightField);
        fields.put("Weight (kg)", weightField);
        fields.put("Daily Calorie Goal", goalField);

        JButton saveBtn = new JButton("Save User");
        saveBtn.addActionListener(e -> {
            try {
                st.executeUpdate("INSERT INTO users (name, age, gender, height, weight, calorie_goal) VALUES ('"
                        + nameField.getText() + "', " + Integer.parseInt(ageField.getText()) + ", '"
                        + genderField.getText() + "', " + Double.parseDouble(heightField.getText()) + ", "
                        + Double.parseDouble(weightField.getText()) + ", " + Integer.parseInt(goalField.getText()) + ")");
                JOptionPane.showMessageDialog(this, "User saved!");
                clearFields(nameField, ageField, genderField, heightField, weightField, goalField);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding user!");
            }
        });

        return screen("Create User Profile", buildForm(fields), bottomRow(saveBtn));
    }

    DefaultTableModel userTableModel;
    JTable userTable;

    private JPanel buildViewUsersPanel() {
        userTableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Age", "Gender", "Height", "Weight", "Calorie Goal"}, 0);
        userTable = new JTable(userTableModel);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshUserTable());

        return screen("Accounts", new JScrollPane(userTable), bottomRow(refreshBtn));
    }

    private void refreshUserTable() {
        fillTable(userTableModel,
                "SELECT user_id, name, age, gender, height, weight, calorie_goal FROM users",
                "user_id", "name", "age", "gender", "height", "weight", "calorie_goal");
    }

   
    DefaultListModel<String> changeUserListModel;
    JList<String> changeUserList;

    private JPanel buildChangeUserPanel() {
        changeUserListModel = new DefaultListModel<>();
        changeUserList = new JList<>(changeUserListModel);

        JButton selectBtn = new JButton("Set as Active User");
        selectBtn.addActionListener(e -> {
            String selected = changeUserList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a user first!");
                return;
            }
            currentUserId = Integer.parseInt(selected.split(" - ")[0]);
            JOptionPane.showMessageDialog(this, "Active user set to: " + selected);
            cardLayout.show(cards, "menu");
        });

        return screen("Change User", new JScrollPane(changeUserList), bottomRow(selectBtn));
    }

    private void refreshChangeUserList() {
        fillIdLabelList(changeUserListModel, "SELECT user_id, name FROM users", "user_id", "name");
    }

    DefaultTableModel foodTableModel;
    JTable foodTable;

    private JPanel buildFoodMenuPanel() {
        foodTableModel = new DefaultTableModel(
                new Object[]{"ID", "Food", "Calories", "Protein", "Carbs", "Fat"}, 0);
        foodTable = new JTable(foodTableModel);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshFoodTable());

        return screen("Food Menu", new JScrollPane(foodTable), bottomRow(refreshBtn));
    }

    private void refreshFoodTable() {
        fillTable(foodTableModel,
                "SELECT food_id, food_name, calories, protein, carbs, fat FROM foods",
                "food_id", "food_name", "calories", "protein", "carbs", "fat");
    }

 
    private JPanel buildAddFoodPanel() {
        JTextField nameField = new JTextField();
        JTextField calField = new JTextField();
        JTextField proteinField = new JTextField();
        JTextField carbField = new JTextField();
        JTextField fatField = new JTextField();

        var fields = new java.util.LinkedHashMap<String, JTextField>();
        fields.put("Food Name", nameField);
        fields.put("Calories", calField);
        fields.put("Protein (g)", proteinField);
        fields.put("Carbs (g)", carbField);
        fields.put("Fat (g)", fatField);

        JButton saveBtn = new JButton("Add Food");
        saveBtn.addActionListener(e -> {
            try {
                st.executeUpdate("INSERT INTO foods (food_name, calories, protein, carbs, fat) VALUES ('"
                        + nameField.getText() + "', " + Double.parseDouble(calField.getText()) + ", "
                        + Double.parseDouble(proteinField.getText()) + ", " + Double.parseDouble(carbField.getText())
                        + ", " + Double.parseDouble(fatField.getText()) + ")");
                JOptionPane.showMessageDialog(this, "Food added!");
                clearFields(nameField, calField, proteinField, carbField, fatField);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding food!");
            }
        });

        return screen("Add Food", buildForm(fields), bottomRow(saveBtn));
    }

    JComboBox<String> mealTypeBox;
    JList<String> mealFoodList;
    DefaultListModel<String> mealFoodListModel;
    JTextField quantityField;

    private JPanel buildLogMealPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel();

        mealTypeBox = new JComboBox<>(new String[]{"Breakfast", "Lunch", "Dinner", "Snack"});
        top.add(new JLabel("Meal Type:"));
        top.add(mealTypeBox);

        mealFoodListModel = new DefaultListModel<>();
        mealFoodList = new JList<>(mealFoodListModel);

        JPanel qtyPanel = new JPanel();
        quantityField = new JTextField(6);
        qtyPanel.add(new JLabel("Quantity:"));
        qtyPanel.add(quantityField);

        JButton logBtn = new JButton("Log Meal");
        logBtn.addActionListener(e -> logMeal());

        JPanel bottom = new JPanel();
        bottom.add(qtyPanel);
        bottom.add(logBtn);
        bottom.add(backButton());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(mealFoodList), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshMealFoodList() {
        fillIdLabelList(mealFoodListModel, "SELECT food_id, food_name FROM foods", "food_id", "food_name");
    }

    private void logMeal() {
        try {
            String mealType = (String) mealTypeBox.getSelectedItem();

            String selected = mealFoodList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a food first!");
                return;
            }
            int foodId = Integer.parseInt(selected.split(" - ")[0]);
            double quantity = Double.parseDouble(quantityField.getText());

            st.executeUpdate("INSERT INTO meals (user_id, meal_date, meal_type) VALUES ("
                    + currentUserId + ", CURDATE(), '" + mealType + "')");

            ResultSet idRS = st.executeQuery("SELECT MAX(meal_id) FROM meals WHERE user_id = " + currentUserId);
            int mealId = idRS.next() ? idRS.getInt(1) : 0;

            st.executeUpdate("INSERT INTO meal_foods (meal_id, food_id, quantity) VALUES ("
                    + mealId + ", " + foodId + ", " + quantity + ")");

            JOptionPane.showMessageDialog(this, mealType + " logged successfully!");
            quantityField.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error logging meal!");
        }
    }

    
    JLabel userLabel, calLabel, proteinLabel, carbLabel, fatLabel;

    private JPanel buildNutritionPanel() {
        userLabel = new JLabel("User: -", SwingConstants.CENTER);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        userLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel title = new JLabel("Today's Nutrition", SwingConstants.CENTER);

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(userLabel);
        topPanel.add(title);

        JPanel center = new JPanel(new GridLayout(4, 1, 10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        calLabel = new JLabel("Calories: -");
        proteinLabel = new JLabel("Protein: -");
        carbLabel = new JLabel("Carbs: -");
        fatLabel = new JLabel("Fat: -");

        Font big = new Font("SansSerif", Font.PLAIN, 18);
        for (JLabel l : new JLabel[]{calLabel, proteinLabel, carbLabel, fatLabel}) l.setFont(big);

        center.add(calLabel);
        center.add(proteinLabel);
        center.add(carbLabel);
        center.add(fatLabel);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadNutritionTotals());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(bottomRow(refreshBtn), BorderLayout.SOUTH);
        return panel;
    }

    private void loadNutritionTotals() {
        try {
            ResultSet userRS = st.executeQuery("SELECT name FROM users WHERE user_id = " + currentUserId);
            userLabel.setText(userRS.next() ? "User: " + userRS.getString("name") : "User: (no profile found)");

            ResultSet rs = st.executeQuery(
                    "SELECT SUM(f.calories * mf.quantity / 100), SUM(f.protein * mf.quantity / 100), "
                    + "SUM(f.carbs * mf.quantity / 100), SUM(f.fat * mf.quantity / 100) "
                    + "FROM meals m "
                    + "JOIN meal_foods mf ON m.meal_id = mf.meal_id "
                    + "JOIN foods f ON mf.food_id = f.food_id "
                    + "WHERE m.user_id = " + currentUserId + " AND m.meal_date = CURDATE()");

            if (rs.next()) {
                calLabel.setText("Calories: " + rs.getDouble(1));
                proteinLabel.setText("Protein: " + rs.getDouble(2) + " g");
                carbLabel.setText("Carbs: " + rs.getDouble(3) + " g");
                fatLabel.setText("Fat: " + rs.getDouble(4) + " g");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error viewing nutrition!");
        }
    }


    // =========================
    // DAILY ADVICE
    // =========================
    JLabel adviceUserLabel, calorieAdviceLabel, proteinAdviceLabel, generalAdviceLabel;

    private JPanel buildAdvicePanel() {
        adviceUserLabel = new JLabel("User: -", SwingConstants.CENTER);
        adviceUserLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        calorieAdviceLabel = new JLabel("Calories: -", SwingConstants.CENTER);
        proteinAdviceLabel = new JLabel("Protein: -", SwingConstants.CENTER);
        generalAdviceLabel = new JLabel("Stats: -", SwingConstants.CENTER);

        Font adviceFont = new Font("SansSerif", Font.PLAIN, 17);
        calorieAdviceLabel.setFont(adviceFont);
        proteinAdviceLabel.setFont(adviceFont);
        generalAdviceLabel.setFont(adviceFont);

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        center.add(calorieAdviceLabel);
        center.add(proteinAdviceLabel);
        center.add(generalAdviceLabel);

        JButton refreshBtn = new JButton("Refresh Advice");
        refreshBtn.addActionListener(e -> loadAdvice());

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(adviceUserLabel);
        top.add(new JLabel("Daily Stats", SwingConstants.CENTER));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(bottomRow(refreshBtn), BorderLayout.SOUTH);
        return panel;
    }

    private void loadAdvice() {
        try {
            // Get user's name, weight and daily calorie goal
            ResultSet userRS = st.executeQuery(
                    "SELECT name, weight, calorie_goal FROM users WHERE user_id = " + currentUserId);

            if (!userRS.next()) {
                adviceUserLabel.setText("User: (no profile found)");
                calorieAdviceLabel.setText("Calories: No user profile found");
                proteinAdviceLabel.setText("Protein: No user profile found");
                generalAdviceLabel.setText("Advice: Create a user profile first.");
                return;
            }

            String name = userRS.getString("name");
            double weight = userRS.getDouble("weight");
            double calorieGoal = userRS.getDouble("calorie_goal");

            adviceUserLabel.setText("User: " + name);

            // Get today's nutrition totals
            ResultSet rs = st.executeQuery(
                    "SELECT COALESCE(SUM(f.calories * mf.quantity / 100), 0), "
                    + "COALESCE(SUM(f.protein * mf.quantity / 100), 0) "
                    + "FROM meals m "
                    + "JOIN meal_foods mf ON m.meal_id = mf.meal_id "
                    + "JOIN foods f ON mf.food_id = f.food_id "
                    + "WHERE m.user_id = " + currentUserId + " AND m.meal_date = CURDATE()");

            if (rs.next()) {
                double calories = rs.getDouble(1);
                double protein = rs.getDouble(2);

                // Simple app rule: protein target = 0.8 g per kg body weight
                double proteinGoal = weight * 1.2;

                calorieAdviceLabel.setText(String.format(
                        "Calories: %.0f / %.0f kcal", calories, calorieGoal));
                proteinAdviceLabel.setText(String.format(
                        "Protein: %.1f / %.1f g", protein, proteinGoal));

                String calorieAdvice;
                if (calories < calorieGoal) {
                    calorieAdvice = String.format(
                            "You have %.0f kcal remaining today.", calorieGoal - calories);
                } else if (calories > calorieGoal) {
                    calorieAdvice = String.format(
                            "You are %.0f kcal above your daily goal.", calories - calorieGoal);
                } else {
                    calorieAdvice = "You have reached your daily calorie goal.";
                }

                String proteinAdvice;
                if (protein < proteinGoal) {
                    proteinAdvice = String.format(
                            "Protein is %.1f g below the ideal target. Consider a protein-rich food.",
                            proteinGoal - protein);
                } else {
                    proteinAdvice = "Your protein intake has reached the ideal target.";
                }

                generalAdviceLabel.setText(
                        "<html><center>" + calorieAdvice + "<br>" + proteinAdvice + "</center></html>");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading advice!");
        }
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NutritionTrackerGUI gui = new NutritionTrackerGUI();
            gui.setVisible(true);
        });
    }
}