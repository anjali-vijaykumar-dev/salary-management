package com.acme.salary_management.employee;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Long id) {
        super("Employee " + id + " not found");
    }
}