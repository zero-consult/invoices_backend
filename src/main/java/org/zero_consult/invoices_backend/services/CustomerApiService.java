package org.zero_consult.invoices_backend.services;

import org.springframework.stereotype.Service;
import org.zero_consult.idl.client.api.CustomerApi;
import org.zero_consult.invoices_backend.configuration.CustomProperties;

@Service
public class CustomerApiService {

    private final CustomProperties customProperties;
    private final CustomerApi customerApi;

    public CustomerApiService(CustomProperties customProperties) {
        this.customProperties = customProperties;
        this.customerApi = new CustomerApi();
        this.customerApi.setCustomBaseUrl(customProperties.getPeopleBackendHost());
    }

    public CustomerApi getCustomerApi() {
        return customerApi;
    }
}
