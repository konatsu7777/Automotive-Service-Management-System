package service;

import util.FileHandler;

import java.util.ArrayList;

public class IdGeneratorService {

    public IdGeneratorService() { }

    private int getMaxId(String filePath) {
        int max = 0;
        ArrayList<String> lines = FileHandler.readFile(filePath);
        for (String line : lines) {
            String[] d = line.split(",", -1);
            if (d.length < 1) continue;
            try {
                String idStr = d[0].replaceAll("[^0-9]", "");
                if (!idStr.isEmpty()) {
                    int num = Integer.parseInt(idStr);
                    if (num > max) max = num;
                }
            } catch (NumberFormatException ignored) { }
        }
        return max;
    }

    public String generateManagerId() {
        int next = getMaxId("data/managers.txt") + 1;
        return "M" + String.format("%03d", next);
    }

    public String generateStaffId() {
        int next = getMaxId("data/staffs.txt") + 1;
        return "S" + String.format("%03d", next);
    }

    public String generateTechnicianId() {
        int next = getMaxId("data/technicians.txt") + 1;
        return "T" + String.format("%03d", next);
    }

    public String generateCustomerId() {
        int next = getMaxId("data/customers.txt") + 1;
        return "C" + String.format("%03d", next);
    }

    public String generateUserId(String role) {
        switch (role) {
            case "Manager":      return generateManagerId();
            case "CounterStaff": return generateStaffId();
            case "Technician":   return generateTechnicianId();
            case "Customer":     return generateCustomerId();
            default:             return "U" + String.format("%03d", 1);
        }
    }

    public String generateAppointmentId() {
        int next = getMaxId("data/appointments.txt") + 1;
        return "A" + String.format("%03d", next);
    }

    public String generatePaymentId() {
        int next = getMaxId("data/payments.txt") + 1;
        return "P" + String.format("%03d", next);
    }

    public String generateFeedbackId() {
        int next = getMaxId("data/feedbacks.txt") + 1;
        return "F" + String.format("%03d", next);
    }
    
    public String generateScheduleId() {
        int next = getMaxId("data/schedule.txt") + 1;
        return "SC" + String.format("%03d", next);
    } 
}