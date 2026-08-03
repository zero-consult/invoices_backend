package org.zero_consult.payslip_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zero_consult.payslip_backend.entities.Payslip;

import java.time.LocalDate;
import java.util.List;

public interface PayslipRepository extends JpaRepository<Payslip, String> {
    List<Payslip> findByMonthBetween(LocalDate from, LocalDate until);

    List<Payslip> findByMonthBetweenAndEmployeeId(LocalDate from, LocalDate until, String employeeId);

    List<Payslip> findByPayslipFileIsNull();
}
