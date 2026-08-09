package org.zero_consult.invoices_backend.services;

import org.jspecify.annotations.NonNull;
import org.zero_consult.idl.client.ApiException;
import org.zero_consult.idl.client.api.EmployeeApi;
import org.zero_consult.idl.client.model.Employee;
import org.zero_consult.idl.client.model.UpdateCustomerHasTimesheetEntriesRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class EmployeeApiMock extends EmployeeApi {
    private static final ThreadLocal<SimpleDateFormat> dateFormat =
            ThreadLocal.withInitial(() -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf;
            });

    @Override
    public Employee updateEmployeeHasTimesheetEntries(@NonNull String id, @NonNull UpdateCustomerHasTimesheetEntriesRequest updateCustomerHasTimesheetEntriesRequest) throws ApiException {
        return new Employee();
    }

    @Override
    public Employee getEmployee(@NonNull String id) throws ApiException {
        Employee employee = new Employee();
        try {
            Date dateParsed = dateFormat.get().parse("2026-05-01");
            employee.setStartDate(dateParsed.getTime());
        } catch (ParseException ignored) {
        }
        return employee;
    }
}
