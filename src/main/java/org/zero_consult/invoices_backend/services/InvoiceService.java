package org.zero_consult.invoices_backend.services;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.zero_consult.idl.client.ApiException;
import org.zero_consult.idl.client.model.Customer;
import org.zero_consult.idl.client.model.TimesheetEntry;
import org.zero_consult.invoices_backend.entities.Invoice;
import org.zero_consult.invoices_backend.entities.InvoiceStatus;
import org.zero_consult.invoices_backend.exceptions.*;
import org.zero_consult.invoices_backend.repositories.InvoiceRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@Transactional
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoicingMonthService invoicingMonthService;
    private final TimesheetApiService timesheetApiService;
    private final CustomerApiService customerApiService;

    public InvoiceService(InvoiceRepository invoiceRepository, InvoicingMonthService invoicingMonthService, TimesheetApiService timesheetApiService, CustomerApiService customerApiService) {
        this.invoiceRepository = invoiceRepository;
        this.invoicingMonthService = invoicingMonthService;
        this.timesheetApiService = timesheetApiService;
        this.customerApiService = customerApiService;
    }

    public List<Invoice> getAllInvoices(LocalDate from, LocalDate until, Optional<String> customerId) {
        if (customerId.isEmpty()) {
            return invoiceRepository.findByInvoiceDateBetween(from, until);
        } else {
            return invoiceRepository.findByInvoiceDateBetweenAndCustomerId(from, until, customerId.get());
        }
    }

    public double calculateTotal(String customerId, LocalDate from, LocalDate until) throws ServiceUnavailableException {
        List<TimesheetEntry> timesheetEntries;
        Customer customer;
        try {
            timesheetEntries = timesheetApiService.getTimesheetApi().timesheetsList(from, until, null, customerId);
        } catch (ApiException e) {
            throw new ServiceUnavailableException("Can't connect to timesheet backend", e);
        }
        try {
            customer = customerApiService.getCustomerApi().getCustomer(customerId);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return customer.getHiringRatePerHour() * timesheetEntries.stream().mapToDouble(value -> Duration.between(LocalTime.parse(value.getStartTime(), DateTimeFormatter.ofPattern("HH:mm")), LocalTime.parse(value.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"))).toHours()).sum();
    }

    public Invoice generateInvoice(Invoice entity) throws InvalidGenerationException, ServiceUnavailableException {
        if(getCustomersWithConceptInvoices().contains(entity.getCustomerId())) {
            throw new InvalidGenerationException("Can't create another invoice for customer who already has a concept");
        }
        entity.setInvoiceDate(LocalDate.now());
        if (invoicingMonthService.getCurrentPayslipMonth().isBefore(entity.getInvoiceUntil()) || invoicingMonthService.getCurrentPayslipMonth().isEqual(entity.getInvoiceUntil())) {
            throw new InvalidGenerationException("Can't invoice until the payslip month has been closed");
        }
        LocalDate invoiceFrom;
        try {
            invoiceFrom = getInvoiceDate(entity.getCustomerId(), Optional.empty());
        } catch (NoneFoundException e) {
            invoiceFrom = LocalDate.now().minusYears(5);
        }
        if(invoiceFrom.isAfter(entity.getInvoiceUntil()) || invoiceFrom.isEqual(entity.getInvoiceUntil())) {
            throw new InvalidGenerationException("Need a range to be able to create an invoice");
        }
        entity.setTotalWithoutVat(calculateTotal(entity.getCustomerId(), invoiceFrom, entity.getInvoiceUntil()));
        return saveInvoiceWithUpdatedInvoiceNumber(entity);
    }

    private synchronized @NonNull Invoice saveInvoiceWithUpdatedInvoiceNumber(Invoice entity) {
        if(entity.getStatus() == InvoiceStatus.SENT || entity.getStatus() == InvoiceStatus.PAID) {
            Integer lastInvoiceNumberOfInvoiceYear = invoiceRepository.findLastInvoiceNumberOfInvoiceYear(entity.getInvoiceDate().getYear());
            if(lastInvoiceNumberOfInvoiceYear != null) {
                entity.setInvoiceNumber(lastInvoiceNumberOfInvoiceYear + 1);
            } else {
                entity.setInvoiceNumber(1);
            }
        }
        return invoiceRepository.save(entity);
    }

    public Invoice updateInvoice(String id, Invoice entity) throws EntityNotFoundException, ServiceUnavailableException {
        Optional<Invoice> invoiceById = invoiceRepository.findById(id);
        if (invoiceById.isEmpty()) {
            throw new EntityNotFoundException("Invoice not found");
        }
        Invoice invoice = invoiceById.get();
        invoice.setNotes(entity.getNotes());
        invoice.setInvoiceFile(entity.getInvoiceFile());
        boolean wasConcept = invoice.getStatus() == InvoiceStatus.CONCEPT;
        if (wasConcept) {
            invoice.setInvoiceDate(LocalDate.now());
            invoice.setVatPercent(entity.getVatPercent());
            LocalDate invoiceFrom;
            try {
                invoiceFrom = getInvoiceDate(entity.getCustomerId(), Optional.of(id));
            } catch (NoneFoundException e) {
                invoiceFrom = LocalDate.now().minusYears(5);
            }
            invoice.setTotalWithoutVat(calculateTotal(invoice.getCustomerId(), invoiceFrom, entity.getInvoiceUntil()));
            invoice.setInvoiceFile(null);
        } else if (invoice.getStatus() == InvoiceStatus.SENT) {
            if (entity.getStatus() == InvoiceStatus.PAID) {
                invoice.setStatus(entity.getStatus());
            }
        }
        if(wasConcept) {
            invoice.setStatus(entity.getStatus());
            return saveInvoiceWithUpdatedInvoiceNumber(invoice);
        } else {
            return invoiceRepository.save(invoice);
        }
    }

    public Invoice getInvoice(String id) throws EntityNotFoundException {
        return invoiceRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("TimesheetEntry not found"));
    }

    public void deleteInvoice(String id) throws EntityNotFoundException, InvalidDeletionException {
        Invoice invoice = getInvoice(id);
        if(invoice.getStatus() != InvoiceStatus.CONCEPT) {
            throw new InvalidDeletionException("Invoice with status sent or paid can no longer be removed");
        }
        invoiceRepository.deleteById(id);
    }

    public List<Invoice> getEmptyInvoiceFiles() {
        return invoiceRepository.findByInvoiceFileIsNull();
    }

    public LocalDate getInvoiceDate(String customerId, Optional<String> id) throws NoneFoundException {
        Invoice invoice;
        if(id.isEmpty()) {
            invoice = invoiceRepository.findFirstByCustomerIdOrderByInvoiceDateDesc(customerId);
        } else {
            invoice = invoiceRepository.findFirstByCustomerIdAndIdNotOrderByInvoiceDateDesc(customerId, id.get());
        }
        if (invoice == null) {
            throw new NoneFoundException("No invoice found for customer");
        }
        return invoice.getInvoiceUntil();
    }

    public List<String> getCustomersWithConceptInvoices() {
        return invoiceRepository.findCustomersByStatus(InvoiceStatus.CONCEPT);
    }
}
