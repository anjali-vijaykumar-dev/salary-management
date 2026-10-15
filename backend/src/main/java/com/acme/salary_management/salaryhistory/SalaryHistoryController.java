package com.acme.salary_management.salaryhistory;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SalaryHistoryController {

    private final SalaryHistoryService service;

    public SalaryHistoryController(SalaryHistoryService service) {
        this.service = service;
    }

    @GetMapping("/api/employees/{employeeId}/salary-history")
    public List<SalaryRevisionResponse> history(@PathVariable Long employeeId) {
        return service.historyFor(employeeId);
    }
}