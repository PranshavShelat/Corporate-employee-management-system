package cems;

import cems.models.Employee;
import cems.models.LeaveRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

@Controller
public class CemsController {

    @Autowired
    private EmployeeRepo employeeRepo;
    
    @Autowired
    private LeaveRepo leaveRepo;

    // --- NEW FIX: HYDRATE DATABASE FROM TEXT FILE ---
    // This solves the issue of the database wiping when the server restarts
    private void syncTextFileToDatabase() {
        try {
            File myObj = new File("credentials.txt");
            if (myObj.exists()) {
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    if (data.trim().isEmpty()) continue; // Skip empty lines

                    String[] parts = data.split(",");
                    if (parts.length >= 3) {
                        String fileUser = parts[0].trim();
                        String filePass = parts[1].trim();
                        String fileRole = parts[2].trim();

                        // If it's an employee, make sure they exist in the database
                        if (fileRole.equals("EMPLOYEE")) {
                            boolean exists = false;
                            for (Employee emp : employeeRepo.findAll()) {
                                if (emp.getUsername().equals(fileUser)) {
                                    exists = true;
                                    break;
                                }
                            }
                            
                            // If they are in the text file but NOT the database, recreate them
                            if (!exists) {
                                Employee newEmp = new Employee();
                                newEmp.setName(fileUser); // Use username as fallback name
                                newEmp.setUsername(fileUser);
                                newEmp.setPassword(filePass);
                                employeeRepo.save(newEmp);
                            }
                        }
                    }
                }
                myReader.close();
            }
        } catch (Exception e) {
            System.out.println("Could not sync credentials to DB.");
        }
    }

    @GetMapping("/")
    public String dashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        
        if (role == null) {
            return "index"; 
        }
        
        if (role.equals("ADMIN")) {
            syncTextFileToDatabase(); // Run the sync before loading the table!
            model.addAttribute("employees", employeeRepo.findAll());
            model.addAttribute("leaves", leaveRepo.findAll());
        } else if (role.equals("EMPLOYEE")) {
            Employee emp = (Employee) session.getAttribute("user");
            emp = employeeRepo.findById(emp.getId()).orElse(emp);
            model.addAttribute("employee", emp);
            model.addAttribute("myLeaves", emp.getLeaveRequests());
        }
        return "index"; 
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        String role = null;
        
        try {
            File myObj = new File("credentials.txt");
            if (myObj.exists()) {
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    if (data.trim().isEmpty()) continue; 
                    
                    String[] parts = data.split(","); 
                    
                    if (parts.length >= 3) {
                        String fileUser = parts[0].trim();
                        String filePass = parts[1].trim();
                        String fileRole = parts[2].trim();
                        
                        if (fileUser.equals(username) && filePass.equals(password)) {
                            role = fileRole;
                            break;
                        }
                    }
                }
                myReader.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (role != null) {
            session.setAttribute("role", role);
            
            if (role.equals("ADMIN")) {
                session.setAttribute("name", "Administrator");
                return "redirect:/";
            }
            
            if (role.equals("EMPLOYEE")) {
                syncTextFileToDatabase(); // Run sync so the employee DB record is ready
                for (Employee emp : employeeRepo.findAll()) {
                    if (emp.getUsername().equals(username)) {
                        session.setAttribute("user", emp);
                        session.setAttribute("name", emp.getName());
                        return "redirect:/";
                    }
                }
            }
        }
        
        return "redirect:/?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/add-employee")
    public String addEmployee(@RequestParam String name, 
                              @RequestParam String username, 
                              @RequestParam String password) {
        
        // 1. Save to Database
        Employee emp = new Employee();
        emp.setName(name);
        emp.setUsername(username);
        emp.setPassword(password);
        employeeRepo.save(emp);

        // 2. Append to credentials.txt
        try {
            FileWriter fw = new FileWriter("credentials.txt", true);
            fw.write("\n" + username + "," + password + ",EMPLOYEE");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @PostMapping("/apply-leave")
    public String applyLeave(@RequestParam String reason, HttpSession session) {
        Employee sessionEmp = (Employee) session.getAttribute("user");
        if (sessionEmp != null) {
            Employee emp = employeeRepo.findById(sessionEmp.getId()).orElse(null);
            if (emp != null && emp.getLeaveBalance() > 0) {
                LeaveRequest req = emp.createLeaveRequest(reason);
                leaveRepo.save(req);
                
                emp.setLeaveBalance(emp.getLeaveBalance() - 1);
                employeeRepo.save(emp);
            }
        }
        return "redirect:/";
    }

    @PostMapping("/update-leave")
    public String updateLeaveStatus(@RequestParam Long leaveId, @RequestParam String action) {
        LeaveRequest req = leaveRepo.findById(leaveId).orElse(null);
        if (req != null && req.getStatus().equals("PENDING")) {
            if (action.equals("APPROVE")) {
                req.setStatus("APPROVED");
            } else if (action.equals("REJECT")) {
                req.setStatus("REJECTED");
                Employee emp = req.getEmployee();
                emp.setLeaveBalance(emp.getLeaveBalance() + 1); // Refund balance
                employeeRepo.save(emp);
            }
            leaveRepo.save(req);
        }
        return "redirect:/";
    }
}