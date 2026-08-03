package org.zero_consult.payslip_backend.services;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.zero_consult.idl.client.ApiException;
import org.zero_consult.idl.client.model.Employee;
import org.zero_consult.idl.client.model.TimesheetEntry;
import org.zero_consult.idl.client.model.TimesheetStatus;
import org.zero_consult.payslip_backend.entities.Allowance;
import org.zero_consult.payslip_backend.entities.Payslip;
import org.zero_consult.payslip_backend.exceptions.EntityNotFoundException;
import org.zero_consult.payslip_backend.exceptions.InvalidGenerationException;
import org.zero_consult.payslip_backend.exceptions.ServiceUnavailableException;
import org.zero_consult.payslip_backend.repositories.PayslipRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@Transactional
public class PayslipService {
    private final PayslipRepository payslipRepository;
    private final TimesheetApiService timesheetApiService;
    private final EmployeeApiService employeeApiService;

    public PayslipService(PayslipRepository payslipRepository, TimesheetApiService timesheetApiService, EmployeeApiService employeeApiService) {
        this.payslipRepository = payslipRepository;
        this.timesheetApiService = timesheetApiService;
        this.employeeApiService = employeeApiService;
    }

    public List<Payslip> getAllPayslips(LocalDate from, LocalDate until, Optional<String> employeeId) {
        if(employeeId.isEmpty()) {
            return payslipRepository.findByPayslipMonthBetween(from, until);
        } else {
            return payslipRepository.findByPayslipMonthBetweenAndEmployeeId(from, until, employeeId.get());
        }
    }

    public Payslip generatePayslip(Payslip entity) throws EntityNotFoundException, ServiceUnavailableException, InvalidGenerationException {
        LocalDate monthFrom = entity.getPayslipMonth().withDayOfMonth(1);
        LocalDate monthUntil = monthFrom.plusMonths(1).minusDays(1);
        List<TimesheetEntry> timesheetEntries;
        Employee employee;
        try {
            timesheetEntries = new ArrayList<>(timesheetApiService.getTimesheetApi().timesheetsList(monthFrom, monthUntil, entity.getEmployeeId()));
            employee = employeeApiService.getEmployeeApi().getEmployee(entity.getEmployeeId());
        } catch (ApiException e) {
            throw new ServiceUnavailableException("Can't connect to timesheets backend", e);
        }
        List<TimesheetEntry> inProgressEntries = timesheetEntries.stream().filter((entry) -> entry.getStatus().equals(TimesheetStatus.IN_PROGRESS)).toList();
        if(!inProgressEntries.isEmpty()) {
            throw new InvalidGenerationException("server.error.in_progress_timesheet");
        }
        long sum = timesheetEntries.stream().mapToLong(entry -> ChronoUnit.MINUTES.between(LocalTime.parse(entry.getStartTime(), DateTimeFormatter.ofPattern("HH:mm")), LocalTime.parse(entry.getEndTime(), DateTimeFormatter.ofPattern("HH:mm")))).sum();
        if(sum != (calculateWorkHoursForPeriod(monthFrom, monthUntil, employee.getStartDate()) * 60L)) {
            throw new InvalidGenerationException("server.error.invalid_timesheets_amount");
        }
        return payslipRepository.save(entity);
    }

    private int calculateWorkHoursForPeriod(LocalDate monthFrom, LocalDate monthEnd, Long startDate) {
        int workhoursForMonth = 0;
        LocalDate startDateEmployee = LocalDate.ofInstant(new Date(startDate).toInstant(), ZoneId.systemDefault());
        while(monthFrom.isBefore(monthEnd) || monthFrom.isEqual(monthEnd)) {
            if((monthFrom.isAfter(startDateEmployee) || monthFrom.isEqual(startDateEmployee)) && !(monthFrom.getDayOfWeek().equals(DayOfWeek.SATURDAY) || monthFrom.getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                workhoursForMonth += 8;
            }
            monthFrom = monthFrom.plusDays(1);
        }
        return workhoursForMonth;
    }

    public Payslip updatePayslip(String id, Payslip entity) throws EntityNotFoundException {
        Optional<Payslip> payslipById = payslipRepository.findById(id);
        if (payslipById.isEmpty()) {
            throw new EntityNotFoundException("Payslip not found");
        }
        Payslip payslip = payslipById.get();
        List<Allowance> allowanceClones = new ArrayList<>();
        List<Allowance> allowances = entity.getAllowances();
        for(Allowance allowance : allowances) {
            Allowance allowanceCopy = new Allowance();
            allowanceCopy.setId(allowance.getId());
            allowanceCopy.setAmount(allowance.getAmount());
            allowanceCopy.setLabel(allowance.getLabel());
            allowanceCopy.setPayslip(payslip);
            allowanceClones.add(allowanceCopy);
        }
        payslip.setAllowances(allowanceClones);
        payslip.setEmployeeId(entity.getEmployeeId());
        payslip.setGrossSalary(entity.getGrossSalary());
        payslip.setPayslipMonth(entity.getPayslipMonth());
        payslip.setPayslipFile(entity.getPayslipFile());
        payslip.setTaxRate(entity.getTaxRate());
        return payslipRepository.save(payslip);
    }

    public Payslip getPayslip(String id) throws EntityNotFoundException {
        return payslipRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("TimesheetEntry not found"));
    }

    public void deletePayslip(String id) {
        payslipRepository.deleteById(id);
    }

    public List<Payslip> getEmptyPayslipFiles() {
        return payslipRepository.findByPayslipFileIsNull();
    }
}
