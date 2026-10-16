package com.acme.salary_management.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

@WebMvcTest(SpaController.class)
@WithMockUser(roles = "HR_MANAGER")
class SpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void forwardsAngularRoutesToIndexHtmlSoPageRefreshWorks() throws Exception {
        mockMvc.perform(get("/employees")).andExpect(forwardedUrl("/index.html"));
        mockMvc.perform(get("/insights")).andExpect(forwardedUrl("/index.html"));
    }
}