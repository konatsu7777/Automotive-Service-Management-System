package model;

public class CounterStaff extends User {

    public CounterStaff(String userId, String name, String email, String password, String role) {
        super(userId, name, email, password, role);
    }

    @Override
    public String toString() {
        return "Staff{" +
            super.toString() +
            '}';
    }
}