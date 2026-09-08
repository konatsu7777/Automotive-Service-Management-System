package model;

public class Appointment {

    // appointments.txt format:
    // [0]appointmentId, [1]customerId, [2]technicianId,
    // [3]serviceType, [4]dateTime, [5]status, [6]carPlate
    private String appointmentId;
    private String customerId;
    private String technicianId;
    private String serviceType;
    private String dateTime;
    private String status;
    private String carPlate;

    public Appointment() {}

    public Appointment(String appointmentId, String customerId, String technicianId,
                       String serviceType, String dateTime, String status) {
        this.appointmentId = appointmentId;
        this.customerId    = customerId;
        this.technicianId  = technicianId;
        this.serviceType   = serviceType;
        this.dateTime      = dateTime;
        this.status        = status;
        this.carPlate      = "";
    }

    public Appointment(String appointmentId, String customerId, String technicianId,
                       String serviceType, String dateTime, String status, String carPlate) {
        this.appointmentId = appointmentId;
        this.customerId    = customerId;
        this.technicianId  = technicianId;
        this.serviceType   = serviceType;
        this.dateTime      = dateTime;
        this.status        = status;
        this.carPlate      = carPlate == null ? "" : carPlate;
    }

    public String getAppointmentId()              { return appointmentId; }
    public void   setAppointmentId(String v)      { this.appointmentId = v; }
    public String getCustomerId()                 { return customerId; }
    public void   setCustomerId(String v)         { this.customerId = v; }
    public String getTechnicianId()               { return technicianId; }
    public void   setTechnicianId(String v)       { this.technicianId = v; }
    public String getServiceType()                { return serviceType; }
    public void   setServiceType(String v)        { this.serviceType = v; }
    public String getDateTime()                   { return dateTime; }
    public void   setDateTime(String v)           { this.dateTime = v; }
    public String getStatus()                     { return status; }
    public void   setStatus(String v)             { this.status = v; }
    public String getCarPlate()                   { return carPlate; }
    public void   setCarPlate(String v)           { this.carPlate = v == null ? "" : v; }

    public boolean isCompleted() { return "COMPLETED".equalsIgnoreCase(this.status); }

    public int getDurationHours() {
        return "Major".equalsIgnoreCase(this.serviceType) ? 3 : 1;
    }

    public String toFileString() {
        return appointmentId + "," + customerId + "," + technicianId + ","
             + serviceType + "," + dateTime + "," + status + ","
             + (carPlate == null ? "" : carPlate);
    }

    @Override
    public String toString() {
        return "Appointment{id='" + appointmentId + "', customer='" + customerId
             + "', type='" + serviceType + "', status='" + status
             + "', car='" + carPlate + "'}";
    }
}