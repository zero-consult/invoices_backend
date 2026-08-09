package org.zero_consult.invoices_backend.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Invoice {

    private String customerId;
    private LocalDate expireDate;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private LocalDate invoiceDate;
    private String invoiceFile;
    private Integer invoiceNumber;
    private LocalDate invoiceUntil;
    private String notes;
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
    private Double totalWithoutVat;
    private Double vatPercent;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceFile() {
        return invoiceFile;
    }

    public void setInvoiceFile(String invoiceFile) {
        this.invoiceFile = invoiceFile;
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getInvoiceUntil() {
        return invoiceUntil;
    }

    public void setInvoiceUntil(LocalDate invoiceUntil) {
        this.invoiceUntil = invoiceUntil;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Double getTotalWithoutVat() {
        return totalWithoutVat;
    }

    public void setTotalWithoutVat(Double totalWithoutVat) {
        this.totalWithoutVat = totalWithoutVat;
    }

    public Double getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(Double vatPercent) {
        this.vatPercent = vatPercent;
    }
}
