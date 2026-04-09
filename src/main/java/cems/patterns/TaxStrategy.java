package cems.patterns;

// STRATEGY PATTERN: Defines interchangeable tax calculation algorithms
public interface TaxStrategy {
    double calculateTax(double basicPay);
}