package com.acme.salary_management.employee;

/** Published when an employee is deleted, so other modules can remove data that belongs to them. */
public record EmployeeDeletedEvent(Long employeeId) {
}