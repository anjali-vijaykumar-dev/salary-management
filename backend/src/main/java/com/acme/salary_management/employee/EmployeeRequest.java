package com.acme.salary_management.employee;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email String email,
        @NotBlank String jobTitle,
        @NotBlank String department,
        @NotBlank String country,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull @Positive BigDecimal salary,
        @NotNull @PastOrPresent LocalDate hireDate,
        @Size(max = 255) String salaryChangeReason) {
}