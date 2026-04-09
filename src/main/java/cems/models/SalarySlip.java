package cems.models;

import cems.patterns.TaxStrategy;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class SalarySlip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slipId;
    private String month;
    private double basicPay;
    private double tax;
    private double netPay;

    @ManyToOne
    private Employee employee;

    // GRASP PRINCIPLE 3: INFORMATION EXPERT (Member 2)
    // The slip generates its own totals using the Strategy Pattern
    public void generate(TaxStrategy strategy) {
        this.month = LocalDate.now().getMonth().toString();
        this.tax = strategy.calculateTax(this.basicPay);
        this.netPay = this.basicPay - this.tax;
    }
}