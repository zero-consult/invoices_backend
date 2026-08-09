package org.zero_consult.invoices_backend.controllers;

import org.apache.commons.io.IOUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.zero_consult.idl.api.PayslipsApi;
import org.zero_consult.idl.model.Payslip;
import org.zero_consult.invoices_backend.exceptions.EntityNotFoundException;
import org.zero_consult.invoices_backend.exceptions.InvalidGenerationException;
import org.zero_consult.invoices_backend.exceptions.RestControllerException;
import org.zero_consult.invoices_backend.exceptions.ServiceUnavailableException;
import org.zero_consult.invoices_backend.mappers.PayslipMapper;
import org.zero_consult.invoices_backend.services.PayslipService;

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
public class PayslipController implements PayslipsApi {
    private final PayslipService payslipService;

    public PayslipController(PayslipService payslipService) {
        this.payslipService = payslipService;
    }

    @Override
    public ResponseEntity<Payslip> generatePayslip(Payslip payslip) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(PayslipMapper.toIdl(payslipService.generatePayslip(PayslipMapper.toEntity(payslip))));
        } catch (EntityNotFoundException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(404), e.getMessage(), e);
        } catch (InvalidGenerationException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(406), e.getMessage(), e);
        } catch (ServiceUnavailableException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(503), e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<List<Payslip>> payslipsList(LocalDate from, LocalDate until, Optional<String> employeeId) {
        return ResponseEntity.ok(
                payslipService
                        .getAllPayslips(from, until, employeeId)
                        .stream()
                        .map(PayslipMapper::toIdl)
                        .toList());
    }

    @Override
    public ResponseEntity<Payslip> getPayslip(String id) {
        try {
            return ResponseEntity.ok(PayslipMapper.toIdl(payslipService.getPayslip(id)));
        } catch (EntityNotFoundException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(404), e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<Void> deletePayslip(String id) {
        payslipService.deletePayslip(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getPayslipFile(String id) {
        try {
            org.zero_consult.invoices_backend.entities.Payslip payslip = payslipService.getPayslip(id);
            StreamingResponseBody responseBody = outputStream -> {
                try (InputStream payslipFile = Files.newInputStream(Paths.get(payslip.getPayslipFile()))) {
                    IOUtils.copy(payslipFile, outputStream);
                }
            };
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip.pdf")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(responseBody);
        } catch (EntityNotFoundException e) {
            throw new RestControllerException(HttpStatusCode.valueOf(404), e.getMessage(), e);
        }
    }
}
