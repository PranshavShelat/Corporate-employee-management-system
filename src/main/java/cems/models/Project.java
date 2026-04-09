package cems.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    // NEW: Added so the Controller can track project completion
    private int progressPercent; 

    // NEW: Added 'mappedBy' so MySQL doesn't create duplicate tracking tables
    @ManyToMany(mappedBy = "projects")
    private List<Employee> teamMembers;
}