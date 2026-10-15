package com.acme.salary_management.insights;

import java.math.BigDecimal;

public record CountrySalaryStats(String country, String currency, long headcount,
                                 BigDecimal minSalary, BigDecimal maxSalary, BigDecimal averageSalary) {

    /** Used by the JPQL query, where COUNT returns Long and AVG returns Double. */
    public CountrySalaryStats(String country, String currency, Long headcount,
                              BigDecimal minSalary, BigDecimal maxSalary, Double averageSalary) {
        this(country, currency, headcount, minSalary, maxSalary, SalaryMath.roundToCents(averageSalary));
    }
}