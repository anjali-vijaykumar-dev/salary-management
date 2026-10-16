package com.acme.salary_management.security;

import com.acme.salary_management.common.PageResponse;
import com.acme.salary_management.employee.EmployeeController;
import com.acme.salary_management.employee.EmployeeService;
import com.acme.salary_management.web.MeController;
import com.acme.salary_management.web.SpaController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({EmployeeController.class, SpaController.class, MeController.class})
@Import(SecurityConfig.class)
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService service;

    @Test
    void apiReturns401WhenNotSignedIn() throws Exception {
        mockMvc.perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pagesRedirectToTheLoginPageWhenNotSignedIn() throws Exception {
        mockMvc.perform(get("/employees").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));;
    }

    @Test
    @WithMockUser(roles = "HR_VIEWER")
    void viewerCanReadEmployees() throws Exception {
        when(service.search(any(), any())).thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR_VIEWER")
    void viewerCannotDeleteEmployees() throws Exception {
        mockMvc.perform(delete("/api/employees/1").with(csrf())).andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "HR_MANAGER")
    void managerCanDeleteEmployees() throws Exception {
        mockMvc.perform(delete("/api/employees/1").with(csrf())).andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    @WithMockUser(roles = "HR_MANAGER")
    void changesWithoutACsrfTokenAreRejected() throws Exception {
        mockMvc.perform(delete("/api/employees/1")).andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(username = "stranger@gmail.com")
    void signedInUserWithoutARoleCannotSeeSalaries() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hr.viewer", roles = "HR_VIEWER")
    void meReturnsTheUserAndTheirRoles() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("hr.viewer"))
                .andExpect(jsonPath("$.roles[0]").value("HR_VIEWER"));
    }

    @Test
    void showsTheSignedOutMessageAfterLogout() throws Exception {
        mockMvc.perform(get("/login?logout"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("signed out")));
    }
}
