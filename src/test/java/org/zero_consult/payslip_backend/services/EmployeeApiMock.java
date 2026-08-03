package org.zero_consult.payslip_backend.services;

import org.jspecify.annotations.NonNull;
import org.zero_consult.idl.client.ApiException;
import org.zero_consult.idl.client.api.EmployeeApi;
import org.zero_consult.idl.client.model.Employee;
import org.zero_consult.idl.client.model.UpdateCustomerHasTimesheetEntriesRequest;

public class EmployeeApiMock extends EmployeeApi {
    @Override
    public Employee updateEmployeeHasTimesheetEntries(@NonNull String id, @NonNull UpdateCustomerHasTimesheetEntriesRequest updateCustomerHasTimesheetEntriesRequest) throws ApiException {
        return new Employee();
    }
}
