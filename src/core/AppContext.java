package core;

import service.*;

public class AppContext {

    private static AppContext instance;

    private UserService userService;
    private AppointmentService appointmentService;
    private PaymentService paymentService;
    private FeedbackService feedbackService;
    private ReportService reportService;
    private IdGeneratorService idGeneratorService;
    private final ScheduleService scheduleService = new ScheduleService();

    private AppContext() {
        this.userService = new UserService();
        this.appointmentService = new AppointmentService();
        this.paymentService = new PaymentService();
        this.feedbackService = new FeedbackService();
        this.reportService = new ReportService();
        this.idGeneratorService = new IdGeneratorService();
    }

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public UserService getUserService() {
        return userService;
    }

    public AppointmentService getAppointmentService() {
        return appointmentService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public FeedbackService getFeedbackService() {
        return feedbackService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public IdGeneratorService getIdGeneratorService() {
        return idGeneratorService;
    }
    
    public ScheduleService getScheduleService() { 
        return scheduleService;
    }
}