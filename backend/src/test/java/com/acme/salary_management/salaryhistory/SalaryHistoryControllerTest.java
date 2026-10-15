package com.acme.salary_management.salaryhistory;

import com.acme.salary_management.employee.EmployeeNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SalaryHistoryController.class)
class SalaryHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalaryHistoryService service;

    @Test
    void returnsTheSalaryHistoryOfAnEmployee() throws Exception {
        when(service.historyFor(1L)).thenReturn(List.of(new SalaryRevisionResponse(10L,
                new BigDecimal("1000000"), new BigDecimal("1250000"), "INR", "Annual appraisal",
                new BigDecimal("25.0"), Instant.parse("2026-10-15T10:00:00Z"))));

        mockMvc.perform(get("/api/employees/1/salary-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Annual appraisal"))
                .andExpect(jsonPath("$[0].changePercent").value(25.0));
    }

    @Test
    void returns404ForAnUnknownEmployee() throws Exception {
        when(service.historyFor(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employees/99/salary-history"))
                .andExpect(status().isNotFound());
    }
}