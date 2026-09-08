# Automotive Service Management System

A comprehensive Java Swing-based desktop application designed for managing automotive service centre operations. Built with a clean, layered architecture separating core models, data persistence repositories, business logic services, and user interfaces.

---

## 📌 Features

* **Role-Based Authentication & Navigation**
  * Custom access flows for `Manager`, `CounterStaff`, `Technician`, `Customer`, and `Guest` users.
* **Appointment Management**
  * Schedule, track, and update vehicle service requests with automated status tracking.
* **Technician Scheduling & Work Allocation**
  * Assign technicians to specific appointments and manage workload schedules.
* **Billing & Payments**
  * Calculate service fees, generate receipts, and track payment transactions.
* **Feedback System**
  * Collect customer ratings and service reviews.
* **Reporting & Analytics**
  * Generate management reports for service income, customer activity, and operational performance.
* **File-Based Data Persistence**
  * Lightweight text-based storage using dedicated file handlers and repositories.

---

## 🛠 Tech Stack & Architecture

* **Language:** Java (JDK 8 or higher)
* **GUI Framework:** Java Swing
* **Architecture:** Layered Architecture (Models, Repositories, Services, UI, Utilities)
* **Data Storage:** Flat File Persistence (`.txt` files)
* **IDE:** Apache NetBeans

---

## 📁 Directory Structure

```text
Automotive-Service-Management-System/
├── data/                       # Text-based data storage
│   ├── appointments.txt
│   ├── customers.txt
│   ├── feedbacks.txt
│   ├── managers.txt
│   ├── payments.txt
│   ├── prices.txt
│   ├── schedules.txt
│   ├── staffs.txt
│   └── technicians.txt
│
└── src/
    ├── core/                   # Global application state
    │   └── AppContext.java
    ├── model/                  # Domain entities & data models
    │   ├── Appointment.java
    │   ├── Authenticatable.java
    │   ├── CounterStaff.java
    │   ├── Customer.java
    │   ├── Feedback.java
    │   ├── Manager.java
    │   ├── Payment.java
    │   ├── Schedule.java
    │   ├── Technician.java
    │   └── User.java
    ├── repository/             # Data access layer
    │   ├── AppointmentRepository.java
    │   ├── FeedbackRepository.java
    │   ├── PaymentRepository.java
    │   ├── ScheduleRepository.java
    │   └── UserRepository.java
    ├── service/                # Business logic layer
    │   ├── AppointmentService.java
    │   ├── FeedbackService.java
    │   ├── IdGeneratorService.java
    │   ├── PaymentService.java
    │   ├── ReportService.java
    │   ├── ScheduleService.java
    │   └── UserService.java
    ├── ui/                     # Java Swing User Interface
    │   ├── CustomerUI.java
    │   ├── GuestUI.java
    │   ├── LoginUI.java
    │   ├── ManagerUI.java
    │   ├── StaffUI.java
    │   ├── StartUI.java
    │   └── TechnicianUI.java
    └── util/                   # Utility helpers
        └── FileHandler.java