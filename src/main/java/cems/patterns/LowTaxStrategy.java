package cems.patterns;

public class LowTaxStrategy implements TaxStrategy {
    @Override
    public double calculateTax(double basicPay) {
        return basicPay * 0.10; // 10% tax for lower brackets
    }
}