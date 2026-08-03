package org.zero_consult.payslip_backend.services;

import org.springframework.stereotype.Service;
import org.zero_consult.idl.client.api.CustomerApi;
import org.zero_consult.payslip_backend.configuration.CustomerProperties;

@Service
public class CustomerApiService {

    private final CustomerProperties customerProperties;
    private final CustomerApi customerApi;

    public CustomerApiService(CustomerProperties customerProperties) {
        this.customerProperties = customerProperties;
        this.customerApi = new CustomerApi();
        this.customerApi.setCustomBaseUrl(customerProperties.getPeopleBackendHost());
    }

    public CustomerApi getCustomerApi() {
        return customerApi;
    }
}
