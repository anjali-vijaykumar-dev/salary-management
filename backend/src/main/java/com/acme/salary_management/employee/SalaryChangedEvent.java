package com.acme.salary_management.employee;

import java.math.BigDecimal;

/** Published whenever an employee's salary changes; other modules react without EmployeeService knowing about them. */
public record SalaryChangedEvent(Long employeeId, BigDecimal oldSalary, BigDecimal newSalary,
                                 String currency, String reason) {
}