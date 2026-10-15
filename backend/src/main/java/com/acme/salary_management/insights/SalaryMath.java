package com.acme.salary_management.insights;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class SalaryMath {

    private SalaryMath() {
    }

    /** SQL AVG returns a Double; money is shown as BigDecimal with 2 decimals. */
    static BigDecimal roundToCents(Double value) {
        return value == null ? null : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}