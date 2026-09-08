package service;

import model.Payment;
import repository.PaymentRepository;
import util.FileHandler;

import java.util.ArrayList;
import java.util.List;

public class PaymentService {

    private PaymentRepository paymentRepository;
    private static final String PRICE_FILE = "data/prices.txt";

    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
    }

    // prices.txt: [0]serviceType,[1]price
    public void setPrices(double normalPrice, double majorPrice) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Normal," + normalPrice);
        lines.add("Major," + majorPrice);
        FileHandler.writeFile(PRICE_FILE, lines);
    }

    public double getNormalServicePrice() {
        return readPrice("Normal");
    }

    public double getMajorServicePrice() {
        return readPrice("Major");
    }

    public double getPriceForService(String serviceType) {
        return readPrice(serviceType);
    }

    private double readPrice(String serviceType) {
        for (String line : FileHandler.readFile(PRICE_FILE)) {
            String[] d = line.split(",", -1);
            if (d.length >= 2 && d[0].equalsIgnoreCase(serviceType)) {
                try { return Double.parseDouble(d[1]); } catch (NumberFormatException ignored) { }
            }
        }
        return serviceType.equalsIgnoreCase("Major") ? 200.0 : 80.0; // default
    }

    public void processPayment(String paymentId, String appointmentId,
                               double amount, String paymentDate, String paymentMethod) {
        Payment payment = new Payment(paymentId, appointmentId, amount, paymentDate, paymentMethod);
        paymentRepository.savePayment(payment);
    }

    public String generateReceipt(String paymentId) {
        Payment p = paymentRepository.getPaymentById(paymentId);
        if (p == null) return "Receipt not found.";
        return "========== RECEIPT ==========\n" +
               "Receipt ID   : " + p.getPaymentId()     + "\n" +
               "Appointment  : " + p.getAppointmentId() + "\n" +
               "Amount       : RM " + String.format("%.2f", p.getAmount()) + "\n" +
               "Date         : " + p.getPaymentDate()   + "\n" +
               "Method       : " + p.getPaymentMethod() + "\n" +
               "=============================";
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.getAllPayments();
    }

    public List<Payment> getPaymentsByAppointment(String appointmentId) {
        List<Payment> result = new ArrayList<>();
        for (Payment p : paymentRepository.getAllPayments()) {
            if (p.getAppointmentId().equals(appointmentId)) result.add(p);
        }
        return result;
    }
}