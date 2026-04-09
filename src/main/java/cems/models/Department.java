package cems.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // NEW: Changed 'name' to 'deptName' to match the Controller logic
    private String deptName; 
    
    // NEW: Added so the Admin can set a location
    private String location; 

    @OneToMany(mappedBy = "department")
    private List<Employee> employees;
}