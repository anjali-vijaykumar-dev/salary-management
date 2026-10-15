package com.acme.salary_management.salaryhistory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public record SalaryRevisionResponse(Long id, BigDecimal oldSalary, BigDecimal newSalary, String currency,
                                     String reason, BigDecimal changePercent, Instant changedAt) {

    static SalaryRevisionResponse from(SalaryRevision r) {
        return new SalaryRevisionResponse(r.getId(), r.getOldSalary(), r.getNewSalary(), r.getCurrency(),
                r.getReason(), percentChange(r.getOldSalary(), r.getNewSalary()), r.getChangedAt());
    }

    private static BigDecimal percentChange(BigDecimal oldSalary, BigDecimal newSalary) {
        if (oldSalary.signum() == 0) {
            return null;
        }
        return newSalary.subtract(oldSalary)
                .multiply(BigDecimal.valueOf(100))
                .divide(oldSalary, 1, RoundingMode.HALF_UP);
    }
}