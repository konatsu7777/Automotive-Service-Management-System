package service;

import model.Feedback;
import repository.FeedbackRepository;

import java.util.ArrayList;
import java.util.List;

public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService() {
        this.feedbackRepository = new FeedbackRepository();
    }

    // Create an empty feedback record (used by technician side)
    public void createFeedback(String feedbackId, String customerId, String appointmentId) {
        feedbackRepository.saveFeedback(
                new Feedback(feedbackId, customerId, appointmentId, "", "", 0, 0));
    }

    // Customer submits rating + comment (serviceRating + staffRating + customerComment)
    public void addCustomerReview(String feedbackId, String comment, int serviceRating, int staffRating) {
        Feedback f = feedbackRepository.getFeedbackById(feedbackId);
        if (f != null) {
            f.setCustomerComment(comment);
            f.setServiceRating(serviceRating);
            f.setStaffRating(staffRating);
            feedbackRepository.updateFeedback(f);
        }
    }

    // Called when customer submits review for an appointment with no existing feedback record
    public void createCustomerReview(String feedbackId, String customerId, String appointmentId,
                                     String comment, int serviceRating, int staffRating) {
        feedbackRepository.saveFeedback(
                new Feedback(feedbackId, customerId, appointmentId, comment, "", serviceRating, staffRating));
    }

    // Technician adds their feedback note to an existing record
    public void addTechnicianFeedback(String feedbackId, String technicianFeedback) {
        Feedback f = feedbackRepository.getFeedbackById(feedbackId);
        if (f != null) {
            f.setTechnicianFeedback(technicianFeedback);
            feedbackRepository.updateFeedback(f);
        }
    }

    public List<Feedback> getAllFeedbacks()                        { return feedbackRepository.getAllFeedbacks(); }
    public Feedback       getFeedbackById(String feedbackId)       { return feedbackRepository.getFeedbackById(feedbackId); }

    public List<Feedback> getFeedbacksByCustomer(String customerId) {
        List<Feedback> result = new ArrayList<>();
        for (Feedback f : feedbackRepository.getAllFeedbacks())
            if (f.getCustomerId().equals(customerId)) result.add(f);
        return result;
    }

    public List<Feedback> getFeedbacksByAppointment(String appointmentId) {
        List<Feedback> result = new ArrayList<>();
        for (Feedback f : feedbackRepository.getAllFeedbacks())
            if (f.getAppointmentId().equals(appointmentId)) result.add(f);
        return result;
    }
}