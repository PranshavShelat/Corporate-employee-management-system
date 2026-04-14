package cems.models;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("MANAGER")
@Data
@EqualsAndHashCode(callSuper = true)
public class Manager extends Employee {
    private int teamSize;
    // GRASP PRINCIPLE 2: POLYMORPHISM (Member 4) 
    // Manager inherits Employee but can have overridden behaviors if needed.
}