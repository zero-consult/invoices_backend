package org.zero_consult.invoices_backend.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "zero-consult")
public class CustomProperties {
    private String invoicesDir;
    private String jwtSecretKey;
    private String peopleBackendHost;
    private String timesheetBackendHost;

    public String getInvoicesDir() {
        return invoicesDir;
    }

    public void setInvoicesDir(String invoicesDir) {
        this.invoicesDir = invoicesDir;
    }

    public String getJwtSecretKey() {
        return jwtSecretKey;
    }

    public void setJwtSecretKey(String jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

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
