package org.zero_consult.invoices_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zero_consult.invoices_backend.entities.InvoiceDiscriminator;
import org.zero_consult.invoices_backend.entities.InvoicingMonth;

import java.util.Optional;

public interface InvoicingMonthRepository extends JpaRepository<InvoicingMonth, String> {
    public Optional<InvoicingMonth> findByInvoiceDiscriminatorEquals(InvoiceDiscriminator invoiceDiscriminator);
}
