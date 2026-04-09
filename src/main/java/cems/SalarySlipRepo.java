package cems;

import cems.models.SalarySlip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalarySlipRepo extends JpaRepository<SalarySlip, Long> {
}