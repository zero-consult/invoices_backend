package org.zero_consult.invoices_backend.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zero_consult.idl.client.ApiClient;
import org.zero_consult.idl.client.Configuration;
import org.zero_consult.idl.client.api.EmployeeApi;
import org.zero_consult.invoices_backend.configuration.CustomProperties;
import org.zero_consult.invoices_backend.security.JwtUtil;
import org.zero_consult.invoices_backend.utils.RequestHelper;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EmployeeApiService {

    private final EmployeeApi employeeApi;

    public EmployeeApiService(CustomProperties customProperties, JwtUtil jwtUtil) {
        String jwtToken = jwtUtil.resolveToken(RequestHelper.getCurrentHttpRequest());
        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBearerToken(jwtToken);
        this.employeeApi = new EmployeeApi(apiClient);
        this.employeeApi.setCustomBaseUrl(customProperties.getPeopleBackendHost());
    }

    public EmployeeApi getEmployeeApi() {
        return employeeApi;
    }
}
