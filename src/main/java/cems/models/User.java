package cems.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users") // <--- THIS IS THE FIX!
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;
    private String role; // "EMPLOYEE" or "MANAGER"
}