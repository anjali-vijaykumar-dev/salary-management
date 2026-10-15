package com.acme.salary_management.insights;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class InsightsService {

    private final SalaryInsightsRepository repository;

    public InsightsService(SalaryInsightsRepository repository) {
        this.repository = repository;
    }

    public List<CountrySalaryStats> countries() {
        return repository.salaryStatsByCountry();
    }

    public CountryInsights country(String country) {
        CountrySalaryStats summary = repository.salaryStatsForCountry(country).stream()
                .findFirst()
                .orElseThrow(() -> new CountryNotFoundException(country));
        return new CountryInsights(summary, repository.salaryStatsByJobTitle(country));
    }

    public List<String> jobTitles() {
        return repository.distinctJobTitles();
    }
}