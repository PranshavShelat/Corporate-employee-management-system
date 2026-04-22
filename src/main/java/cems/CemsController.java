package cems;

import cems.models.*;
import cems.patterns.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

@Controller
public class CemsController {

    @Autowired private EmployeeRepo employeeRepo;
    @Autowired private LeaveRepo leaveRepo;
    @Autowired private AttendanceRepo attendanceRepo;
    @Autowired private SalarySlipRepo salaryRepo;
    @Autowired private DepartmentRepo departmentRepo;
    @Autowired private ProjectRepo projectRepo;
    @Autowired private UserFactory userFactory;

    @GetMapping("/")
    public String dashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (role == null) return "index"; 
        
        if (role.equals("ADMIN")) {
            model.addAttribute("employees", employeeRepo.findAll());
            model.addAttribute("leaves", leaveRepo.findAll());
            model.addAttribute("departments", departmentRepo.findAll());
            model.addAttribute("projects", projectRepo.findAll());
            model.addAttribute("attendances", attendanceRepo.findAll());
        } else {
            Employee emp = employeeRepo.findById(((Employee) session.getAttribute("user")).getId()).orElse(null);
            model.addAttribute("employee", emp);
            model.addAttribute("myLeaves", emp.getLeaveRequests());
            model.addAttribute("mySlips", emp.getSalarySlips());
            
            if (emp.getRole().equals("MANAGER") && emp.getDepartment() != null) {
                model.addAttribute("myTeam", emp.getDepartment().getEmployees());
            }
            
            AttendanceRecord todayRecord = emp.getAttendanceRecords().stream()
                                    .filter(a -> a.getDate().equals(LocalDate.now()))
                                    .findFirst().orElse(null);
                                    
            boolean clockedInToday = todayRecord != null;
            boolean clockedOutToday = todayRecord != null && todayRecord.getTimeOut() != null;
            
            model.addAttribute("clockedInToday", clockedInToday);
            model.addAttribute("clockedOutToday", clockedOutToday);
        }
        return "index"; 
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        if (username.equals("admin") && password.equals("admin")) {
            session.setAttribute("role", "ADMIN");
            session.setAttribute("name", "Administrator");
            return "redirect:/";
        }
        for (Employee emp : employeeRepo.findAll()) {
            if (emp.getUsername().equals(username) && emp.getPassword().equals(password)) {
                session.setAttribute("role", emp.getRole());
                session.setAttribute("user", emp);
                session.setAttribute("name", emp.getName());
                return "redirect:/";
            }
        }
        return "redirect:/?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/add-user")
    public String addUser(@RequestParam String name, @RequestParam String username, 
                          @RequestParam String password, @RequestParam String role) {
        Employee emp = (Employee) userFactory.createUser(role);
        emp.setName(name);
        emp.setUsername(username);
        emp.setPassword(password);
        emp.setRole(role);
        employeeRepo.save(emp);
        return "redirect:/";
    }

    @PostMapping("/delete-user")
    @Transactional
    public String deleteUser(@RequestParam Long empId) {
        Employee emp = employeeRepo.findById(empId).orElse(null);
        if (emp != null) {
            if (emp.getProjects() != null) {
                emp.getProjects().clear(); 
            }
            employeeRepo.save(emp); 
            employeeRepo.deleteById(empId);
        }
        return "redirect:/";
    }

    @PostMapping("/apply-leave")
    public String applyLeave(@RequestParam String reason, 
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             HttpSession session) {
        Employee emp = employeeRepo.findById(((Employee) session.getAttribute("user")).getId()).get();
        long daysRequested = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        if (daysRequested <= 0) return "redirect:/?leaveError=invalid_dates";
        if (emp.getLeaveBalance() < daysRequested) return "redirect:/?leaveError=balance";

        for (LeaveRequest existingReq : emp.getLeaveRequests()) {
            if (!existingReq.getStatus().equals("REJECTED")) {
                if (!startDate.isAfter(existingReq.getEndDate()) && !endDate.isBefore(existingReq.getStartDate())) {
                    return "redirect:/?leaveError=overlap";
                }
            }
        }
        
        LeaveRequest req = emp.createLeaveRequest(reason, startDate, endDate);
        leaveRepo.save(req);
        emp.setLeaveBalance(emp.getLeaveBalance() - (int)daysRequested);
        employeeRepo.save(emp);
        return "redirect:/?leaveSuccess=true";
    }

    @PostMapping("/update-leave")
    public String updateLeave(@RequestParam Long leaveId, @RequestParam String action) {
        LeaveRequest req = leaveRepo.findById(leaveId).get();
        req.setStatus(action);
        if(action.equals("REJECTED")) {
            long daysRefunded = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
            req.getEmployee().setLeaveBalance(req.getEmployee().getLeaveBalance() + (int)daysRefunded);
        }
        leaveRepo.save(req);
        return "redirect:/";
    }

    @PostMapping("/clock-in")
    public String clockIn(HttpSession session) {
        Employee emp = employeeRepo.findById(((Employee) session.getAttribute("user")).getId()).get();
        boolean alreadyClockedIn = emp.getAttendanceRecords().stream().anyMatch(a -> a.getDate().equals(LocalDate.now()));
        if (!alreadyClockedIn) {
            AttendanceRecord record = new AttendanceRecord();
            record.setEmployee(emp);
            record.setDate(LocalDate.now());
            record.setTimeIn(LocalTime.now());
            attendanceRepo.save(record);
        }
        return "redirect:/";
    }

    @PostMapping("/clock-out")
    public String clockOut(HttpSession session) {
        Employee emp = employeeRepo.findById(((Employee) session.getAttribute("user")).getId()).get();
        AttendanceRecord todayRecord = emp.getAttendanceRecords().stream()
                .filter(a -> a.getDate().equals(LocalDate.now()))
                .findFirst().orElse(null);

        if (todayRecord != null && todayRecord.getTimeOut() == null) {
            todayRecord.setTimeOut(LocalTime.now());
            todayRecord.calculateHours(); 
            attendanceRepo.save(todayRecord);
        }
        return "redirect:/";
    }

    @PostMapping("/generate-salary")
    public String generateSalary(@RequestParam Long empId, @RequestParam double basic) {
        Employee emp = employeeRepo.findById(empId).get();
        
        SalarySlip slip = new SalarySlip.Builder()
                                .withEmployee(emp)
                                .withBasicPay(basic)
                                .build();
                                
        slip.generate(new LowTaxStrategy()); 
        salaryRepo.save(slip);
        return "redirect:/";
    }

    @PostMapping("/assign-department")
    public String assignDepartment(@RequestParam Long empId, @RequestParam Long deptId) {
        Employee emp = employeeRepo.findById(empId).orElse(null);
        Department dept = departmentRepo.findById(deptId).orElse(null);
        if (emp != null && dept != null) {
            emp.setDepartment(dept);
            employeeRepo.save(emp);
        }
        return "redirect:/";
    }

    @PostMapping("/assign-project")
    @Transactional
    public String assignProject(@RequestParam Long empId, @RequestParam Long projId) {
        Employee emp = employeeRepo.findById(empId).orElse(null);
        Project proj = projectRepo.findById(projId).orElse(null);
        
        if (emp != null && proj != null) {
            if (emp.getProjects() == null) {
                emp.setProjects(new ArrayList<>());
            }
            if (!emp.getProjects().contains(proj)) {
                emp.getProjects().add(proj);
                employeeRepo.save(emp);
            }
        }
        return "redirect:/";
    }

    @PostMapping("/update-project")
    public String updateProjectProgress(@RequestParam Long projId, @RequestParam int progressPercent) {
        Project proj = projectRepo.findById(projId).orElse(null);
        if (proj != null) {
            proj.setProgressPercent(progressPercent);
            projectRepo.save(proj);
        }
        return "redirect:/";
    }

    @PostMapping("/rename-project")
    public String renameProject(@RequestParam Long projId, @RequestParam String newTitle) {
        Project proj = projectRepo.findById(projId).orElse(null);
        if (proj != null && newTitle != null && !newTitle.trim().isEmpty()) {
            proj.setTitle(newTitle.trim());
            projectRepo.save(proj);
        }
        return "redirect:/";
    }

    @PostMapping("/clone-project")
    public String cloneProject(@RequestParam Long projId) {
        Project proj = projectRepo.findById(projId).orElse(null);
        if (proj != null) {
            Project clonedProj = proj.clone();
            projectRepo.save(clonedProj);
        }
        return "redirect:/";
    }

    @PostMapping("/add-department")
    public String addDepartment(@RequestParam String deptName, @RequestParam String location) {
        Department dept = new Department();
        dept.setDeptName(deptName);
        dept.setLocation(location);
        departmentRepo.save(dept);
        return "redirect:/";
    }

    @PostMapping("/delete-department")
    @Transactional
    public String deleteDepartment(@RequestParam Long deptId) {
        Department dept = departmentRepo.findById(deptId).orElse(null);
        if (dept != null) {
            for (Employee emp : employeeRepo.findAll()) {
                if (emp.getDepartment() != null && emp.getDepartment().getId().equals(deptId)) {
                    emp.setDepartment(null);
                    employeeRepo.save(emp);
                }
            }
            departmentRepo.deleteById(deptId);
        }
        return "redirect:/";
    }

    @PostMapping("/add-project")
    public String addProject(@RequestParam String title) {
        Project proj = new Project();
        proj.setTitle(title);
        proj.setProgressPercent(0);
        projectRepo.save(proj);
        return "redirect:/";
    }

    @PostMapping("/delete-project")
    @Transactional
    public String deleteProject(@RequestParam Long projId) {
        Project proj = projectRepo.findById(projId).orElse(null);
        if (proj != null) {
            for (Employee emp : employeeRepo.findAll()) {
                if (emp.getProjects() != null && emp.getProjects().contains(proj)) {
                    emp.getProjects().remove(proj); 
                    employeeRepo.save(emp);
                }
            }
            projectRepo.deleteById(projId);
        }
        return "redirect:/";
    }
}