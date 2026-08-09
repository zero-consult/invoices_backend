package org.zero_consult.invoices_backend.controllers;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.zero_consult.idl.api.InvoiceApi;
import org.zero_consult.idl.model.Invoice;
import org.zero_consult.invoices_backend.exceptions.*;
import org.zero_consult.invoices_backend.mappers.InvoiceMapper;
import org.zero_consult.invoices_backend.services.InvoiceService;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {
        "http://localhost:5174",
        "http://localhost:5175",
        "http://timesheet.localhost",
        "http://timesheet.dev.localhost",
        "http://invoices.localhost",
        "http://invoices.dev.localhost",
})
@RestController
public class InvoiceController implements InvoiceApi {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Override
    public ResponseEntity<Void> deleteInvoice(String id) {
        try {
            invoiceService.deleteInvoice(id);
        } catch (EntityNotFoundException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(404), e.getMessage(), e);
        } catch (InvalidDeletionException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(406), e.getMessage(), e);
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Invoice> generateInvoice(Invoice invoice) {
        try {
            return ResponseEntity.ok(InvoiceMapper.toIdl(invoiceService.generateInvoice(InvoiceMapper.toEntity(invoice))));
        } catch (InvalidGenerationException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(406), e.getMessage(), e);
        } catch (ServiceUnavailableException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(500), e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<Invoice> getInvoice(String id) {
        try {
            return ResponseEntity.ok(InvoiceMapper.toIdl(invoiceService.getInvoice(id)));
        } catch (EntityNotFoundException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(404), e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<List<Invoice>> invoiceList(LocalDate from, LocalDate until, Optional<String> customerId) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(from, until, customerId).stream().map(InvoiceMapper::toIdl).toList());
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getInvoiceFile(String id) {
        try {
            org.zero_consult.invoices_backend.entities.Invoice invoice = invoiceService.getInvoice(id);
            StreamingResponseBody responseBody = outputStream -> {
                try (InputStream payslipFile = Files.newInputStream(Paths.get(invoice.getInvoiceFile()))) {
                    IOUtils.copy(payslipFile, outputStream);
                }
            };
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice.pdf")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(responseBody);
        } catch (EntityNotFoundException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(404), e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<Invoice> editInvoice(String id, Invoice invoice) {
        try {
            return ResponseEntity.ok(InvoiceMapper.toIdl(invoiceService.updateInvoice(id, InvoiceMapper.toEntity(invoice))));
        } catch (EntityNotFoundException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(404), e.getMessage(), e);
        } catch (ServiceUnavailableException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(500), e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<List<String>> getCustomersWithConceptInvoices() {
        return ResponseEntity.ok(invoiceService.getCustomersWithConceptInvoices());
    }
}
