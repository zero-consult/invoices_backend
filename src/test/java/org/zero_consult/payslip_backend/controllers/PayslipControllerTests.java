package org.zero_consult.payslip_backend.controllers;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.zero_consult.payslip_backend.InvoicesBackendApplication;
import org.zero_consult.payslip_backend.entities.Allowance;
import org.zero_consult.payslip_backend.entities.Payslip;
import org.zero_consult.payslip_backend.repositories.PayslipRepository;
import org.zero_consult.payslip_backend.services.CustomerApiMock;
import org.zero_consult.payslip_backend.services.CustomerApiService;
import org.zero_consult.payslip_backend.services.EmployeeApiMock;
import org.zero_consult.payslip_backend.services.EmployeeApiService;

import java.io.File;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = InvoicesBackendApplication.class)
@AutoConfigureMockMvc
public class PayslipControllerTests {

    private final static LocalDate MOCK_DATE = LocalDate.of(2026, 07, 20);

    private final MockMvc mvc;
    private final PayslipRepository payslipRepository;
    @MockitoBean
    private CustomerApiService customerApiService;
    @MockitoBean
    private EmployeeApiService employeeApiService;

    @Mock
    private Clock clock;

    //field that will contain the fixed clock
    private static Clock fixedClock;

    public PayslipControllerTests(@Autowired MockMvc mvc, @Autowired PayslipRepository payslipRepository) {
        this.mvc = mvc;
        this.payslipRepository = payslipRepository;
    }

    @BeforeEach
    public void initClock() {
        fixedClock = Clock.fixed(MOCK_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        Mockito.doReturn(fixedClock.instant()).when(clock).instant();
        Mockito.doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    @Test
    public void testPayslipsList() throws Exception {
        initData();
        mvc.perform(MockMvcRequestBuilders.get("/payslips?from=2026-07-20&until=2026-07-27")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is("description")));
    }

    @Test
    public void testAddPayslip() throws Exception {
        initData();
        Mockito.when(customerApiService.getCustomerApi()).thenReturn(new CustomerApiMock());
        Mockito.when(employeeApiService.getEmployeeApi()).thenReturn(new EmployeeApiMock());
        mvc.perform(MockMvcRequestBuilders.post("/payslips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("json_input/AddPayslip.json").getFile()), "UTF-8")))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    public void testGetPayslip() throws Exception {
        Payslip data = initData();
        mvc.perform(MockMvcRequestBuilders.get("/payslips/" + data.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void testUpdatePayslip() throws Exception {
        Payslip data = initData();
        Mockito.when(customerApiService.getCustomerApi()).thenReturn(new CustomerApiMock());
        Mockito.when(employeeApiService.getEmployeeApi()).thenReturn(new EmployeeApiMock());
        mvc.perform(MockMvcRequestBuilders.put("/payslips/" + data.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("json_input/UpdatePayslip.json").getFile()), "UTF-8")))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testDeletePayslip() throws Exception {
        Payslip data = initData();
        Mockito.when(customerApiService.getCustomerApi()).thenReturn(new CustomerApiMock());
        Mockito.when(employeeApiService.getEmployeeApi()).thenReturn(new EmployeeApiMock());
        mvc.perform(MockMvcRequestBuilders.delete("/payslips/" + data.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    public void testDeletePayslipEntityNotFoundException() throws Exception {
        Payslip data = initData();
        Mockito.when(customerApiService.getCustomerApi()).thenReturn(new CustomerApiMock());
        Mockito.when(employeeApiService.getEmployeeApi()).thenReturn(new EmployeeApiMock());
        mvc.perform(MockMvcRequestBuilders.delete("/payslips/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    private Payslip initData() {
        Payslip entity = new Payslip();
        entity.setEmployeeId("1");
        entity.setMonth(java.time.LocalDate.of(2026, 7, 20));
        entity.setGrossSalary(4000d);
        entity.setTaxRate(37.5d);
        entity.setPayslipFile("");
        ArrayList<Allowance> allowances = new ArrayList<>();
        Allowance allowance = new Allowance();
        allowance.setLabel("Company car");
        allowance.setAmount(-250d);
        allowances.add(allowance);
        entity.setAllowances(allowances);
        return payslipRepository.save(entity);
    }

}
