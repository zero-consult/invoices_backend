package org.zero_consult.payslip_backend.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "zero-consult")
public class CustomerProperties {
    private String peopleBackendHost;
    private String timesheetBackendHost;

    public String getPeopleBackendHost() {
        return peopleBackendHost;
    }

    public void setPeopleBackendHost(String peopleBackendHost) {
        this.peopleBackendHost = peopleBackendHost;
    }

    public String getTimesheetBackendHost() {
        return timesheetBackendHost;
    }

    public void setTimesheetBackendHost(String timesheetBackendHost) {
        this.timesheetBackendHost = timesheetBackendHost;
    }
}
