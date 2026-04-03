package cems.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class SalarySlip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String month;
    private double netPay;

    @ManyToOne
    private Employee employee;
}