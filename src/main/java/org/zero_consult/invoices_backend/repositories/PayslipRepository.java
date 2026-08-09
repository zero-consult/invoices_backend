package org.zero_consult.invoices_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zero_consult.invoices_backend.entities.Payslip;

import java.time.LocalDate;
import java.util.List;

public interface PayslipRepository extends JpaRepository<Payslip, String> {
    List<Payslip> findByPayslipMonthBetween(LocalDate from, LocalDate until);

    List<Payslip> findByPayslipMonthBetweenAndEmployeeId(LocalDate from, LocalDate until, String employeeId);

    List<Payslip> findByPayslipFileIsNull();
}
