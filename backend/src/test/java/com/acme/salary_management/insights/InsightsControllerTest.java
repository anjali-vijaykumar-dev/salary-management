package com.acme.salary_management.insights;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InsightsController.class)
class InsightsControllerTest {

    private static final CountrySalaryStats INDIA = new CountrySalaryStats("India", "INR", 3,
            new BigDecimal("1000000"), new BigDecimal("3000000"), new BigDecimal("2000000.00"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InsightsService service;

    @Test
    void returnsStatsForEveryCountry() throws Exception {
        when(service.countries()).thenReturn(List.of(INDIA));

        mockMvc.perform(get("/api/insights/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country").value("India"))
                .andExpect(jsonPath("$[0].headcount").value(3));
    }

    @Test
    void returnsTheJobTitleBreakdownForACountry() throws Exception {
        JobTitleSalaryStats engineers = new JobTitleSalaryStats("Software Engineer", 2,
                new BigDecimal("1000000"), new BigDecimal("2000000"), new BigDecimal("1500000.00"));
        when(service.country("India")).thenReturn(new CountryInsights(INDIA, List.of(engineers)));

        mockMvc.perform(get("/api/insights/countries/{country}", "India"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.country").value("India"))
                .andExpect(jsonPath("$.byJobTitle[0].jobTitle").value("Software Engineer"));
    }

    @Test
    void returns404ForAnUnknownCountry() throws Exception {
        when(service.country("Atlantis")).thenThrow(new CountryNotFoundException("Atlantis"));

        mockMvc.perform(get("/api/insights/countries/{country}", "Atlantis"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsAllJobTitles() throws Exception {
        when(service.jobTitles()).thenReturn(List.of("Accountant", "Software Engineer"));

        mockMvc.perform(get("/api/insights/job-titles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value("Software Engineer"));
    }
}