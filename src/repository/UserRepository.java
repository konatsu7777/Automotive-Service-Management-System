package repository;

import model.User;
import model.Manager;
import model.CounterStaff;
import model.Technician;
import model.Customer;
import util.FileHandler;

import java.util.ArrayList;

public class UserRepository {

    private static final String MANAGER_FILE    = "data/managers.txt";
    private static final String STAFF_FILE      = "data/staffs.txt";
    private static final String TECHNICIAN_FILE = "data/technicians.txt";
    private static final String CUSTOMER_FILE   = "data/customers.txt";

    private String getFilePath(String role) {
        switch (role) {
            case "Manager":      return MANAGER_FILE;
            case "CounterStaff": return STAFF_FILE;
            case "Technician":   return TECHNICIAN_FILE;
            case "Customer":     return CUSTOMER_FILE;
            default:             return "data/users.txt";
        }
    }

    // txt: [0]userId,[1]name,[2]email,[3]password,[4]role
    private String toLine(User user) {
        return user.getUserId() + "," + user.getName() + "," +
               user.getEmail() + "," + user.getPassword() + "," + user.getRole();
    }

    private User fromLine(String line) {
        String[] d = line.split(",", -1);
        if (d.length < 5) return null;
        String role = d[4];
        switch (role) {
            case "Manager":      return new Manager(d[0], d[1], d[2], d[3], d[4]);
            case "CounterStaff": return new CounterStaff(d[0], d[1], d[2], d[3], d[4]);
            case "Technician":   return new Technician(d[0], d[1], d[2], d[3], d[4]);
            case "Customer":     return new Customer(d[0], d[1], d[2], d[3], d[4]);
            default:             return null;
        }
    }

    public void saveUser(User user) {
        String path = getFilePath(user.getRole());
        ArrayList<String> lines = FileHandler.readFile(path);
        lines.add(toLine(user));
        FileHandler.writeFile(path, lines);
    }

    public ArrayList<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        String[] files = {MANAGER_FILE, STAFF_FILE, TECHNICIAN_FILE, CUSTOMER_FILE};
        for (String file : files) {
            for (String line : FileHandler.readFile(file)) {
                User u = fromLine(line);
                if (u != null) users.add(u);
            }
        }
        return users;
    }

    public ArrayList<User> getUsersByRole(String role) {
        ArrayList<User> users = new ArrayList<>();
        for (String line : FileHandler.readFile(getFilePath(role))) {
            User u = fromLine(line);
            if (u != null) users.add(u);
        }
        return users;
    }

    public User getUserById(String userId) {
        for (User u : getAllUsers()) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    public void updateUser(User user) {
        String path = getFilePath(user.getRole());
        ArrayList<String> lines = FileHandler.readFile(path);
        ArrayList<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] d = line.split(",", -1);
            if (d.length >= 1 && d[0].equals(user.getUserId())) {
                updated.add(toLine(user));
            } else {
                updated.add(line);
            }
        }
        FileHandler.writeFile(path, updated);
    }

    public void deleteUser(String userId) {
        String[] files = {MANAGER_FILE, STAFF_FILE, TECHNICIAN_FILE, CUSTOMER_FILE};
        for (String path : files) {
            ArrayList<String> lines = FileHandler.readFile(path);
            ArrayList<String> updated = new ArrayList<>();
            boolean found = false;
            for (String line : lines) {
                String[] d = line.split(",", -1);
                if (d.length >= 1 && d[0].equals(userId)) {
                    found = true;
                } else {
                    updated.add(line);
                }
            }
            if (found) {
                FileHandler.writeFile(path, updated);
                return;
            }
        }
    }
}