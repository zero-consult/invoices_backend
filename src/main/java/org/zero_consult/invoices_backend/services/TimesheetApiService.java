package org.zero_consult.invoices_backend.services;

import org.springframework.stereotype.Service;
import org.zero_consult.idl.client.api.TimesheetApi;
import org.zero_consult.invoices_backend.configuration.CustomProperties;

@Service
public class TimesheetApiService {

    private final CustomProperties customProperties;
    private final TimesheetApi timesheetsApi;

    public TimesheetApiService(CustomProperties customProperties) {
        this.customProperties = customProperties;
        this.timesheetsApi = new TimesheetApi();
        this.timesheetsApi.setCustomBaseUrl(customProperties.getTimesheetBackendHost());
    }

    public TimesheetApi getTimesheetApi() {
        return timesheetsApi;
    }
}
