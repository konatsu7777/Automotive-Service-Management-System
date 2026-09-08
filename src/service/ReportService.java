package service;

import model.Appointment;
import model.Feedback;
import model.Payment;
import repository.AppointmentRepository;
import repository.FeedbackRepository;
import repository.PaymentRepository;

import java.util.*;

public class ReportService {

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository     paymentRepository;
    private final FeedbackRepository    feedbackRepository;

    public ReportService() {
        this.appointmentRepository = new AppointmentRepository();
        this.paymentRepository     = new PaymentRepository();
        this.feedbackRepository    = new FeedbackRepository();
    }

    // ── Overall ───────────────────────────────────────────────────
    public double calculateTotalSales() {
        double total = 0.0;
        for (Payment p : paymentRepository.getAllPayments()) total += p.getAmount();
        return total;
    }

    public int countTotalAppointments() {
        return appointmentRepository.getAllAppointments().size();
    }

    public double calculateAverageRating() {
        return calculateAverageRatingForMonth(null);
    }

    public Map<String, Integer> getAppointmentStatusStatistics() {
        return getAppointmentStatusStatisticsForMonth(null);
    }

    public Map<String, Integer> getServiceTypeStatistics() {
        return getServiceTypeStatisticsForMonth(null);
    }

    // ── Monthly ───────────────────────────────────────────────────
    // yearMonth format: "2026-04" or null for all
    public double calculateTotalSalesForMonth(String yearMonth) {
        double total = 0.0;
        for (Payment p : paymentRepository.getAllPayments()) {
            if (yearMonth == null || p.getPaymentDate().startsWith(yearMonth))
                total += p.getAmount();
        }
        return total;
    }

    public int countAppointmentsForMonth(String yearMonth) {
        int count = 0;
        for (Appointment a : appointmentRepository.getAllAppointments()) {
            if (yearMonth == null || a.getDateTime().startsWith(yearMonth)) count++;
        }
        return count;
    }

    public double calculateAverageRatingForMonth(String yearMonth) {
        List<Appointment> appts = appointmentRepository.getAllAppointments();
        Set<String> apptIds = new HashSet<>();
        for (Appointment a : appts)
            if (yearMonth == null || a.getDateTime().startsWith(yearMonth))
                apptIds.add(a.getAppointmentId());

        int sum = 0, count = 0;
        for (Feedback f : feedbackRepository.getAllFeedbacks()) {
            if (apptIds.contains(f.getAppointmentId()) && f.getRating() > 0) {
                sum += f.getRating(); count++;
            }
        }
        return count == 0 ? 0.0 : (double) sum / count;
    }

    public Map<String, Integer> getAppointmentStatusStatisticsForMonth(String yearMonth) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        for (Appointment a : appointmentRepository.getAllAppointments()) {
            if (yearMonth == null || a.getDateTime().startsWith(yearMonth))
                stats.merge(a.getStatus(), 1, Integer::sum);
        }
        return stats;
    }

    public Map<String, Integer> getServiceTypeStatisticsForMonth(String yearMonth) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        for (Appointment a : appointmentRepository.getAllAppointments()) {
            if (yearMonth == null || a.getDateTime().startsWith(yearMonth))
                stats.merge(a.getServiceType(), 1, Integer::sum);
        }
        return stats;
    }

    // Returns sorted list of yearMonth strings present in appointments
    public List<String> getAvailableMonths() {
        Set<String> months = new TreeSet<>();
        for (Appointment a : appointmentRepository.getAllAppointments()) {
            String dt = a.getDateTime();
            if (dt != null && dt.length() >= 7)
                months.add(dt.substring(0, 7));
        }
        return new ArrayList<>(months);
    }
}