package org.zero_consult.payslip_backend.services;

import org.springframework.stereotype.Service;
import org.zero_consult.idl.client.api.EmployeeApi;
import org.zero_consult.idl.client.api.TimesheetApi;
import org.zero_consult.payslip_backend.configuration.CustomerProperties;

@Service
public class TimesheetApiService {

    private final CustomerProperties customerProperties;
    private final TimesheetApi timesheetsApi;

    public TimesheetApiService(CustomerProperties customerProperties) {
        this.customerProperties = customerProperties;
        this.timesheetsApi = new TimesheetApi();
        this.timesheetsApi.setCustomBaseUrl(customerProperties.getTimesheetBackendHost());
    }

    public TimesheetApi getTimesheetApi() {
        return timesheetsApi;
    }
}
