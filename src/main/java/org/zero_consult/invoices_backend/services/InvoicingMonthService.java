package org.zero_consult.invoices_backend.services;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.zero_consult.idl.client.ApiException;
import org.zero_consult.idl.client.model.TimesheetEntry;
import org.zero_consult.idl.client.model.TimesheetStatus;
import org.zero_consult.invoices_backend.configuration.MockableClock;
import org.zero_consult.invoices_backend.entities.InvoiceDiscriminator;
import org.zero_consult.invoices_backend.entities.InvoicingMonth;
import org.zero_consult.invoices_backend.entities.Payslip;
import org.zero_consult.invoices_backend.exceptions.CloseMonthConstraintException;
import org.zero_consult.invoices_backend.exceptions.ServiceUnavailableException;
import org.zero_consult.invoices_backend.repositories.InvoicingMonthRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@Transactional
public class InvoicingMonthService {

    private final MockableClock mockableClock;
    private final InvoicingMonthRepository invoicingMonthRepository;
    private final TimesheetApiService timesheetApiService;
    private final PayslipService payslipService;

    public InvoicingMonthService(MockableClock mockableClock, InvoicingMonthRepository invoicingMonthRepository, TimesheetApiService timesheetApiService, PayslipService payslipService) {
        this.mockableClock = mockableClock;
        this.invoicingMonthRepository = invoicingMonthRepository;
        this.timesheetApiService = timesheetApiService;
        this.payslipService = payslipService;
    }

    public LocalDate getCurrentPayslipMonth() {
        Optional<InvoicingMonth> byInvoiceDiscriminatorEqual = invoicingMonthRepository.findByInvoiceDiscriminatorEquals(InvoiceDiscriminator.PAYSLIP);
        if(byInvoiceDiscriminatorEqual.isPresent()) {
            return byInvoiceDiscriminatorEqual.get().getInvoicingMonth();
        } else {
            InvoicingMonth initialInvoicingMonth = new InvoicingMonth();
            initialInvoicingMonth.setInvoicingMonth(LocalDate.now(mockableClock.clock()).minusMonths(1).withDayOfMonth(1));
            initialInvoicingMonth.setInvoiceDiscriminator(InvoiceDiscriminator.PAYSLIP);
            invoicingMonthRepository.save(initialInvoicingMonth);
            return initialInvoicingMonth.getInvoicingMonth();
        }
    }

    public LocalDate closeCurrentPayslipMonth() throws ServiceUnavailableException, CloseMonthConstraintException {
        LocalDate payslipMonth = getCurrentPayslipMonth();
        try {
            List<TimesheetEntry> timesheetEntries = timesheetApiService.getTimesheetApi().timesheetsList(payslipMonth, payslipMonth.plusMonths(1), null, null);
            List<String> employeeIds = new ArrayList<>();
            for(TimesheetEntry timesheetEntry : timesheetEntries) {
                if(timesheetEntry.getStatus().equals(TimesheetStatus.IN_PROGRESS)) {
                    throw new CloseMonthConstraintException("server.error.in_progress_entries");
                } else {
                    if(!employeeIds.contains(timesheetEntry.getEmployeeId())) {
                        List<Payslip> allPayslips = payslipService.getAllPayslips(timesheetEntry.getDate().withDayOfMonth(1), timesheetEntry.getDate().plusMonths(1).minusDays(1), Optional.of(timesheetEntry.getEmployeeId()));
                        if(allPayslips.isEmpty()) {
                            throw new CloseMonthConstraintException("server.error.aproved_entries_without_payslip");
                        }
                        employeeIds.add(timesheetEntry.getEmployeeId());
                    }
                }
            }
        } catch (ApiException e) {
            throw new ServiceUnavailableException("Can't connect to timesheet backend");
        }


        payslipMonth = payslipMonth.plusMonths(1);
        Optional<InvoicingMonth> byInvoiceDiscriminatorEqual = invoicingMonthRepository.findByInvoiceDiscriminatorEquals(InvoiceDiscriminator.PAYSLIP);
        byInvoiceDiscriminatorEqual.get().setInvoicingMonth(payslipMonth);
        invoicingMonthRepository.save(byInvoiceDiscriminatorEqual.get());
        return payslipMonth;
    }
}
