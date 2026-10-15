package com.acme.salary_management.employee;

/** Optional filters for the employee list; null or blank means "no filter". */
public record EmployeeSearchCriteria(String name, String country, String jobTitle) {
}