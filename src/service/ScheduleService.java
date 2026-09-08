package service;

import model.Appointment;
import model.Schedule;
import model.User;
import repository.ScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService() {
        this.scheduleRepository = new ScheduleRepository();
    }

    // ── Day-off request ───────────────────────────────────────────
    public void requestDayOff(String scheduleId, String technicianId, String date) {
        // Remove any existing entry for this tech on this date first
        scheduleRepository.deleteByTechnicianAndDate(technicianId, date);
        scheduleRepository.saveSchedule(
                new Schedule(scheduleId, technicianId, date, "09:00", "18:00", Schedule.DAYOFF_REQUEST));
    }

    public void cancelDayOffRequest(String technicianId, String date) {
        scheduleRepository.deleteByTechnicianAndDate(technicianId, date);
    }

    // ── Auto shift assignment ─────────────────────────────────────
    // Rules:
    //   - Max 3 technicians working per day
    //   - Each technician works 5 days per week
    //   - Day-off requests are respected where possible
    //   - When conflicts make < 3 people available, still assign who is left
    //     (caller checks shortfall and highlights in red)
    public void assignMonthlyShifts(String yearMonth, List<User> technicians) {
        // Clear existing WORK schedules for this month (keep DAYOFF_REQUEST)
        List<Schedule> existing = scheduleRepository.getSchedulesByMonth(yearMonth);
        for (Schedule s : existing)
            if (s.isWork()) scheduleRepository.deleteSchedule(s.getScheduleId());

        YearMonth ym        = YearMonth.parse(yearMonth);
        int       daysInMonth = ym.lengthOfMonth();
        int       idCounter   = getNextScheduleIdCounter();

        // Track how many days each tech has worked this week
        Map<String, Integer> weekWorkCount = new HashMap<>();
        int                  currentWeek   = -1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date    = ym.atDay(day);
            String    dateStr = date.toString();
            int       week    = date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());

            if (week != currentWeek) {
                weekWorkCount.clear();
                currentWeek = week;
            }

            // Who requested day off today?
            Set<String> requestedOff = new HashSet<>();
            for (Schedule s : scheduleRepository.getSchedulesByDate(dateStr))
                if (s.isDayOffRequest()) requestedOff.add(s.getTechnicianId());

            // Who is eligible? (not requested off + hasn't worked 5 days this week)
            List<String> eligible = new ArrayList<>();
            for (User t : technicians) {
                int worked = weekWorkCount.getOrDefault(t.getUserId(), 0);
                if (!requestedOff.contains(t.getUserId()) && worked < 5)
                    eligible.add(t.getUserId());
            }

            // Pick up to 3 (prefer those who have worked fewer days)
            eligible.sort(Comparator.comparingInt(id -> weekWorkCount.getOrDefault(id, 0)));
            List<String> workers = eligible.subList(0, Math.min(3, eligible.size()));

            for (String techId : workers) {
                String scId = String.format("SC%04d", idCounter++);
                scheduleRepository.saveSchedule(
                        new Schedule(scId, techId, dateStr, "09:00", "18:00", Schedule.WORK));
                weekWorkCount.merge(techId, 1, Integer::sum);
            }
        }
    }

    // Returns dates in the month where fewer than 3 people are scheduled (shortage)
    public Set<String> getShortfallDates(String yearMonth) {
        Set<String> shortage = new HashSet<>();
        YearMonth   ym       = YearMonth.parse(yearMonth);
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            String date    = ym.atDay(day).toString();
            long   workers = scheduleRepository.getSchedulesByDate(date).stream()
                    .filter(Schedule::isWork).count();
            if (workers < 3) shortage.add(date);
        }
        return shortage;
    }

    // ── Availability check (used by StaffUI assign) ───────────────
    public List<User> getAvailableTechnicians(String date, String timeSlot,
                                              int durationHours,
                                              List<User> allTechnicians,
                                              List<Appointment> allAppointments) {
        List<Schedule> daySchedules = scheduleRepository.getSchedulesByDate(date);
        List<User>     available    = new ArrayList<>();

        for (User tech : allTechnicians) {
            Schedule workShift = null;
            for (Schedule s : daySchedules)
                if (s.isWork() && s.getTechnicianId().equals(tech.getUserId())) { workShift = s; break; }
            if (workShift == null) continue;
            if (!workShift.coversSlot(timeSlot, durationHours)) continue;
            if (hasConflict(tech.getUserId(), date, timeSlot, durationHours, allAppointments)) continue;
            available.add(tech);
        }
        return available;
    }

    private boolean hasConflict(String technicianId, String date, String timeSlot,
                                int durationHours, List<Appointment> allAppointments) {
        int reqStart = toMinutes(timeSlot);
        int reqEnd   = reqStart + durationHours * 60;
        for (Appointment a : allAppointments) {
            if (!"COMPLETED".equalsIgnoreCase(a.getStatus())
                    && technicianId.equals(a.getTechnicianId())
                    && a.getDateTime() != null
                    && a.getDateTime().startsWith(date)) {
                String t = a.getDateTime().contains(" ") ? a.getDateTime().split(" ")[1] : null;
                if (t == null) continue;
                int existStart = toMinutes(t);
                int existEnd   = existStart + a.getDurationHours() * 60;
                if (reqStart < existEnd && reqEnd > existStart) return true;
            }
        }
        return false;
    }

    // ── Query helpers ─────────────────────────────────────────────
    public List<Schedule> getMySchedules(String technicianId) {
        return scheduleRepository.getSchedulesByTechnician(technicianId);
    }

    public List<Schedule> getSchedulesByDate(String date) {
        return scheduleRepository.getSchedulesByDate(date);
    }

    public List<Schedule> getSchedulesByMonth(String yearMonth) {
        return scheduleRepository.getSchedulesByMonth(yearMonth);
    }

    public void deleteSchedule(String scheduleId) {
        scheduleRepository.deleteSchedule(scheduleId);
    }

    public void registerSchedule(String scheduleId, String technicianId,
                                 String date, String startTime, String endTime) {
        scheduleRepository.saveSchedule(
                new Schedule(scheduleId, technicianId, date, startTime, endTime, Schedule.WORK));
    }

    // ── Helpers ───────────────────────────────────────────────────
    private int toMinutes(String time) {
        String[] p = time.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    private int getNextScheduleIdCounter() {
        int max = 0;
        for (Schedule s : scheduleRepository.getAllSchedules()) {
            String id = s.getScheduleId().replaceAll("[^0-9]", "");
            try { max = Math.max(max, Integer.parseInt(id)); } catch (NumberFormatException ignored) {}
        }
        return max + 1;
    }
}