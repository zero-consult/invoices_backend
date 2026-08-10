package org.zero_consult.invoices_backend.cron;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zero_consult.idl.client.ApiException;
import org.zero_consult.idl.client.model.Employee;
import org.zero_consult.idl.client.model.TimesheetEntry;
import org.zero_consult.idl.client.model.TimesheetType;
import org.zero_consult.invoices_backend.configuration.CustomProperties;
import org.zero_consult.invoices_backend.entities.Payslip;
import org.zero_consult.invoices_backend.exceptions.EntityNotFoundException;
import org.zero_consult.invoices_backend.services.EmployeeApiService;
import org.zero_consult.invoices_backend.services.PayslipService;
import org.zero_consult.invoices_backend.services.TimesheetApiService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class PayslipPdfGenerator {
    private final PayslipService payslipService;
    private final EmployeeApiService employeeApiService;
    private final TimesheetApiService timesheetApiService;
    private final CustomProperties customProperties;

    private record Allowance(String days, String unit, String percent, String amount, String description) {
    }

    public PayslipPdfGenerator(PayslipService payslipService, EmployeeApiService employeeApiService, TimesheetApiService timesheetApiService, CustomProperties customProperties) {
        this.payslipService = payslipService;
        this.employeeApiService = employeeApiService;
        this.timesheetApiService = timesheetApiService;
        this.customProperties = customProperties;
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void generatePayslipPdfs() throws ApiException {
        for (Payslip payslip : payslipService.getEmptyPayslipFiles()) {
            String payslipFile = customProperties.getInvoicesDir() + "/payslips/" + payslip.getId() + ".pdf";
            List<TimesheetEntry> timesheetEntries = timesheetApiService.getTimesheetApi().timesheetsList(payslip.getPayslipMonth(), payslip.getPayslipMonth().plusMonths(1).minusDays(1), payslip.getEmployeeId(), null);
            Employee employee = employeeApiService.getEmployeeApi().getEmployee(payslip.getEmployeeId());
            generatePayslipPdf(payslip, payslipFile, employee, timesheetEntries);
            payslip.setPayslipFile(payslipFile);
            try {
                payslipService.updatePayslip(payslip.getId(), payslip);
            } catch (EntityNotFoundException ignore) {
            }
        }
    }

    public static void main(String[] args) {
        Payslip payslip = new Payslip();
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setFunctionTitle("Software developer");
        payslip.setGrossSalary(4000d);
        payslip.setTaxRate(37.5d);
        ArrayList<org.zero_consult.invoices_backend.entities.Allowance> allowances = new ArrayList<>();
        org.zero_consult.invoices_backend.entities.Allowance allowance = new org.zero_consult.invoices_backend.entities.Allowance();
        allowance.setAmount(-150d);
        allowance.setLabel("Meal vouchers");
        allowances.add(allowance);
        payslip.setAllowances(allowances);
        payslip.setPayslipMonth(LocalDate.of(2026, 06, 01));
        List<TimesheetEntry> timesheetEntries = createMonthTimeEntries(payslip.getPayslipMonth());
        generatePayslipPdf(payslip, "c:/invoices/1.pdf", employee, timesheetEntries);
    }

    private static @NonNull List<TimesheetEntry> createMonthTimeEntries(LocalDate month) {
        List<TimesheetEntry> timesheetEntries = new ArrayList<>();
        LocalDate day = month;
        while (day.getMonth() == month.getMonth()) {
            if (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY) {
                TimesheetEntry entry = new TimesheetEntry();
                entry.setDate(day);
                entry.setStartTime("09:00");
                entry.setEndTime("17:00");
                entry.setType(TimesheetType.WORK);
                timesheetEntries.add(entry);
            }
            day = day.plusDays(1);
        }
        return timesheetEntries;
    }

    private static String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("nl-BE")).format(amount);
    }

    private static void generatePayslipPdf(Payslip payslip, String payslipFile, Employee employee, List<TimesheetEntry> timesheetEntries) {
        try (InputStream payslipReportStream = PayslipPdfGenerator.class.getClassLoader().getResourceAsStream("jrxml/payslip.jrxml");
             InputStream payslipAllowanceReportStream = PayslipPdfGenerator.class.getClassLoader().getResourceAsStream("jrxml/payslip_allowance.jrxml");
             OutputStream payslipPdfOutputStream = Files.newOutputStream(Path.of(payslipFile))) {
            JasperReport payslipReport = JasperCompileManager.compileReport(payslipReportStream);
            JasperReport payslipAllowanceReport = JasperCompileManager.compileReport(payslipAllowanceReportStream);

            long workHours = timesheetEntries.stream().filter(timesheetEntry -> timesheetEntry.getType().equals(TimesheetType.WORK)).mapToLong(value -> ChronoUnit.MINUTES.between(LocalTime.parse(value.getStartTime(), DateTimeFormatter.ofPattern("HH:mm")), LocalTime.parse(value.getEndTime(), DateTimeFormatter.ofPattern("HH:mm")))).sum() / 60;
            long holidayHours = timesheetEntries.stream().filter(timesheetEntry -> timesheetEntry.getType().equals(TimesheetType.HOLIDAY)).mapToLong(value -> ChronoUnit.MINUTES.between(LocalTime.parse(value.getStartTime(), DateTimeFormatter.ofPattern("HH:mm")), LocalTime.parse(value.getEndTime(), DateTimeFormatter.ofPattern("HH:mm")))).sum() / 60;
            long sickHours = timesheetEntries.stream().filter(timesheetEntry -> timesheetEntry.getType().equals(TimesheetType.SICKNESS)).mapToLong(value -> ChronoUnit.MINUTES.between(LocalTime.parse(value.getStartTime(), DateTimeFormatter.ofPattern("HH:mm")), LocalTime.parse(value.getEndTime(), DateTimeFormatter.ofPattern("HH:mm")))).sum() / 60;

            List<Allowance> allowances = new ArrayList<>();
            allowances.add(new Allowance("", "", "", formatCurrency(payslip.getGrossSalary()), "Gross wage"));
            allowances.add(new Allowance("", "", "", "", "&nbsp;"));
            boolean printGrossWage = true;
            if (workHours > 0) {
                allowances.add(new Allowance("" + workHours / 8, "" + workHours, "", formatCurrency(payslip.getGrossSalary()), "Normal hours"));
                printGrossWage = false;
            }
            if (holidayHours > 0) {
                allowances.add(new Allowance("" + holidayHours / 8, "" + holidayHours, "", printGrossWage ? formatCurrency(payslip.getGrossSalary()) : "", "Holidays"));
                printGrossWage = false;
            }
            if (sickHours > 0) {
                allowances.add(new Allowance("" + sickHours / 8, "" + sickHours, "", printGrossWage ? formatCurrency(payslip.getGrossSalary()) : "", "Sick leave"));
            }
            double taxes = payslip.getGrossSalary() * payslip.getTaxRate() / 100;
            allowances.add(new Allowance("", "", payslip.getTaxRate() + "", formatCurrency(taxes), "Taxes"));
            for (org.zero_consult.invoices_backend.entities.Allowance payslipAllowance : payslip.getAllowances()) {
                allowances.add(new Allowance("", "", "", payslipAllowance.getAmount() != null ? formatCurrency(payslipAllowance.getAmount()) : "", payslipAllowance.getLabel()));
            }
            double totalNetWage = (payslip.getGrossSalary() - taxes) + payslip.getAllowances().stream().mapToDouble(value -> value.getAmount()).sum();
            allowances.add(new Allowance("", "", "", formatCurrency(totalNetWage), "TOTAL NET"));

            List<Map<String, String>> mappedAllowances = mapAllowances(allowances);
            JRDataSource dataSource = new JRBeanCollectionDataSource(mappedAllowances);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("EMPLOYEE_NAME", employee.getFirstName() + " " + employee.getLastName());
            parameters.put("EMPLOYEE_FUNCTION", employee.getFunctionTitle());
            parameters.put("PERIOD", payslip.getPayslipMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " - " + payslip.getPayslipMonth().plusMonths(1).minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            parameters.put("CREATION_DAY", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            parameters.put("CURRENCY", "Eur");
            parameters.put("LINES", dataSource);
            parameters.put("PAYSLIP_ALLOWANCE_SUB", payslipAllowanceReport);
            JasperPrint jasperPrint = JasperFillManager.fillReport(payslipReport, parameters, new JREmptyDataSource());

            JRPdfExporter exporter = new JRPdfExporter();

            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(payslipPdfOutputStream));

            exporter.exportReport();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NonNull List<Map<String, String>> mapAllowances(List<Allowance> allowances) {
        List<Map<String, String>> mappedAllowances = new ArrayList<>();
        for (Allowance allowance : allowances) {
            HashMap<String, String> singleRow = new HashMap<>();
            singleRow.put("DAYS", allowance.days);
            singleRow.put("UNIT", allowance.unit);
            singleRow.put("PERCENT", allowance.percent);
            singleRow.put("AMOUNT", allowance.amount);
            singleRow.put("DESCRIPTION", allowance.description);
            mappedAllowances.add(singleRow);
        }
        return mappedAllowances;
    }
}
