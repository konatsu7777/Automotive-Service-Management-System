package repository;

import model.Appointment;
import util.FileHandler;

import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {

    private final String FILE_PATH = "data/appointments.txt";

    // [0]appointmentId, [1]customerId, [2]technicianId,
    // [3]serviceType, [4]dateTime, [5]status, [6]carPlate (optional)
    private Appointment fromLine(String line) {
        String[] d = line.split(",", -1);
        if (d.length < 6) return null;
        String carPlate = d.length > 6 ? d[6].trim() : "";
        return new Appointment(d[0].trim(), d[1].trim(), d[2].trim(),
                               d[3].trim(), d[4].trim(), d[5].trim(), carPlate);
    }

    public void saveAppointment(Appointment appointment) {
        ArrayList<String> lines = FileHandler.readFile(FILE_PATH);
        lines.add(appointment.toFileString());
        FileHandler.writeFile(FILE_PATH, lines);
    }

    public Appointment getAppointmentById(String appointmentId) {
        for (String line : FileHandler.readFile(FILE_PATH)) {
            String[] d = line.split(",", -1);
            if (d.length >= 1 && d[0].trim().equals(appointmentId)) return fromLine(line);
        }
        return null;
    }

    public ArrayList<Appointment> getAllAppointments() {
        ArrayList<Appointment> list = new ArrayList<>();
        for (String line : FileHandler.readFile(FILE_PATH)) {
            Appointment a = fromLine(line);
            if (a != null) list.add(a);
        }
        return list;
    }

    public List<Appointment> getAppointmentsByCustomer(String customerId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment a : getAllAppointments())
            if (a.getCustomerId().equals(customerId)) list.add(a);
        return list;
    }

    public List<Appointment> getAppointmentsByTechnician(String technicianId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment a : getAllAppointments())
            if (technicianId.equals(a.getTechnicianId())) list.add(a);
        return list;
    }

    public void updateAppointment(Appointment appointment) {
        ArrayList<String> lines   = FileHandler.readFile(FILE_PATH);
        ArrayList<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] d = line.split(",", -1);
            updated.add(d.length >= 1 && d[0].trim().equals(appointment.getAppointmentId())
                    ? appointment.toFileString() : line);
        }
        FileHandler.writeFile(FILE_PATH, updated);
    }

    public void deleteAppointment(String appointmentId) {
        ArrayList<String> lines   = FileHandler.readFile(FILE_PATH);
        ArrayList<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] d = line.split(",", -1);
            if (d.length >= 1 && !d[0].trim().equals(appointmentId)) updated.add(line);
        }
        FileHandler.writeFile(FILE_PATH, updated);
    }
}