// UserManager.java
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private List<User> users;
    private final String DATA_FILE = "users.dat";

    public UserManager() {
        this.users = new ArrayList<>();
        loadUsers(); // Load saved users when creating a UserManager
    }

    // Create a new user
    public void createUser(String username, int score) {
        if (getUserByUsername(username) == null) { // Check if user already exists
            User user = new User(username, score);
            users.add(user);
            System.out.println("User created: " + username);
            saveUsers(); // Save after creating a user
        } else {
            System.out.println("User already exists!");
        }
    }

    // Read all users
    public List<User> getAllUsers() {
        return users;
    }

    // Read a specific user by username
    public User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null; // User not found
    }

    // Update a user's score
    public void updateUserScore(String username, int newScore) {
        User user = getUserByUsername(username);
        if (user != null) {
            user.setScore(newScore);
            System.out.println("Updated score for user: " + username);
            saveUsers(); // Save after updating
        } else {
            System.out.println("User not found!");
        }
    }

    // Delete a user
    public void deleteUser(String username) {
        User user = getUserByUsername(username);
        if (user != null) {
            users.remove(user);
            System.out.println("User deleted: " + username);
            saveUsers(); // Save after deleting
        } else {
            System.out.println("User not found!");
        }
    }

    // Save users to file
    public void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
            System.out.println("Users saved to file successfully.");
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    // Load users from file
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<User> loadedUsers = (List<User>) ois.readObject();
                users = loadedUsers;  // Directly assign the loaded list
                System.out.println("Loaded " + users.size() + " users from file.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading users: " + e.getMessage());
                e.printStackTrace(System.err);
                // Create a new list if loading fails
                users = new ArrayList<>();
            }
        } else {
            System.out.println("No user data file found. Starting with empty user list.");
        }
    }

    // Call this method when the game is shutting down
    public void shutdown() {
        saveUsers(); // Ensure all data is saved
        System.out.println("UserManager shutdown complete.");
    }
    
    // Method to display all users and their scores
    public void displayAllUsers() {
        if (users.isEmpty()) {
            System.out.println("No users registered.");
        } else {
            System.out.println("Current users:");
            for (User user : users) {
                System.out.println(user);
            }
        }
    }
}