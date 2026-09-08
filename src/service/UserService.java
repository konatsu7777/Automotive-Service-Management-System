package service;

import model.User;
import model.Manager;
import model.CounterStaff;
import model.Technician;
import model.Customer;
import repository.UserRepository;

import java.util.ArrayList;

public class UserService {

    private UserRepository     userRepository;
    private IdGeneratorService idGeneratorService;

    private static final String EMAIL_REGEX = "^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$";
    
    public UserService() {
        this.userRepository     = new UserRepository();
        this.idGeneratorService = new IdGeneratorService();
    }

    public String registerUser(String name, String email, String password, String role) {
        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        String userId = idGeneratorService.generateUserId(role);
        User user;
        switch (role) {
            case "Manager":      user = new Manager(userId, name, email, password, role); break;
            case "CounterStaff": user = new CounterStaff(userId, name, email, password, role); break;
            case "Technician":   user = new Technician(userId, name, email, password, role); break;
            case "Customer":     user = new Customer(userId, name, email, password, role); break;
            default: return null;
        }
        userRepository.saveUser(user);
        return userId;
    }

    public User login(String userId, String password, String role) {
        for (User user : userRepository.getUsersByRole(role)) {
            if (user.getUserId().equals(userId) && user.authenticate(password)) {
                return user;
            }
        }
        return null;
    }
    
    public ArrayList<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public ArrayList<User> getUsersByRole(String role) {
        return userRepository.getUsersByRole(role);
    }

    public User getUserById(String userId) {
        return userRepository.getUserById(userId);
    }

    public void updateUser(String userId, String name, String email, String password) {
        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        User user = userRepository.getUserById(userId);
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            userRepository.updateUser(user);
        }
    }

    public void deleteUser(String userId) {
        userRepository.deleteUser(userId);
    }
}