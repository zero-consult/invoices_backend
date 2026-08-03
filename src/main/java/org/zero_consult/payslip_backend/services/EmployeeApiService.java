package org.zero_consult.payslip_backend.services;

import org.springframework.stereotype.Service;
import org.zero_consult.idl.client.api.EmployeeApi;
import org.zero_consult.payslip_backend.configuration.CustomerProperties;

@Service
public class EmployeeApiService {

    private final CustomerProperties customerProperties;
    private final EmployeeApi employeeApi;

    public EmployeeApiService(CustomerProperties customerProperties) {
        this.customerProperties = customerProperties;
        this.employeeApi = new EmployeeApi();
        this.employeeApi.setCustomBaseUrl(customerProperties.getPeopleBackendHost());
    }

    public EmployeeApi getEmployeeApi() {
        return employeeApi;
    }
}
