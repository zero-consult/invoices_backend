package org.zero_consult.payslip_backend.controllers;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.zero_consult.idl.api.InvoicingMonthApi;
import org.zero_consult.payslip_backend.exceptions.CloseMonthConstraintException;
import org.zero_consult.payslip_backend.exceptions.RestControllerException;
import org.zero_consult.payslip_backend.exceptions.ServiceUnavailableException;
import org.zero_consult.payslip_backend.services.InvoicingMonthService;

import java.time.format.DateTimeFormatter;

@CrossOrigin(origins = {
        "http://localhost:5174",
        "http://localhost:5175",
        "http://timesheets.localhost",
        "http://timesheets.dev.localhost",
        "http://invoices.localhost",
        "http://invoices.dev.localhost",
})
@RestController
public class InvoicingMonthController implements InvoicingMonthApi {
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final InvoicingMonthService invoicingMonthService;

    public InvoicingMonthController(InvoicingMonthService invoicingMonthService) {
        this.invoicingMonthService = invoicingMonthService;
    }

    @Override
    public ResponseEntity<String> closeCurrentPayslipMonth() {
        try {
            return ResponseEntity.ok(MONTH_FORMAT.format(invoicingMonthService.closeCurrentPayslipMonth()));
        } catch (ServiceUnavailableException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(500), e.getMessage());
        } catch (CloseMonthConstraintException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(406), e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> getCurrentPayslipMonth() {
        return ResponseEntity.ok(MONTH_FORMAT.format(invoicingMonthService.getCurrentPayslipMonth()));
    }
}
