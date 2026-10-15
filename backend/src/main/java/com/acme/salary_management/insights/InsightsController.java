package com.acme.salary_management.insights;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService service;

    public InsightsController(InsightsService service) {
        this.service = service;
    }

    @GetMapping("/countries")
    public List<CountrySalaryStats> countries() {
        return service.countries();
    }

    @GetMapping("/countries/{country}")
    public CountryInsights country(@PathVariable String country) {
        return service.country(country);
    }

    @GetMapping("/job-titles")
    public List<String> jobTitles() {
        return service.jobTitles();
    }
}