package repository;

import model.Feedback;
import util.FileHandler;

import java.util.ArrayList;

public class FeedbackRepository {

    private final String FILE_PATH = "data/feedbacks.txt";

    // feedbacks.txt format:
    // [0]feedbackId, [1]customerId, [2]appointmentId,
    // [3]customerComment, [4]technicianFeedback,
    // [5]serviceRating, [6]staffRating
    private Feedback fromLine(String line) {
        String[] d = line.split(",", -1);
        if (d.length < 5) return null;
        int serviceRating = 0;
        int staffRating   = 0;
        try { if (d.length > 5) serviceRating = Integer.parseInt(d[5].trim()); } catch (NumberFormatException ignored) {}
        try { if (d.length > 6) staffRating   = Integer.parseInt(d[6].trim()); } catch (NumberFormatException ignored) {}
        return new Feedback(d[0], d[1], d[2], d[3], d[4], serviceRating, staffRating);
    }

    public void saveFeedback(Feedback feedback) {
        ArrayList<String> lines = FileHandler.readFile(FILE_PATH);
        lines.add(feedback.toFileString());
        FileHandler.writeFile(FILE_PATH, lines);
    }

    public Feedback getFeedbackById(String feedbackId) {
        for (String line : FileHandler.readFile(FILE_PATH)) {
            String[] d = line.split(",", -1);
            if (d.length >= 1 && d[0].equals(feedbackId)) return fromLine(line);
        }
        return null;
    }

    public ArrayList<Feedback> getAllFeedbacks() {
        ArrayList<Feedback> list = new ArrayList<>();
        for (String line : FileHandler.readFile(FILE_PATH)) {
            Feedback f = fromLine(line);
            if (f != null) list.add(f);
        }
        return list;
    }

    public void updateFeedback(Feedback feedback) {
        ArrayList<String> lines   = FileHandler.readFile(FILE_PATH);
        ArrayList<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] d = line.split(",", -1);
            updated.add(d.length >= 1 && d[0].equals(feedback.getFeedbackId())
                    ? feedback.toFileString() : line);
        }
        FileHandler.writeFile(FILE_PATH, updated);
    }

    public void deleteFeedback(String feedbackId) {
        ArrayList<String> lines   = FileHandler.readFile(FILE_PATH);
        ArrayList<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] d = line.split(",", -1);
            if (d.length >= 1 && !d[0].equals(feedbackId)) updated.add(line);
        }
        FileHandler.writeFile(FILE_PATH, updated);
    }
}