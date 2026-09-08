package repository;

import model.Payment;
import util.FileHandler;

import java.util.ArrayList;

public class PaymentRepository {

    private final String FILE_PATH = "data/payments.txt";

    // payments.txt: [0]paymentId,[1]appointmentId,[2]amount,[3]paymentDate,[4]paymentMethod

    private String toLine(Payment payment) {
        return payment.getPaymentId() + "," + payment.getAppointmentId() + "," +
               payment.getAmount() + "," + payment.getPaymentDate() + "," + payment.getPaymentMethod();
    }
    
    private Payment fromLine(String line) {
        String[] d = line.split(",", -1);
        if (d.length < 5) return null;
        double amount = 0;
        try { amount = Double.parseDouble(d[2]); } catch (NumberFormatException ignored) { }
        return new Payment(d[0], d[1], amount, d[3], d[4]);
    }

    public void savePayment(Payment payment) {
        ArrayList<String> lines = FileHandler.readFile(FILE_PATH);
        lines.add(toLine(payment));
        FileHandler.writeFile(FILE_PATH, lines);
    }

    public Payment getPaymentById(String paymentId) {
        for (String line : FileHandler.readFile(FILE_PATH)) {
            String[] d = line.split(",", -1);
            if (d.length >= 1 && d[0].equals(paymentId)) return fromLine(line);
        }
        return null;
    }

    public ArrayList<Payment> getAllPayments() {
        ArrayList<Payment> list = new ArrayList<>();
        for (String line : FileHandler.readFile(FILE_PATH)) {
            Payment p = fromLine(line);
            if (p != null) list.add(p);
        }
        return list;
    }

    public void updatePayment(Payment payment) {
        ArrayList<String> lines = FileHandler.readFile(FILE_PATH);
        ArrayList<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] d = line.split(",", -1);
            if (d.length >= 1 && d[0].equals(payment.getPaymentId())) {
                updated.add(toLine(payment));
            } else {
                updated.add(line);
            }
        }
        FileHandler.writeFile(FILE_PATH, updated);
    }

    public void deletePayment(String paymentId) {
        ArrayList<String> lines = FileHandler.readFile(FILE_PATH);
        ArrayList<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] d = line.split(",", -1);
            if (d.length >= 1 && !d[0].equals(paymentId)) updated.add(line);
        }
        FileHandler.writeFile(FILE_PATH, updated);
    }
}