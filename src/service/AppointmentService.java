package service;

import model.Appointment;
import repository.AppointmentRepository;

import java.util.List;

public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService() {
        this.appointmentRepository = new AppointmentRepository();
    }

    public void createAppointment(String appointmentId, String customerId,
                                  String serviceType, String dateTime, String carPlate) {
        appointmentRepository.saveAppointment(
                new Appointment(appointmentId, customerId, "", serviceType, dateTime, "PENDING", carPlate));
    }

    public void assignTechnician(String appointmentId, String technicianId) {
        Appointment a = appointmentRepository.getAppointmentById(appointmentId);
        if (a != null) {
            a.setTechnicianId(technicianId);
            a.setStatus("ASSIGNED");
            appointmentRepository.updateAppointment(a);
        }
    }

    public void completeAppointment(String appointmentId) {
        Appointment a = appointmentRepository.getAppointmentById(appointmentId);
        if (a != null) {
            a.setStatus("COMPLETED");
            appointmentRepository.updateAppointment(a);
        }
    }

    public void updateAppointment(String appointmentId, String customerId, String technicianId,
                                  String serviceType, String dateTime, String status) {
        Appointment a = appointmentRepository.getAppointmentById(appointmentId);
        if (a != null) {
            a.setCustomerId(customerId);
            a.setTechnicianId(technicianId);
            a.setServiceType(serviceType);
            a.setDateTime(dateTime);
            a.setStatus(status);
            appointmentRepository.updateAppointment(a);
        }
    }

    public Appointment        getAppointmentById(String id)       { return appointmentRepository.getAppointmentById(id); }
    public List<Appointment>  getAllAppointments()                 { return appointmentRepository.getAllAppointments(); }
    public List<Appointment>  getAppointmentsByCustomer(String id){ return appointmentRepository.getAppointmentsByCustomer(id); }
    public List<Appointment>  getAppointmentsByTechnician(String id){ return appointmentRepository.getAppointmentsByTechnician(id); }
    public void               deleteAppointment(String id)        { appointmentRepository.deleteAppointment(id); }
}