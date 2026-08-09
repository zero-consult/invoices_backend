package org.zero_consult.invoices_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class InvoicingMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Enumerated(EnumType.STRING)
    private InvoiceDiscriminator invoiceDiscriminator;
    private LocalDate invoicingMonth;

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

    public LocalDate getInvoicingMonth() {
        return invoicingMonth;
    }

    public void setInvoicingMonth(LocalDate month) {
        this.invoicingMonth = month;
    }
}
