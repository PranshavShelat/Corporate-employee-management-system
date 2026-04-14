package cems.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("EMPLOYEE")
@Data
@EqualsAndHashCode(callSuper = true)
public class Employee extends User {
    private String name;
    private String phone;
    private int leaveBalance = 20;

    @ManyToOne
    private Department department;

    // Safety initialization prevents 500 errors during assignment
    @ManyToMany
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeaveRequest> leaveRequests = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalarySlip> salarySlips = new ArrayList<>();

    // GRASP PRINCIPLE 1: CREATOR
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