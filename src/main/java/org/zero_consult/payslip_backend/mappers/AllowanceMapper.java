package org.zero_consult.payslip_backend.mappers;

import org.zero_consult.payslip_backend.entities.Allowance;
import org.zero_consult.payslip_backend.entities.Payslip;

import java.util.Optional;

public class AllowanceMapper {
    public static Allowance toEntity(org.zero_consult.idl.model.Allowance idl, Payslip payslip) {
        Allowance entity = new Allowance();
        entity.setAmount(idl.getAmount());
        idl.getId().ifPresent(entity::setId);
        entity.setLabel(idl.getLabel());
        entity.setPayslip(payslip);
        return entity;
    }

    public static org.zero_consult.idl.model.Allowance toIdl(Allowance entity) {
        org.zero_consult.idl.model.Allowance idl = new org.zero_consult.idl.model.Allowance();
        idl.setAmount(entity.getAmount());
        idl.setId(entity.getId() != null ? Optional.of(entity.getId()) : Optional.empty());
        idl.setLabel(entity.getLabel());
        return idl;
    }

}
