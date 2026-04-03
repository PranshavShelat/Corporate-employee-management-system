package cems.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status = "PENDING";

    @ManyToOne
    private Employee employee;
}