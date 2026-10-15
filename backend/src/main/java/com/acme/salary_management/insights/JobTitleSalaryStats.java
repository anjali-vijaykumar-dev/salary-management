package com.acme.salary_management.insights;

import java.math.BigDecimal;

public record JobTitleSalaryStats(String jobTitle, long headcount,
                                  BigDecimal minSalary, BigDecimal maxSalary, BigDecimal averageSalary) {

    /** Used by the JPQL query, where COUNT returns Long and AVG returns Double. */
    public JobTitleSalaryStats(String jobTitle, Long headcount,
                               BigDecimal minSalary, BigDecimal maxSalary, Double averageSalary) {
        this(jobTitle, headcount, minSalary, maxSalary, SalaryMath.roundToCents(averageSalary));
    }
}