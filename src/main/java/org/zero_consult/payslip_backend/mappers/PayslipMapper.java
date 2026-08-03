package org.zero_consult.payslip_backend.mappers;

import org.zero_consult.payslip_backend.entities.Payslip;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PayslipMapper {
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Payslip toEntity(org.zero_consult.idl.model.Payslip idl) {
        Payslip entity = new Payslip();
        entity.setAllowances(idl.getAllowances().stream().map(allowance -> AllowanceMapper.toEntity(allowance, entity)).toList());
        entity.setEmployeeId(idl.getEmployeeId());
        entity.setGrossSalary(idl.getGrossSalary());
        idl.getId().ifPresent(entity::setId);
        entity.setMonth(LocalDate.parse(idl.getMonth(), MONTH_FORMAT));
        idl.getPayslipFile().ifPresent(entity::setPayslipFile);
        entity.setTaxRate(idl.getTaxRate());
        return entity;
    }

    public static org.zero_consult.idl.model.Payslip toIdl(Payslip entity) {
        org.zero_consult.idl.model.Payslip idl = new org.zero_consult.idl.model.Payslip();
        idl.setAllowances(entity.getAllowances().stream().map(AllowanceMapper::toIdl).toList());
        idl.setEmployeeId(entity.getEmployeeId());
        idl.setGrossSalary(entity.getGrossSalary());
        idl.setId(entity.getId()!=null? Optional.of(entity.getId()) : Optional.empty());
        idl.setMonth(entity.getMonth().format(MONTH_FORMAT));
        idl.setPayslipFile(entity.getPayslipFile() != null ? Optional.of(entity.getPayslipFile()) : Optional.empty());
        idl.setTaxRate(entity.getTaxRate());
        return idl;
    }
}
