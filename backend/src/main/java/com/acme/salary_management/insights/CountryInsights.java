package com.acme.salary_management.insights;

import java.util.List;

public record CountryInsights(CountrySalaryStats summary, List<JobTitleSalaryStats> byJobTitle) {
}