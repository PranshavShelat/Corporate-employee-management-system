package cems.services;

import cems.EmployeeRepo;
import cems.models.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepo employeeRepo;

    // Business Logic: Save to MySQL
    public void createEmployee(String name, String username, String password) {
        Employee emp = new Employee();
        emp.setName(name);
        emp.setUsername(username);
        emp.setPassword(password);
        employeeRepo.save(emp);
    }

    // Business Logic: Authenticate User
    public Employee authenticate(String username, String password) {
        for (Employee emp : employeeRepo.findAll()) {
            if (emp.getUsername().equals(username) && emp.getPassword().equals(password)) {
                return emp;
            }
        }
        return null;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepo.findById(id).orElse(null);
    }

    public void saveEmployee(Employee emp) {
        employeeRepo.save(emp);
    }
}