package org.zero_consult.payslip_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class InvoicingMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Enumerated(EnumType.STRING)
    private InvoiceDiscriminator invoiceDiscriminator;
    private LocalDate month;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InvoiceDiscriminator getInvoiceDiscriminator() {
        return invoiceDiscriminator;
    }

    public void setInvoiceDiscriminator(InvoiceDiscriminator invoiceDiscriminator) {
        this.invoiceDiscriminator = invoiceDiscriminator;
    }

    public LocalDate getMonth() {
        return month;
    }

    public void setMonth(LocalDate month) {
        this.month = month;
    }
}
