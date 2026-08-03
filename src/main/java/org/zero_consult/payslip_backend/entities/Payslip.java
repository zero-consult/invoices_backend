package org.zero_consult.payslip_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Payslip {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "payslip")
    private List<Allowance> allowances;
    private String employeeId;
    private Double grossSalary;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private LocalDate payslipMonth;
    private String payslipFile;
    private Double taxRate;

    public List<Allowance> getAllowances() {
        return allowances;
    }

    public void setAllowances(List<Allowance> allowances) {
        this.allowances = allowances;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Double getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(Double grossSalary) {
        this.grossSalary = grossSalary;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getPayslipMonth() {
        return payslipMonth;
    }

    public void setPayslipMonth(LocalDate month) {
        this.payslipMonth = month;
    }

    public String getPayslipFile() {
        return payslipFile;
    }

    public void setPayslipFile(String payslipFile) {
        this.payslipFile = payslipFile;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }
}
