package org.zero_consult.invoices_backend.services;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.zero_consult.idl.client.ApiException;
import org.zero_consult.idl.client.api.TimesheetApi;
import org.zero_consult.idl.client.model.TimesheetEntry;
import org.zero_consult.idl.client.model.TimesheetStatus;
import org.zero_consult.idl.client.model.TimesheetType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TimesheetApiMock extends TimesheetApi {
    @Override
    public List<TimesheetEntry> timesheetsList(@NonNull LocalDate from, @NonNull LocalDate until, @Nullable String employeeId, @Nullable String customerId) throws ApiException {
        ArrayList<TimesheetEntry> timesheetEntries = new ArrayList<>();
        if(employeeId != null) {
            LocalDate dateToProcess = from;
            while(dateToProcess.isBefore(until) || dateToProcess.isEqual(until)) {
                if(dateToProcess.getDayOfWeek() != DayOfWeek.SATURDAY && dateToProcess.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    TimesheetEntry entry = new TimesheetEntry();
                    entry.setStartTime("09:00");
                    entry.setEndTime("17:00");
                    entry.setDate(dateToProcess);
                    entry.setDescription("");
                    entry.setCustomerId("1");
                    entry.setStatus(TimesheetStatus.APPROVED);
                    entry.setType(TimesheetType.WORK);
                    entry.setEmployeeId("1");
                    timesheetEntries.add(entry);
                }
                dateToProcess = dateToProcess.plusDays(1);
            }
        }
        return timesheetEntries;
    }
}
