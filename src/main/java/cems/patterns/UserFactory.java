package cems.patterns;

import cems.models.Employee;
import cems.models.Manager;
import cems.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {
    // FACTORY PATTERN: Decides which subclass to instantiate based on input
    public User createUser(String role) {
        if ("MANAGER".equalsIgnoreCase(role)) {
            return new Manager();
        }
        return new Employee(); // Default
    }
}