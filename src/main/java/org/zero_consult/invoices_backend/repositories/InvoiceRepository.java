package org.zero_consult.invoices_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zero_consult.invoices_backend.entities.Invoice;
import org.zero_consult.invoices_backend.entities.InvoiceStatus;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    List<Invoice> findByInvoiceDateBetween(LocalDate from, LocalDate until);

    List<Invoice> findByInvoiceDateBetweenAndCustomerId(LocalDate from, LocalDate until, String customerId);

    List<Invoice> findByInvoiceFileIsNull();

    Invoice findFirstByCustomerIdOrderByInvoiceDateDesc(String customerId);

    Invoice findFirstByCustomerIdAndIdNotOrderByInvoiceDateDesc(String customerId, String id);

    @Query("SELECT max(inv.invoiceNumber) FROM Invoice inv WHERE YEAR(inv.invoiceDate) = ?1")
    Integer findLastInvoiceNumberOfInvoiceYear(Integer year);

    @Query("SELECT DISTINCT inv.customerId FROM Invoice inv WHERE inv.status = ?1")
    List<String> findCustomersByStatus(InvoiceStatus status);
}
