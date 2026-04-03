package cems.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Employee extends User {
    private String name;
    private String phone;
    private int leaveBalance = 20;

    @ManyToOne
    private Department department;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<LeaveRequest> leaveRequests;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<AttendanceRecord> attendanceRecords;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<SalarySlip> salarySlips;

    // GRASP Principle: CREATOR
    // Employee is responsible for creating its own LeaveRequests
    public LeaveRequest createLeaveRequest(String reason) {
        LeaveRequest req = new LeaveRequest();
        req.setEmployee(this);
        req.setType(reason);
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(1));
        req.setStatus("PENDING");
        return req;
    }
}