package org.zero_consult.invoices_backend.entities;

import jakarta.persistence.*;

@Entity
public class Allowance {
    private Double amount;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String label;
    @ManyToOne(fetch = FetchType.LAZY)
    private Payslip payslip;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Payslip getPayslip() {
        return payslip;
    }

    public void setPayslip(Payslip payslip) {
        this.payslip = payslip;
    }
}
