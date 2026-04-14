package cems.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import cems.patterns.TaxStrategy;

@Entity
public class SalarySlip {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String month;
    private double basicPay;
    private double tax;
    private double netPay;

    @ManyToOne
    private Employee employee;

    // STRATEGY PATTERN (From your GitHub)
    public void generate(TaxStrategy strategy) {
        this.tax = strategy.calculateTax(this.basicPay);
        this.netPay = this.basicPay - this.tax;
    }

    // BUILDER PATTERN (New addition)
    public static class Builder {
        private SalarySlip slip = new SalarySlip();

        public Builder() {
            slip.month = LocalDate.now().getMonth().toString();
        }

        public Builder withEmployee(Employee emp) {
            slip.employee = emp;
            return this;
        }

        public Builder withBasicPay(double basicPay) {
            slip.basicPay = basicPay;
            return this;
        }

        public SalarySlip build() {
            return slip;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public double getBasicPay() { return basicPay; }
    public void setBasicPay(double basicPay) { this.basicPay = basicPay; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }
    public double getNetPay() { return netPay; }
    public void setNetPay(double netPay) { this.netPay = netPay; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
}