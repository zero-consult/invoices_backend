package org.zero_consult.invoices_backend.cron;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zero_consult.idl.client.ApiException;
import org.zero_consult.idl.client.model.Customer;
import org.zero_consult.idl.client.model.TimesheetEntry;
import org.zero_consult.invoices_backend.configuration.CustomProperties;
import org.zero_consult.invoices_backend.entities.Invoice;
import org.zero_consult.invoices_backend.exceptions.EntityNotFoundException;
import org.zero_consult.invoices_backend.exceptions.NoneFoundException;
import org.zero_consult.invoices_backend.exceptions.ServiceUnavailableException;
import org.zero_consult.invoices_backend.services.CustomerApiService;
import org.zero_consult.invoices_backend.services.InvoiceService;
import org.zero_consult.invoices_backend.services.TimesheetApiService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class InvoicePdfGenerator {

    private final InvoiceService invoiceService;
    private final CustomProperties customProperties;
    private final CustomerApiService customerApiService;
    private final TimesheetApiService timesheetApiService;

    public InvoicePdfGenerator(InvoiceService invoiceService, CustomProperties customProperties, CustomerApiService customerApiService, TimesheetApiService timesheetApiService) {
        this.invoiceService = invoiceService;
        this.customProperties = customProperties;
        this.customerApiService = customerApiService;
        this.timesheetApiService = timesheetApiService;
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void generateInvoicePdfs() throws ApiException, ServiceUnavailableException, JRException, IOException {
        for (Invoice invoice : invoiceService.getEmptyInvoiceFiles()) {
            String payslipFile = customProperties.getInvoicesDir() + "/invoices/" + invoice.getId() + ".pdf";
            Customer customer = customerApiService.getCustomerApi().getCustomer(invoice.getCustomerId());
            LocalDate invoiceFrom;
            try {
                invoiceFrom = invoiceService.getInvoiceDate(customer.getId(), Optional.of(invoice.getId()));
            } catch (NoneFoundException e) {
                invoiceFrom = LocalDate.now().minusYears(5);
            }
            List<TimesheetEntry> timesheetEntries = timesheetApiService.getTimesheetApi().timesheetsList(invoiceFrom, invoice.getInvoiceUntil(), null, customer.getId());
            double hours = timesheetEntries.stream().mapToDouble(value -> Duration.between(LocalTime.parse(value.getStartTime(), DateTimeFormatter.ofPattern("HH:mm")), LocalTime.parse(value.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"))).toHours()).sum();
            generateInvoicePdf(invoice, payslipFile, customer, hours);
            invoice.setInvoiceFile(payslipFile);
            try {
                invoiceService.updateInvoice(invoice.getId(), invoice);
            } catch (EntityNotFoundException ignore) {
            }
        }
    }

    private static String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("nl-BE")).format(amount);
    }

    private static void generateInvoicePdf(Invoice invoice, String invoiceFile, Customer customer, double hours) throws JRException, IOException {
        try (InputStream invoiceReportStream = InvoicePdfGenerator.class.getClassLoader().getResourceAsStream("jrxml/invoice.jrxml");
             OutputStream invoicePdfOutputStream = Files.newOutputStream(Path.of(invoiceFile))) {
            JasperReport invoiceReport = JasperCompileManager.compileReport(invoiceReportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("CUSTOMER_NAME", customer.getCompanyName());
            parameters.put("CUSTOMER_ADDRESS", customer.getCity());
            parameters.put("INVOICE_NUMBER", invoice.getInvoiceDate().getYear() + "-" + invoice.getInvoiceNumber());
            parameters.put("INVOICE_DATE", invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            parameters.put("EXPIRE_DATE", invoice.getExpireDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            parameters.put("INVOICE_HOURS", "" + hours);
            parameters.put("HIRING_RATE", formatCurrency(customer.getHiringRatePerHour()));
            parameters.put("VAT_PERCENT", invoice.getVatPercent() + "%");
            parameters.put("VAT", formatCurrency((customer.getHiringRatePerHour() * hours) * invoice.getVatPercent() / 100));
            parameters.put("TOTAL_EXCL_VAT", formatCurrency(invoice.getTotalWithoutVat()));
            parameters.put("TOTAL", formatCurrency((customer.getHiringRatePerHour() * hours) * (invoice.getVatPercent() + 100) / 100));
            parameters.put("NOTES", invoice.getNotes());
            JasperPrint jasperPrint = JasperFillManager.fillReport(invoiceReport, parameters, new JREmptyDataSource());

            JRPdfExporter exporter = new JRPdfExporter();

            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(invoicePdfOutputStream));

            exporter.exportReport();
        }
    }
}
