package repository;

import model.Schedule;
import util.FileHandler;

import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {

    private final String FILE_PATH = "data/schedules.txt";

    // [0]scheduleId, [1]technicianId, [2]date,
    // [3]startTime, [4]endTime, [5]type
    private Schedule fromLine(String line) {
        String[] d = line.split(",", -1);
        if (d.length < 5) return null;
        String type = d.length > 5 ? d[5].trim() : Schedule.WORK;
        return new Schedule(d[0].trim(), d[1].trim(), d[2].trim(),
                            d[3].trim(), d[4].trim(), type);
    }

    public void saveSchedule(Schedule schedule) {
        ArrayList<String> lines = FileHandler.readFile(FILE_PATH);
        lines.add(schedule.toFileString());
        FileHandler.writeFile(FILE_PATH, lines);
    }

    public List<Schedule> getAllSchedules() {
        List<Schedule> list = new ArrayList<>();
        for (String line : FileHandler.readFile(FILE_PATH)) {
            Schedule s = fromLine(line);
            if (s != null) list.add(s);
        }
        return list;
    }

    public List<Schedule> getSchedulesByTechnician(String technicianId) {
        List<Schedule> list = new ArrayList<>();
        for (Schedule s : getAllSchedules())
            if (s.getTechnicianId().equals(technicianId)) list.add(s);
        return list;
    }

    public List<Schedule> getSchedulesByDate(String date) {
        List<Schedule> list = new ArrayList<>();
        for (Schedule s : getAllSchedules())
            if (s.getDate().equals(date)) list.add(s);
        return list;
    }

    public List<Schedule> getSchedulesByMonth(String yearMonth) {
        List<Schedule> list = new ArrayList<>();
        for (Schedule s : getAllSchedules())
            if (s.getDate().startsWith(yearMonth)) list.add(s);
        return list;
    }

    public void deleteSchedule(String scheduleId) {
        ArrayList<String> lines   = FileHandler.readFile(FILE_PATH);
        ArrayList<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] d = line.split(",", -1);
            if (d.length >= 1 && !d[0].trim().equals(scheduleId)) updated.add(line);
        }
        FileHandler.writeFile(FILE_PATH, updated);
    }

    public void deleteByTechnicianAndDate(String technicianId, String date) {
        ArrayList<String> lines   = FileHandler.readFile(FILE_PATH);
        ArrayList<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] d = line.split(",", -1);
            if (d.length >= 3
                    && d[1].trim().equals(technicianId)
                    && d[2].trim().equals(date)) continue;
            updated.add(line);
        }
        FileHandler.writeFile(FILE_PATH, updated);
    }

    public void deleteByMonth(String yearMonth) {
        ArrayList<String> lines   = FileHandler.readFile(FILE_PATH);
        ArrayList<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] d = line.split(",", -1);
            if (d.length >= 3 && d[2].trim().startsWith(yearMonth)) continue;
            updated.add(line);
        }
        FileHandler.writeFile(FILE_PATH, updated);
    }
}