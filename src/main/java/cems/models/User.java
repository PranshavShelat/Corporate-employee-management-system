package cems.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
// FIX: Using SINGLE_TABLE puts all users in one table, eliminating Foreign Key crashes!
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type") // Tells the DB if the row is an Employee or Manager
@Data
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;
    private String role; // "EMPLOYEE" or "MANAGER"
}