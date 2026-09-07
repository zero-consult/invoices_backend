package org.zero_consult.invoices_backend.services;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.zero_consult.idl.client.ApiClient;
import org.zero_consult.idl.client.Configuration;
import org.zero_consult.idl.client.api.CustomerApi;
import org.zero_consult.invoices_backend.configuration.CustomProperties;
import org.zero_consult.invoices_backend.security.JwtUtil;
import org.zero_consult.invoices_backend.utils.RequestHelper;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CustomerApiService {

    private final CustomerApi customerApi;

    public CustomerApiService(CustomProperties customProperties, JwtUtil jwtUtil) {
        String jwtToken = jwtUtil.resolveToken(RequestHelper.getCurrentHttpRequest());
        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBearerToken(jwtToken);
        this.customerApi = new CustomerApi(apiClient);
        this.customerApi.setCustomBaseUrl(customProperties.getPeopleBackendHost());
    }

    public CustomerApi getCustomerApi() {
        return customerApi;
    }
}
