package model;

public class Manager extends User {

    public Manager(String userId, String name, String email, String password, String role) {
        super(userId, name, email, password, role);
    }

    @Override
    public String toString() {
        return "Manager{" +
           super.toString() +
           '}';
    }
}