package com.acme.salary_management.employee;

import com.acme.salary_management.common.PageResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final EmployeeResponse ASHA = new EmployeeResponse(1L, "Asha Rao", "asha@acme.com",
            "Software Engineer", "Engineering", "India", "INR", new BigDecimal("1800000"), LocalDate.of(2022, 4, 1));

    private static final String VALID_BODY = """
            {"fullName":"Asha Rao","email":"asha@acme.com","jobTitle":"Software Engineer",
             "department":"Engineering","country":"India","currency":"INR",
             "salary":1800000,"hireDate":"2022-04-01"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService service;

    @Test
    void listsEmployeesUsingTheGivenFilters() throws Exception {
        when(service.search(any(), any())).thenReturn(new PageResponse<>(List.of(ASHA), 0, 20, 1, 1));

        mockMvc.perform(get("/api/employees").param("country", "India"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Asha Rao"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).search(eq(new EmployeeSearchCriteria(null, "India", null)), any());
    }

    @Test
    void capsThePageSizeAt100() throws Exception {
        when(service.search(any(), any())).thenReturn(new PageResponse<>(List.of(), 0, 100, 0, 0));

        mockMvc.perform(get("/api/employees").param("size", "5000")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).search(any(), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void rejectsSortingByAnUnknownField() throws Exception {
        mockMvc.perform(get("/api/employees").param("sortBy", "password"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void returnsAnEmployeeById() throws Exception {
        when(service.get(1L)).thenReturn(ASHA);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("asha@acme.com"))
                .andExpect(jsonPath("$.hireDate").value("2022-04-01"));
    }

    @Test
    void returns404WhenTheEmployeeDoesNotExist() throws Exception {
        when(service.get(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee 99 not found"));
    }

    @Test
    void createsAnEmployee() throws Exception {
        when(service.create(any())).thenReturn(ASHA);

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/employees/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void rejectsAnInvalidEmployeeWithFieldErrors() throws Exception {
        String invalid = """
                {"fullName":"","email":"not-an-email","jobTitle":"Software Engineer",
                 "department":"Engineering","country":"India","currency":"INR",
                 "salary":-5,"hireDate":"2022-04-01"}
                """;

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.salary").exists());

        verifyNoInteractions(service);
    }

    @Test
    void returns409WhenTheEmailAlreadyExists() throws Exception {
        when(service.create(any())).thenThrow(new DuplicateEmailException("asha@acme.com"));

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void updatesAnEmployee() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(ASHA);

        mockMvc.perform(put("/api/employees/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Asha Rao"));
    }

    @Test
    void deletesAnEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/1")).andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}