package model;

public class Payment {

    // 1. Attributes
    // payments.txt: [0]paymentId,[1]appointmentId,[2]amount,[3]paymentDate,[4]paymentMethod
    private String paymentId;
    private String appointmentId;
    private double amount;
    private String paymentDate;
    private String paymentMethod;

    // 2. Constructors
    public Payment() { }

    public Payment(String paymentId, String appointmentId, double amount,
                   String paymentDate, String paymentMethod) {
        this.paymentId     = paymentId;
        this.appointmentId = appointmentId;
        this.amount        = amount;
        this.paymentDate   = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    // 3. Getters / Setters
    public String getPaymentId()             { return paymentId; }
    public void   setPaymentId(String v)     { this.paymentId = v; }

    public String getAppointmentId()         { return appointmentId; }
    public void   setAppointmentId(String v) { this.appointmentId = v; }

    public double getAmount()                { return amount; }
    public void   setAmount(double v)        { this.amount = v; }

    public String getPaymentDate()           { return paymentDate; }
    public void   setPaymentDate(String v)   { this.paymentDate = v; }

    public String getPaymentMethod()         { return paymentMethod; }
    public void   setPaymentMethod(String v) { this.paymentMethod = v; }

    @Override
    public String toString() {
        return "Payment{" +
               "paymentId='" + paymentId + '\'' +
               ", appointmentId='" + appointmentId + '\'' +
               ", amount=" + amount +
               ", paymentDate='" + paymentDate + '\'' +
               ", paymentMethod='" + paymentMethod + '\'' + '}';
    }
}