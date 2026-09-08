package model;

public class Feedback {

    // feedbacks.txt format:
    // [0]feedbackId, [1]customerId, [2]appointmentId,
    // [3]customerComment, [4]technicianFeedback,
    // [5]rating (serviceRating), [6]staffRating
    private String feedbackId;
    private String customerId;
    private String appointmentId;
    private String customerComment;
    private String technicianFeedback;
    private int    serviceRating; // customer rating for the service (1-5)
    private int    staffRating;   // customer rating for the counter staff (1-5)

    public Feedback() {}

    public Feedback(String feedbackId, String customerId, String appointmentId,
                    String customerComment, String technicianFeedback) {
        this.feedbackId         = feedbackId;
        this.customerId         = customerId;
        this.appointmentId      = appointmentId;
        this.customerComment    = customerComment;
        this.technicianFeedback = technicianFeedback;
        this.serviceRating      = 0;
        this.staffRating        = 0;
    }

    public Feedback(String feedbackId, String customerId, String appointmentId,
                    String customerComment, String technicianFeedback,
                    int serviceRating, int staffRating) {
        this.feedbackId         = feedbackId;
        this.customerId         = customerId;
        this.appointmentId      = appointmentId;
        this.customerComment    = customerComment;
        this.technicianFeedback = technicianFeedback;
        this.serviceRating      = serviceRating;
        this.staffRating        = staffRating;
    }

    // Getters / Setters
    public String getFeedbackId()                    { return feedbackId; }
    public void   setFeedbackId(String v)            { this.feedbackId = v; }

    public String getCustomerId()                    { return customerId; }
    public void   setCustomerId(String v)            { this.customerId = v; }

    public String getAppointmentId()                 { return appointmentId; }
    public void   setAppointmentId(String v)         { this.appointmentId = v; }

    public String getCustomerComment()               { return customerComment; }
    public void   setCustomerComment(String v)       { this.customerComment = v; }

    public String getTechnicianFeedback()            { return technicianFeedback; }
    public void   setTechnicianFeedback(String v)    { this.technicianFeedback = v; }

    // serviceRating replaces the old "rating" field
    public int  getRating()                          { return serviceRating; }
    public int  getServiceRating()                   { return serviceRating; }
    public void setServiceRating(int v)              { this.serviceRating = v; }

    public int  getStaffRating()                     { return staffRating; }
    public void setStaffRating(int v)                { this.staffRating = v; }

    public boolean hasCustomerComment()  { return customerComment    != null && !customerComment.isEmpty(); }
    public boolean hasTechnicianFeedback() { return technicianFeedback != null && !technicianFeedback.isEmpty(); }

    public String toFileString() {
        return feedbackId + "," + customerId + "," + appointmentId + ","
             + (customerComment    == null ? "" : customerComment)    + ","
             + (technicianFeedback == null ? "" : technicianFeedback) + ","
             + serviceRating + "," + staffRating;
    }

    @Override
    public String toString() {
        return "Feedback{id='" + feedbackId + "', appt='" + appointmentId
             + "', serviceRating=" + serviceRating + ", staffRating=" + staffRating + "}";
    }
}