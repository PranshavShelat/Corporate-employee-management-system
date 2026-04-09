package cems.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Employee extends User {
    private String name;
    private String phone;
    private int leaveBalance = 20;

    @ManyToOne
    private Department department;

    @ManyToMany
    private List<Project> projects;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<LeaveRequest> leaveRequests;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<AttendanceRecord> attendanceRecords;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<SalarySlip> salarySlips;

    // GRASP PRINCIPLE 1: CREATOR
    // Updated to accept actual user-selected dates!
    public LeaveRequest createLeaveRequest(String reason, LocalDate startDate, LocalDate endDate) {
        LeaveRequest req = new LeaveRequest();
        req.setEmployee(this);
        req.setType(reason);
        req.setStartDate(startDate);
        req.setEndDate(endDate);
        req.setStatus("PENDING");
        return req;
    }
}