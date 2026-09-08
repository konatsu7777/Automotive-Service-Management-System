package model;

public class Schedule {

    // schedules.txt format:
    // [0]scheduleId, [1]technicianId, [2]date (yyyy-MM-dd),
    // [3]startTime, [4]endTime, [5]type (WORK / DAYOFF_REQUEST)
    private String scheduleId;
    private String technicianId;
    private String date;
    private String startTime;
    private String endTime;
    private String type; // "WORK" or "DAYOFF_REQUEST"

    public static final String WORK          = "WORK";
    public static final String DAYOFF_REQUEST = "DAYOFF_REQUEST";

    public Schedule() {}

    public Schedule(String scheduleId, String technicianId,
                    String date, String startTime, String endTime, String type) {
        this.scheduleId   = scheduleId;
        this.technicianId = technicianId;
        this.date         = date;
        this.startTime    = startTime;
        this.endTime      = endTime;
        this.type         = type == null ? WORK : type;
    }

    public String getScheduleId()            { return scheduleId; }
    public void   setScheduleId(String v)    { this.scheduleId = v; }
    public String getTechnicianId()          { return technicianId; }
    public void   setTechnicianId(String v)  { this.technicianId = v; }
    public String getDate()                  { return date; }
    public void   setDate(String v)          { this.date = v; }
    public String getStartTime()             { return startTime; }
    public void   setStartTime(String v)     { this.startTime = v; }
    public String getEndTime()               { return endTime; }
    public void   setEndTime(String v)       { this.endTime = v; }
    public String getType()                  { return type; }
    public void   setType(String v)          { this.type = v; }

    public boolean isWork()         { return WORK.equalsIgnoreCase(type); }
    public boolean isDayOffRequest(){ return DAYOFF_REQUEST.equalsIgnoreCase(type); }

    public boolean coversSlot(String timeSlot, int durationHours) {
        if (!isWork()) return false;
        try {
            int slotStart = toMinutes(timeSlot);
            int slotEnd   = slotStart + durationHours * 60;
            int workStart = toMinutes(startTime);
            int workEnd   = toMinutes(endTime);
            return slotStart >= workStart && slotEnd <= workEnd;
        } catch (Exception e) {
            return false;
        }
    }

    private int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public String toFileString() {
        return scheduleId + "," + technicianId + "," + date + ","
             + startTime + "," + endTime + "," + type;
    }

    @Override
    public String toString() {
        return "Schedule{id='" + scheduleId + "', tech='" + technicianId
             + "', date='" + date + "', type='" + type + "'}";
    }
}