package org.zero_consult.invoices_backend.services;

import org.jspecify.annotations.NonNull;
import org.zero_consult.idl.client.ApiException;
import org.zero_consult.idl.client.api.CustomerApi;
import org.zero_consult.idl.client.model.Customer;
import org.zero_consult.idl.client.model.UpdateCustomerHasTimesheetEntriesRequest;

public class CustomerApiMock extends CustomerApi {
    @Override
    public Customer updateCustomerHasTimesheetEntries(@NonNull String id, @NonNull UpdateCustomerHasTimesheetEntriesRequest updateCustomerHasTimesheetEntriesRequest) throws ApiException {
        return new Customer();
    }
}
