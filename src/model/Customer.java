package model;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {

    // 1. Attributes
    private List<String> reservationHistory;

    // 2. Constructors
    public Customer() {
        super();
        this.reservationHistory = new ArrayList<>();
    }

    public Customer(String userId, String name, String email, String password, String role) {
        super(userId, name, email, password, role);
        this.reservationHistory = new ArrayList<>();
    }

    // 3. Getters / Setters
    public List<String> getReservationHistory() {
        return reservationHistory;
    }

    // 4. Utility methods
    public void addReservation(String appointmentId) {
        this.reservationHistory.add(appointmentId);
    }

    public boolean hasReservation(String appointmentId) {
        return this.reservationHistory.contains(appointmentId);
    }

    @Override
    public String toString() {
        return "Customer{" + super.toString() +
               ", reservationHistory=" + reservationHistory + '}';
    }
}