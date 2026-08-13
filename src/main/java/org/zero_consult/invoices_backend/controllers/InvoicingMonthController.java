package org.zero_consult.invoices_backend.controllers;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.zero_consult.idl.api.InvoicingMonthApi;
import org.zero_consult.invoices_backend.exceptions.CloseMonthConstraintException;
import org.zero_consult.invoices_backend.exceptions.NoneFoundException;
import org.zero_consult.invoices_backend.exceptions.RestControllerException;
import org.zero_consult.invoices_backend.exceptions.ServiceUnavailableException;
import org.zero_consult.invoices_backend.services.InvoiceService;
import org.zero_consult.invoices_backend.services.InvoicingMonthService;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@CrossOrigin(origins = {
        "http://localhost",
        "http://localhost:5174",
        "http://localhost:5175",
        "http://timesheet.localhost",
        "http://timesheet.dev.localhost",
        "http://invoices.localhost",
        "http://invoices.dev.localhost",
})
@RestController
public class InvoicingMonthController implements InvoicingMonthApi {
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final InvoiceService invoiceService;
    private final InvoicingMonthService invoicingMonthService;

    public InvoicingMonthController(InvoiceService invoiceService, InvoicingMonthService invoicingMonthService) {
        this.invoiceService = invoiceService;
        this.invoicingMonthService = invoicingMonthService;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getInvoiceDateOfCustomer(String id, Optional<String> invoiceId) {
        try {
            return ResponseEntity.ok(invoiceService.getInvoiceDate(id, invoiceId).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } catch (NoneFoundException e) {
            return ResponseEntity.status(204).body("");
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> closeCurrentPayslipMonth() {
        try {
            return ResponseEntity.ok(MONTH_FORMAT.format(invoicingMonthService.closeCurrentPayslipMonth()));
        } catch (ServiceUnavailableException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(500), e.getMessage(), e);
        } catch (CloseMonthConstraintException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(406), e.getMessage(), e);
        }
    }

    @Override
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<String> getCurrentPayslipMonth() {
        return ResponseEntity.ok(MONTH_FORMAT.format(invoicingMonthService.getCurrentPayslipMonth()));
    }
}
