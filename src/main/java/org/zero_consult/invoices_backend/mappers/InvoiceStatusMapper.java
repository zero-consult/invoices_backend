package org.zero_consult.invoices_backend.mappers;

import org.zero_consult.invoices_backend.entities.InvoiceStatus;

public class InvoiceStatusMapper {
    public static org.zero_consult.idl.model.InvoiceStatus toIdl(InvoiceStatus status) {
        return org.zero_consult.idl.model.InvoiceStatus.valueOf(status.name());
    }

    public static InvoiceStatus toEntity(org.zero_consult.idl.model.InvoiceStatus status) {
        return InvoiceStatus.valueOf(status.name());
    }

}
