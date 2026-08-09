package org.zero_consult.invoices_backend.mappers;

import org.zero_consult.invoices_backend.entities.Invoice;

import java.util.Optional;

public class InvoiceMapper {

    public static Invoice toEntity(org.zero_consult.idl.model.Invoice idl) {
        Invoice entity = new Invoice();
        entity.setCustomerId(idl.getCustomerId());
        entity.setExpireDate(idl.getExpireDate());
        idl.getId().ifPresent(entity::setId);
        entity.setInvoiceDate(idl.getInvoiceDate());
        idl.getInvoiceFile().ifPresent(entity::setInvoiceFile);
        idl.getInvoiceNumber().ifPresent(entity::setInvoiceNumber);
        entity.setInvoiceUntil(idl.getInvoiceUntil());
        entity.setNotes(idl.getNotes());
        entity.setStatus(InvoiceStatusMapper.toEntity(idl.getStatus()));
        entity.setTotalWithoutVat(idl.getTotalWithoutVat());
        entity.setVatPercent(idl.getVatPercent());
        return entity;
    }

    public static org.zero_consult.idl.model.Invoice toIdl(Invoice entity) {
        org.zero_consult.idl.model.Invoice idl = new org.zero_consult.idl.model.Invoice();
        idl.setCustomerId(entity.getCustomerId());
        idl.setExpireDate(entity.getExpireDate());
        idl.setId(entity.getId() != null ? Optional.of(entity.getId()) : Optional.empty());
        idl.setInvoiceDate(entity.getInvoiceDate());
        idl.setInvoiceFile(entity.getInvoiceFile() != null ? Optional.of(entity.getInvoiceFile()) : Optional.empty());
        idl.setInvoiceNumber(entity.getInvoiceNumber() != null ? Optional.of(entity.getInvoiceNumber()) : Optional.empty());
        idl.setInvoiceUntil(entity.getInvoiceUntil());
        idl.setStatus(InvoiceStatusMapper.toIdl(entity.getStatus()));
        idl.setNotes(entity.getNotes());
        idl.setTotalWithoutVat(entity.getTotalWithoutVat());
        idl.setVatPercent(entity.getVatPercent());
        return idl;
    }
}
