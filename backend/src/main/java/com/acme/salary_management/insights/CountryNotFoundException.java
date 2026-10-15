package com.acme.salary_management.insights;

public class CountryNotFoundException extends RuntimeException {
    public CountryNotFoundException(String country) {
        super("No employees found in " + country);
    }
}