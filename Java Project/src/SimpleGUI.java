
import java.awt.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import src.Transaction;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
// import org.json.JSONArray;
// import org.json.JSONObject;

import java.io.*;
import java.time.LocalDate;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;


public class SimpleGUI {
    
    private String loggedInUsername; 
    private JFrame loginFrame;
    private JFrame mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPanel depositPanel;
    private JPanel withdrawPanel;
    private JPanel mainContentPanel;
    private JLabel balanceLabel;
    private double accountBalance = 0.0;
    private Date date;
    private String type; // "Deposit" or "Withdraw"
    private double amount;
    

    private static final String DATABASE_FILE = "data/database.json";
    // private static final String TRANSACTIONS_FILE = "data/transactions.json";

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private List<Transaction> transactions = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    


    public SimpleGUI() {
        // loadUsers();
        List<User> users = loadUsers();

        createLoginWindow();
    }

    // private void loadUsers() {
    //     try (FileReader reader = new FileReader(DATABASE_FILE)) {
    //         User[] usersArray = gson.fromJson(reader, User[].class);
    //         users = usersArray != null ? new ArrayList<>(List.of(usersArray)) : new ArrayList<>();
    //         System.out.println("Users loaded successfully.");
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //         users = new ArrayList<>();
    //     }
    // }
    private void saveUsers() {
        try (FileWriter writer = new FileWriter(DATABASE_FILE)) {
            gson.toJson(users, writer);
            System.out.println("Users saved to JSON.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private User findUser(String username) {
        return users.stream().filter(user -> username.equals(user.getUsername())).findFirst().orElse(null);
    }
    private void addTransaction(String username, Transaction transaction) {
        User user = findUser(username);
        if (user != null) {
            user.getTransactions().add(transaction); // Add transaction to user's list
            saveUsers(); // Save all users back to the file
            System.out.println("Transaction added and saved.");
        } else {
            System.out.println("User not found, transaction not saved.");
        }
    }

    private void saveUser(User newUser) {
        List<User> users = loadUsers();  // Load existing users from database.json
        users.add(newUser);               // Add new user to the list
        saveUsers(users);                 // Save the updated user list
    }
    private void signup(String username, String fullname, String email, String dob, String gender, String phone,
    String street, String city, String state, String postal, String accountType,
    String idNumber, String idType, String password) {
        try {
            String hashedPassword = hashPassword(password);
            User newUser = new User(username, fullname, email ,dob, gender,phone, street, city, state, postal, accountType, idNumber, idType, password );
            // Add new user to database
            saveUser(newUser);
            System.out.println("User saved successfully.");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    
    // private List<User> loadUsers() {
    //     try (FileReader reader = new FileReader(DATABASE_FILE)) {
    //         User[] usersArray = gson.fromJson(reader, User[].class);
    //         return usersArray != null ? new ArrayList<>(List.of(usersArray)) : new ArrayList<>();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //         return new ArrayList<>();
    //     }
    // }

    private void saveTransactions() {
        try (FileWriter writer = new FileWriter(DATABASE_FILE)) {
            gson.toJson(transactions, writer);
            System.out.println("Transactions saved to JSON.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    

    //////////////////////////
    public void Transaction(Date date, String type, double amount) {
        this.date = date;
        this.type = type;
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }
    ///////////////////
    // private void saveUsers(List<User> users) {
    //     try (FileWriter writer = new FileWriter(DATABASE_FILE)) {
    //         gson.toJson(users, writer);
    //         System.out.println("Users saved to JSON.");  // Debug statement
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }


    private void createLoginWindow() {
        loginFrame = new JFrame("Banking System App");
        loginFrame.setSize(300, 200);
        loginFrame.setResizable(false);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);

        JButton signupButton = new JButton("Signup");
        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(signupButton);
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        loginFrame.setLayout(new BorderLayout());
        loginFrame.add(panel, BorderLayout.CENTER);
        loginFrame.add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> attemptLogin());
        cancelButton.addActionListener(e -> System.exit(0));
        signupButton.addActionListener(e -> openSignupWindow());

        loginFrame.setVisible(true);
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (authenticateUser(username, password)) {
            loggedInUsername = username;
            createMainWindow(); // Login successful
            loginFrame.dispose(); // Close login window
        } else {
            JOptionPane.showMessageDialog(loginFrame, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactions() {
        try (FileReader reader = new FileReader(DATABASE_FILE)) {
            Transaction[] transactionsArray = gson.fromJson(reader, Transaction[].class);
            transactions = transactionsArray != null ? new ArrayList<>(List.of(transactionsArray)) : new ArrayList<>();
            System.out.println("Transactions loaded from JSON.");
        } catch (IOException e) {
            e.printStackTrace();
            transactions = new ArrayList<>();
        }
    }

    private void createMainWindow() {
        mainFrame = new JFrame("Main Window");
        mainFrame.setSize(700, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu accountMenu = new JMenu("Account");
        accountMenu.add(new JMenuItem("View Customer Info"));
        accountMenu.add(new JMenuItem("Edit Customer Info"));
        accountMenu.add(new JMenuItem("View account details"));
        accountMenu.add(new JMenuItem("Close account"));

        JMenu bankTransferMenu = new JMenu("Bank Transfer");
        bankTransferMenu.add(new JMenuItem("Transfer money"));
        bankTransferMenu.add(new JMenuItem("View transfer history"));

        JMenu transactionsMenu = new JMenu("Transactions");
        JMenuItem viewTransactionsItem = new JMenuItem("View Transactions");
        viewTransactionsItem.addActionListener(e -> viewTransactions());
        transactionsMenu.add(viewTransactionsItem);

        JMenuItem newTransactionItem = new JMenuItem("New Transaction");
        newTransactionItem.addActionListener(e -> newTransaction());
        transactionsMenu.add(newTransactionItem);

        JMenuItem cancelTransactionItem = new JMenuItem("Cancel Transaction");
        cancelTransactionItem.addActionListener(e -> cancelTransaction());
        transactionsMenu.add(cancelTransactionItem);

        menuBar.add(accountMenu);
        menuBar.add(transactionsMenu);
        menuBar.add(bankTransferMenu);
        mainFrame.setJMenuBar(menuBar);

        JPanel balancePanel = new JPanel();
        balanceLabel = new JLabel("Balance: $" + accountBalance);
        balancePanel.add(balanceLabel);
        mainFrame.add(balancePanel, BorderLayout.NORTH);

        mainContentPanel = new JPanel(new CardLayout());
        mainFrame.add(mainContentPanel, BorderLayout.CENTER);

        createDepositPanel();
        createWithdrawPanel();

        JPanel blankPanel = new JPanel();
        mainContentPanel.add(blankPanel, "BlankPanel");
        CardLayout cl = (CardLayout) (mainContentPanel.getLayout());
        cl.show(mainContentPanel, "BlankPanel");

        mainFrame.setVisible(true);
        viewTransactionsItem.addActionListener(e -> viewTransactions());
    }

    private void createDepositPanel() {
        depositPanel = new JPanel();
        depositPanel.setLayout(new GridLayout(3, 2, 5, 5));
        depositPanel.setBorder(BorderFactory.createTitledBorder("Deposit"));

        depositPanel.add(new JLabel("Enter amount to deposit:"));
        JTextField depositField = new JTextField(20);
        depositPanel.add(depositField);

        JButton depositButton = new JButton("Deposit");
        depositPanel.add(depositButton);
        JButton cancelButton = new JButton("Cancel");
        depositPanel.add(cancelButton);

        depositButton.addActionListener(e -> {
            String amountText = depositField.getText().trim();
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(mainFrame, "Please enter a positive amount.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                accountBalance += amount; // Update the account balance
                balanceLabel.setText("Balance: $" + accountBalance); // Update the balance label
                JOptionPane.showMessageDialog(mainFrame, "Deposited: $" + amount, "Success", JOptionPane.INFORMATION_MESSAGE);
                depositField.setText(""); // Clear the field after deposit
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid amount. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) (mainContentPanel.getLayout());
            cl.show(mainContentPanel, "BlankPanel");
        });

        mainContentPanel.add(depositPanel, "DepositPanel"); // Add deposit panel to main content
        depositPanel.setVisible(false); // Initially hide the deposit panel
    }

    private void createWithdrawPanel() {
        withdrawPanel = new JPanel();
        withdrawPanel.setLayout(new GridLayout(3, 2, 5, 5));
        withdrawPanel.setBorder(BorderFactory.createTitledBorder("Withdraw"));

        withdrawPanel.add(new JLabel("Enter amount to withdraw:"));
        JTextField withdrawField = new JTextField(20);
        withdrawPanel.add(withdrawField);

        JButton withdrawButton = new JButton("Withdraw");
        withdrawPanel.add(withdrawButton);
        JButton cancelButton = new JButton("Cancel");
        withdrawPanel.add(cancelButton);

        withdrawButton.addActionListener(e -> {
            String amountText = withdrawField.getText().trim();
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(mainFrame, "Please enter a positive amount.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (amount > accountBalance) {
                    JOptionPane.showMessageDialog(mainFrame, "Insufficient funds. Please enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    accountBalance -= amount; // Update the account balance
                    balanceLabel.setText("Balance: $" + accountBalance); // Update the balance label
                    JOptionPane.showMessageDialog(mainFrame, "Withdrawn: $" + amount, "Success", JOptionPane.INFORMATION_MESSAGE);
                    withdrawField.setText(""); // Clear the field after withdrawal
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid amount. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) (mainContentPanel.getLayout());
            cl.show(mainContentPanel, "BlankPanel"); // Show blank panel
        });

        mainContentPanel.add(withdrawPanel, "WithdrawPanel"); // Add withdraw panel to main content
        withdrawPanel.setVisible(false); // Initially hide the withdraw panel
    }















    private void openSignupWindow() {


// JDialog signupFrame = new JDialog(mainFrame, "Signup", true);
// signupFrame.setSize(700, 600);
// signupFrame.setLocationRelativeTo(mainFrame);

// JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

// // All signup fields
// JTextField usernameField = new JTextField(20);
// JTextField fullnameField = new JTextField(20);
// JTextField emailField = new JTextField(20);
// JTextField dobField = new JTextField(20);
// JComboBox<String> genderField = new JComboBox<>(new String[]{"Male", "Female", "Other"});
// JTextField phoneField = new JTextField(20);
// JTextField streetField = new JTextField(20);

// // Add a JComboBox for the city selection with French cities only
// JComboBox<String> cityField = new JComboBox<>(new String[]{
//     "Paris", "Marseille", "Lyon", "Toulouse", "Nice", "Nantes", "Strasbourg", "Montpellier",
//     "Bordeaux", "Lille", "Rennes", "Reims", "Le Havre", "Saint-Étienne", "Toulon", "Grenoble",
//     "Dijon", "Angers", "Nîmes", "Villeurbanne", "Clermont-Ferrand", "Le Mans", "Aix-en-Provence",
//     "Brest", "Tours", "Amiens", "Limoges", "Annecy", "Boulogne-Billancourt", "Perpignan"
// });

// JTextField stateField = new JTextField(20);
// // JTextField countryField = new JTextField(20);
// JTextField postalField = new JTextField(20);
// JComboBox<String> accountTypeField = new JComboBox<>(new String[]{"Savings", "Checking", "Business"});
// JTextField idNumberField = new JTextField(20);
// JComboBox<String> idTypeField = new JComboBox<>(new String[]{"Driver’s License", "Passport", "National ID"});
// JPasswordField passwordField = new JPasswordField(20);
// JPasswordField confirmPasswordField = new JPasswordField(20);
// JCheckBox termsCheckbox = new JCheckBox("I agree to the Terms and Conditions");

// // Add components to the panel
// panel.add(new JLabel("Username:"));
// panel.add(usernameField);
// panel.add(new JLabel("Full Name:"));
// panel.add(fullnameField);
// panel.add(new JLabel("Email:"));
// panel.add(emailField);
// panel.add(new JLabel("Date of Birth:"));
// panel.add(dobField);
// panel.add(new JLabel("Gender:"));
// panel.add(genderField);
// panel.add(new JLabel("Phone Number:"));
// panel.add(phoneField);
// panel.add(new JLabel("Street Address:"));
// panel.add(streetField);
// panel.add(new JLabel("City:"));
// panel.add(cityField); // Use the city dropdown here
// panel.add(new JLabel("State/Province/Region:"));
// panel.add(stateField);
// // panel.add(new JLabel("Country:"));
// // panel.add(countryField);
// panel.add(new JLabel("Postal/ZIP Code:"));
// panel.add(postalField);
// panel.add(new JLabel("Account Type:"));
// panel.add(accountTypeField);
// panel.add(new JLabel("ID Number:"));
// panel.add(idNumberField);
// panel.add(new JLabel("Type of ID Document:"));
// panel.add(idTypeField);
// panel.add(new JLabel("Password:"));
// panel.add(passwordField);
// panel.add(new JLabel("Confirm Password:"));
// panel.add(confirmPasswordField);
// panel.add(termsCheckbox);
// panel.add(new JLabel("")); // For layout alignment

// JButton createAccountButton = new JButton("Create Account");
// createAccountButton.addActionListener(e -> {
//     // Collect field data
//     String username = usernameField.getText().trim();
//     String fullname = fullnameField.getText().trim();
//     String email = emailField.getText().trim();
//     String dobText = dobField.getText().trim();
//     String gender = (String) genderField.getSelectedItem();
//     String phone = phoneField.getText().trim();
//     String street = streetField.getText().trim();
//     String city = (String) cityField.getSelectedItem(); // Get the selected city
//     String state = stateField.getText().trim();
//     // String country = countryField.getText().trim();
//     String postal = postalField.getText().trim();
//     String accountType = (String) accountTypeField.getSelectedItem();
//     String idNumber = idNumberField.getText().trim();
//     String idType = (String) idTypeField.getSelectedItem();
//     String password = new String(passwordField.getPassword());
//     String confirmPassword = new String(confirmPasswordField.getPassword());    
//       if (!isValidDateOfBirth(dobText)) {
//         JOptionPane.showMessageDialog(signupFrame, "Invalid Date of Birth. You must be at least 18 years old and use the format DD/MM/YYYY.", "Error", JOptionPane.ERROR_MESSAGE);
//         return;
//     }
    

//     if (!termsCheckbox.isSelected()) {
//         JOptionPane.showMessageDialog(signupFrame, "You must agree to the Terms.", "Error", JOptionPane.ERROR_MESSAGE);
//         return;
//     }

//     if (username.isEmpty() || fullname.isEmpty() || email.isEmpty() || dobText.isEmpty() ||
//         phone.isEmpty() || street.isEmpty() || city.isEmpty() || state.isEmpty() || /*country.isEmpty() ||*/
//         postal.isEmpty() || idNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
//         JOptionPane.showMessageDialog(signupFrame, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
//         return;
//     }

//     if (!password.equals(confirmPassword)) {
//         JOptionPane.showMessageDialog(signupFrame, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
//         return;
//     }
//     if (!isValidPhoneNumber(phone)) {
//         JOptionPane.showMessageDialog(signupFrame, "Invalid phone number.", "Error", JOptionPane.ERROR_MESSAGE);
//         return;
//     }
//     if (!isValidEmail(email)) {
//         JOptionPane.showMessageDialog(signupFrame, "Invalid email address. Allowed domains are @gmail.com, @outlook.com, @icloud.com, and @yahoo.com.", "Error", JOptionPane.ERROR_MESSAGE);
//         return;
//     }
    

//     // Add Date of Birth validation here, if needed
//     LocalDate dob;
//     try {
//         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//         dob = LocalDate.parse(dobText, formatter);
//     } catch (DateTimeParseException ex) {
//         JOptionPane.showMessageDialog(signupFrame, "Invalid Date of Birth format. Use DD/MM/YYYY.", "Error", JOptionPane.ERROR_MESSAGE);
//         return;
//     }

//     // Save all fields to JSON
//     List<User> users = loadUsers();
//     // users.add(new User(username, fullname, email, dob.toString(), gender, city, state, zipCode, idNumber, password));
//     users.add(new User(username, fullname, email, dob.toString(), gender, phone, street, city, state/*, country */, postal, accountType, idNumber, idType, password));

//     saveUsers(users);

//     JOptionPane.showMessageDialog(signupFrame, "Account created successfully!");
//     signupFrame.dispose();
// });

// panel.add(createAccountButton);
// signupFrame.add(panel);
// signupFrame.setVisible(true);
// }

    JDialog signupFrame = new JDialog(mainFrame, "Signup", true);
    signupFrame.setSize(700, 600);
    signupFrame.setLocationRelativeTo(mainFrame);

    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
    
    // All signup fields
    JTextField usernameField = new JTextField(20);
    JTextField fullnameField = new JTextField(20);
    JTextField emailField = new JTextField(20);
    JTextField dobField = new JTextField(20);
    JComboBox<String> genderField = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    JTextField phoneField = new JTextField(20);
    JTextField streetField = new JTextField(20);
    JComboBox<String> cityField = new JComboBox<>(new String[]{
        "Paris", "Marseille", "Lyon", "Toulouse", "Nice", "Nantes", "Strasbourg", "Montpellier",
        "Bordeaux", "Lille", "Rennes", "Reims", "Le Havre", "Saint-Étienne", "Toulon", "Grenoble",
        "Dijon", "Angers", "Nîmes", "Villeurbanne", "Clermont-Ferrand", "Le Mans", "Aix-en-Provence",
        "Brest", "Tours", "Amiens", "Limoges", "Annecy", "Boulogne-Billancourt", "Perpignan"
    });
    JTextField stateField = new JTextField(20);
    JTextField postalField = new JTextField(20);
    JComboBox<String> accountTypeField = new JComboBox<>(new String[]{"Savings", "Checking", "Business"});
    JTextField idNumberField = new JTextField(20);
    JComboBox<String> idTypeField = new JComboBox<>(new String[]{"Driver’s License", "Passport", "National ID"});
    JPasswordField passwordField = new JPasswordField(20);
    JPasswordField confirmPasswordField = new JPasswordField(20);
    JCheckBox termsCheckbox = new JCheckBox("I agree to the Terms and Conditions");

    // Add components to the panel
    panel.add(new JLabel("Username:"));
    panel.add(usernameField);
    panel.add(new JLabel("Full Name:"));
    panel.add(fullnameField);
    panel.add(new JLabel("Email:"));
    panel.add(emailField);
    panel.add(new JLabel("Date of Birth:"));
    panel.add(dobField);
    panel.add(new JLabel("Gender:"));
    panel.add(genderField);
    panel.add(new JLabel("Phone Number:"));
    panel.add(phoneField);
    panel.add(new JLabel("Street Address:"));
    panel.add(streetField);
    panel.add(new JLabel("City:"));
    panel.add(cityField);
    panel.add(new JLabel("State/Province/Region:"));
    panel.add(stateField);
    panel.add(new JLabel("Postal/ZIP Code:"));
    panel.add(postalField);
    panel.add(new JLabel("Account Type:"));
    panel.add(accountTypeField);
    panel.add(new JLabel("ID Number:"));
    panel.add(idNumberField);
    panel.add(new JLabel("Type of ID Document:"));
    panel.add(idTypeField);
    panel.add(new JLabel("Password:"));
    panel.add(passwordField);
    panel.add(new JLabel("Confirm Password:"));
    panel.add(confirmPasswordField);
    panel.add(termsCheckbox);
    panel.add(new JLabel("")); // For layout alignment

    JButton createAccountButton = new JButton("Create Account");
    createAccountButton.addActionListener(e -> {
        String username = usernameField.getText().trim();
        String fullname = fullnameField.getText().trim();
        String email = emailField.getText().trim();
        String dobText = dobField.getText().trim();
        String gender = (String) genderField.getSelectedItem();
        String phone = phoneField.getText().trim();
        String street = streetField.getText().trim();
        String city = (String) cityField.getSelectedItem();
        String state = stateField.getText().trim();
        String postal = postalField.getText().trim();
        String accountType = (String) accountTypeField.getSelectedItem();
        String idNumber = idNumberField.getText().trim();
        String idType = (String) idTypeField.getSelectedItem();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        signup(username, fullname, email, dobText, gender, phone, street, city, state, postal, accountType, idNumber, idType, password);
        JOptionPane.showMessageDialog(signupFrame, "Account created successfully!");
        signupFrame.dispose(); // Close the signup frame

        if (!termsCheckbox.isSelected()) {
            JOptionPane.showMessageDialog(signupFrame, "You must agree to the Terms.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.isEmpty() || fullname.isEmpty() || email.isEmpty() || dobText.isEmpty() ||
            phone.isEmpty() || street.isEmpty() || city.isEmpty() || state.isEmpty() ||
            postal.isEmpty() || idNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(signupFrame, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(signupFrame, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            password = hashPassword(password); // Hash the password before storing
        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(signupFrame, "Error hashing password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<User> users = loadUsers();
        users.add(new User(username, fullname, email, dobText, gender, phone, street, city, state, postal, accountType, idNumber, idType, password));
        saveUsers(users);

        JOptionPane.showMessageDialog(signupFrame, "Account created successfully!");
        signupFrame.dispose();
    });

    panel.add(createAccountButton);
    signupFrame.add(panel);
    signupFrame.setVisible(true);
}























// private List<User> loadUsers() {
//     try (FileReader reader = new FileReader(DATABASE_FILE)) {
//         User[] usersArray = gson.fromJson(reader, User[].class);
//         return usersArray != null ? new ArrayList<>(List.of(usersArray)) : new ArrayList<>();
//     } catch (IOException e) {
//         e.printStackTrace();
//         return new ArrayList<>();
//     }
// }
// //////////////////////////////////////
// private void loadUsers() {
//     try (FileReader reader = new FileReader(DATABASE_FILE)) {
//         User[] usersArray = gson.fromJson(reader, User[].class);
//         if (usersArray != null) {
//             users = new ArrayList<>(List.of(usersArray));
//         }
//         System.out.println("Users loaded successfully.");
//     } catch (IOException e) {
//         e.printStackTrace();
//         users = new ArrayList<>(); // Initialize empty if loading fails
//     }
// }
private List<User> loadUsers() {
    // List<User> users = new ArrayList<>();
    // try (FileReader reader = new FileReader(DATABASE_FILE)) {
    //     // Deserialize JSON to User array
    //     User[] usersArray = gson.fromJson(reader, User[].class);
    //     if (usersArray != null) {
    //         users = new ArrayList<>(List.of(usersArray)); // Set users to list of usersArray
    //     }
    //     System.out.println("Users successfully loaded from the database.");
    // } catch (IOException e) {
    //     System.out.println("Error reading from the database file: " + e.getMessage());
    // } catch (Exception e) {
    //     System.out.println("Error parsing JSON data: " + e.getMessage());
    // }
    // return users; // Ensure the list is returned
    try (FileReader reader = new FileReader(DATABASE_FILE)) {
        User[] usersArray = gson.fromJson(reader, User[].class);
        return usersArray != null ? new ArrayList<>(List.of(usersArray)) : new ArrayList<>();
    } catch (IOException e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}
//////////////////////////////////////////////////
// private List<User> loadUsers() {
// try (FileReader reader = new FileReader(DATABASE_FILE)) {
//     User[] usersArray = gson.fromJson(reader, User[].class);
//     return usersArray != null ? new ArrayList<>(List.of(usersArray)) : new ArrayList<>();
// } catch (IOException e) {
//     e.printStackTrace();
//     return new ArrayList<>();
// }
// }

private void saveUsers(List<User> users) {
try (FileWriter writer = new FileWriter(DATABASE_FILE)) {
    gson.toJson(users, writer);
} catch (IOException e) {
    e.printStackTrace();
}
}
private void saveUserTransaction(String username, Transaction transaction) {
    users = loadUsers(); // Load current users
    for (User user : users) {
        if (user.getUsername().equals(username)) {
            user.addTransaction(transaction); // Add the new transaction to the user's list
            break;
        }
    }
    saveUsers(users); // Save updated user data to JSON
}

public static void main(String[] args) {
SwingUtilities.invokeLater(SimpleGUI::new);
}
    









    /* 
    private boolean authenticateUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CREDENTIALS_FILE))) {
            String line;
            String hashedPassword = hashPassword(password);
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(":");
                if (credentials[0].equals(username) && credentials[1].equals(hashedPassword)) {
                    return true;
                }
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }
*/
    //function for phone number validation
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("06\\d{8}"); // Matches exactly 10 digits
    }

    
    //function for date of birth validation
    private boolean isValidEmail(String email) {
        // Regular expression pattern for allowed email domains
        String emailPattern = "^[\\w\\.-]+@(gmail|outlook|icloud|yahoo)\\.com$";
        return email.matches(emailPattern);
    }

    private boolean isValidDateOfBirth(String dob) {
        // Define the expected date format
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    try {
        // Parse the date of birth string into a LocalDate object
        LocalDate birthDate = LocalDate.parse(dob, formatter);
        LocalDate today = LocalDate.now();
        
        // Calculate the age
        Period age = Period.between(birthDate, today);
        
        // Check if the age is at least 18 years
        return age.getYears() >= 18;
        
    } catch (DateTimeParseException e) {
        // If parsing fails, return false (invalid date format)
        return false;
    }
    }

    /* 
    private boolean isValidDateOfBirth(String dob) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        try {
            LocalDate date = LocalDate.parse(dob, formatter);
            LocalDate today = LocalDate.now();
            Period age = Period.between(date, today);
            return age.getYears() >= 18; // Ensure the user is at least 18 years old
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    */
    private boolean authenticateUser(String username, String password) {
    //     List<User> users = loadUsers();
    //     String hashedPassword;
    //     try {
    //         hashedPassword = hashPassword(password);
    //     } catch (NoSuchAlgorithmException e) {
    //         e.printStackTrace();
    //         return false;
    //     }
    //     // System.out.println("Loaded users: " + users);
    //     System.out.println("Entered username: " + username);
    // System.out.println("Entered password (hashed): " + hashedPassword);

    //     // for (User user : users) {
    //     //     System.out.println("Stored user: " + user.getFullname() + ", Hashed password: " + user.getPassword()); // Debug statement
    //     //         // Check if fullname is not null before calling equals
    //     //     if (user.getFullname() != null && user.getFullname().equals(username) && user.getPassword().equals(hashedPassword)) {
    //     //         return true; // Successful authentication
    //     //     }
    //     // }
    //     for (User user : users) {
    //         // Ensure fullname and password are non-null before comparing
    //         if (user.getFullname() != null && user.getPassword() != null) {
    //             // System.out.println("Stored user: " + user.getFullname() + ", Hashed password: " + user.getPassword());
    //             System.out.println("Checking stored user: " + user.getFullname() + ", stored hashed password: " + user.getPassword());

    //             if (user.getFullname().equals(username) && user.getPassword().equals(hashedPassword)) {
    //                 System.out.println("Match found for user: " + user.getFullname());

    //                 return true; // Successful authentication
    //             }
    //         } else {
    //             System.out.println("Skipping user with incomplete details.");
    //         }
    //     }
    //     System.out.println("No matching user found. Authentication failed.");

    //     return false; // Authentication failed

    /*User user = findUser(username);
        if (user != null) {
            try {
                String hashedPassword = hashPassword(password);
                return hashedPassword.equals(user.getPassword());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return false;
    }*/
    List<User> users = loadUsers();
    String hashedPassword;
    try {
        hashedPassword = hashPassword(password);
    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        return false;
    }

    System.out.println("Loaded users: " + users);

    for (User user : users) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            System.out.println("Skipping user with incomplete details.");
            continue;
        }

        System.out.println("Checking stored user: " + user.getUsername() + ", stored hashed password: " + user.getPassword());

        if (user.getUsername().equals(username) && user.getPassword().equals(hashedPassword)) {
            return true; // Successful authentication
        }
    }
    return false; }// Authentication failed}
            /*if (user.getFullname().equals(fullname) && user.getPassword().equals(hashedPassword)) {
                return true;
            }
        }
        return false;
    }*/
    


    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    ////////////
    // private void hashPlaintextPasswords() {
    //     List<User> users = loadUsers(); // Load users from database
    
    //     boolean modified = false;
    //     for (User user : users) {
    //         String password = user.getPassword();
    //         if (!password.matches("[a-fA-F0-9]{64}")) { // Check if password is already hashed
    //             try {
    //                 String hashedPassword = hashPassword(password); // Hash plaintext password
    //                 user.setPassword(hashedPassword); // Update user object with hashed password
    //                 modified = true;
    //             } catch (NoSuchAlgorithmException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
    
    //     if (modified) {
    //         saveUsers(users); // Save the updated user list with hashed passwords
    //         System.out.println("All plaintext passwords have been hashed.");
    //     }
    // }
    
    /// ////////////
    private void viewTransactions() {
        // Create a dialog to display transactions
        JDialog transactionsDialog = new JDialog(mainFrame, "Transaction History", true);
        transactionsDialog.setSize(500, 400);
        transactionsDialog.setLocationRelativeTo(mainFrame);
    
        // Table to display transactions
        String[] columnNames = {"Date", "Type", "Amount"};
        Object[][] data = new Object[transactions.size()][3];
    
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            data[i][0] = t.getDate().toString();
            data[i][1] = t.getType();
            data[i][2] = String.format("$%.2f", t.getAmount());
        }
    
        JTable transactionsTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        transactionsDialog.add(scrollPane);
    
        transactionsDialog.setVisible(true);
    }
    


    private void newTransaction() {
        // Create a dialog for selecting transaction type
        JDialog transactionDialog = new JDialog(mainFrame, "New Transaction", true);
        transactionDialog.setSize(300, 200);
        transactionDialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Transaction type selection
        JLabel typeLabel = new JLabel("Select Transaction Type:");
        String[] transactionTypes = {"Deposit", "Withdraw"};
        JComboBox<String> transactionTypeCombo = new JComboBox<>(transactionTypes);
        
        // Amount input
        JLabel amountLabel = new JLabel("Enter Amount:");
        JTextField amountField = new JTextField(20);
        
        panel.add(typeLabel);
        panel.add(transactionTypeCombo);
        panel.add(amountLabel);
        panel.add(amountField);
        
        // Buttons
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");
        
        confirmButton.addActionListener(e -> {
            String selectedType = (String) transactionTypeCombo.getSelectedItem();
            String amountText = amountField.getText().trim();
            
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(transactionDialog, "Please enter a positive amount.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (selectedType.equals("Deposit")) {
                    // Call the existing deposit method
                    this.deposit(loggedInUsername, amount);
                } else if (selectedType.equals("Withdraw")) {
                    // Call the existing withdraw method
                    this.withdraw(loggedInUsername, amount);
                }
                balanceLabel.setText("Balance: $" + accountBalance); // Update balance label
                amountField.setText(""); // Clear the input field
                transactionDialog.dispose(); // Close the dialog after transaction
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(transactionDialog, "Invalid amount. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> transactionDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        transactionDialog.setLayout(new BorderLayout());
        transactionDialog.add(panel, BorderLayout.CENTER);
        transactionDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        transactionDialog.setVisible(true);
    }


    private void deposit(String username, double amount) {
        // loggedInUser.setBalance(loggedInUser.getBalance() + amount); // Update in-memory balance
        // saveTransactions(loggedInUser); // Save updated balance to file
        // JOptionPane.showMessageDialog(mainFrame, "Deposited: $" + amount, "Success", JOptionPane.INFORMATION_MESSAGE);        
        accountBalance += amount;
    balanceLabel.setText("Balance: $" + accountBalance);
    Transaction transaction = new Transaction(new Date(), "Deposit", amount);
    addTransaction(username, transaction);
    JOptionPane.showMessageDialog(mainFrame, "Deposited: $" + amount, "Success", JOptionPane.INFORMATION_MESSAGE);
    JOptionPane.showMessageDialog(mainFrame, "Deposited: $" + amount, "Success", JOptionPane.INFORMATION_MESSAGE);
    transactions.add(transaction);
    saveUserTransaction(username, transaction);
    }

    private void recordTransaction(User user, String type, double amount) {
        Transaction transaction = new Transaction(new Date(), type, amount);
        user.addTransaction(transaction);  // Add transaction to user's list
        saveUsers(users);  // Save all users to file, with updated transactions
    }
    


        // Modify withdraw method
    private void withdraw(String username, double amount) {

    if (amount > accountBalance) {
        JOptionPane.showMessageDialog(mainFrame, "Insufficient funds.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    } else {
        accountBalance -= amount;
        balanceLabel.setText("Balance: $" + accountBalance);
        Transaction transaction = new Transaction(new Date(), "Withdraw", amount);
        addTransaction(username, transaction);
        JOptionPane.showMessageDialog(mainFrame, "Withdrawn: $" + amount, "Success", JOptionPane.INFORMATION_MESSAGE);

        // Record and save the transaction
        transactions.add(transaction);
        saveUserTransaction(username, transaction);
    }
    // if (loggedInUser.getBalance() >= amount) {
    //     loggedInUser.setBalance(loggedInUser.getBalance() - amount); // Update in-memory balance
    //     saveTransactions(loggedInUser); // Save updated balance to file
    //     JOptionPane.showMessageDialog(mainFrame, "Withdrawn: $" + amount, "Success", JOptionPane.INFORMATION_MESSAGE);
    // } else {
    //     JOptionPane.showMessageDialog(mainFrame, "Insufficient funds.", "Error", JOptionPane.ERROR_MESSAGE);
    // }
}
    





private void signupUser(User newUser) {
    try {
        String hashedPassword = hashPassword(newUser.getPassword());
        newUser.setPassword(hashedPassword);
        users.add(newUser); // Add user to list
        saveUsers(); // Save to file
        System.out.println("User signed up and saved.");
    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(mainFrame, "Error during signup.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}












    /* 
    private void deposit(double amount) {
        accountBalance += amount; // Update the account balance
        JOptionPane.showMessageDialog(mainFrame, "Deposited: $" + amount, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to handle withdrawal
    private void withdraw(double amount) {
        if (amount > accountBalance) {
            JOptionPane.showMessageDialog(mainFrame, "Insufficient funds. Please enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            accountBalance -= amount; // Update the account balance
            JOptionPane.showMessageDialog(mainFrame, "Withdrawn: $" + amount, "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    
    }
*/
    private void cancelTransaction() {
        // Code to cancel a transaction
    }
}
class User {

    private String username;
    private String fullname;
    private String email;
    private String dob;
    private String gender;
    private String phone;
    private String street;
    private String city;
    private String state;
    private double balance;
    // private String country;
    private String postal;
    private String accountType;
    private String idNumber;
    private String idType;
    private String password;
    private List<Transaction> transactions; // Add a transactions list


    // public User(String fullname, String username, String Dob,String gender, String phoneNumber, String streetAdress,String city, String state, String zipCode,String idNumber, String password, String email) {
    //     this.fullname = fullname;
    //     this.username = username ;
    //     this.Dob = Dob;
    //     this.gender = gender ;
    //     this.phoneNumber = phoneNumber ;
    //     this.streetAdress = streetAdress ;
    //     this.city = city ;
    //     this.state = state ;
    //     this.zipCode = zipCode ;
    //     this.idNumber = idNumber ;
    //     this.password = password;
    //     this.email = email;
    // }

    public User(String username, String fullname, String email, String dob, String gender, String phone,
    String street, String city, String state, String postal, String accountType,
    String idNumber, String idType, String password) {
        /*, String country,   */
    this.username = username;
    this.fullname = fullname;
    this.email = email;
    this.dob = dob;
    this.gender = gender;
    this.phone = phone;
    this.street = street;
    this.city = city;
    this.state = state;
    // this.country = country;
    this.postal = postal;
    this.accountType = accountType;
    this.idNumber = idNumber;
    this.idType = idType;
    this.password = password;
    // this.balance = balance;
    this.transactions = new ArrayList<>();
    }

    // public List<Transaction> getTransactions() { return transactions; }
    public void addTransaction(Transaction transaction) { this.transactions.add(transaction); }
    public double getBalance(){return balance;}
    public void setBalance(double balance) { this.balance = balance; }


    public String getUsername() { return username; }
    public String getFullname() { return fullname; }
    public String getEmail() { return email; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    // public String getCountry() { return country; }
    public String getPostal() { return postal; }
    public String getAccountType() { return accountType; }
    public String getIdNumber() { return idNumber; }
    public String getIdType() { return idType; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<Transaction> getTransactions() { return transactions; }


}





