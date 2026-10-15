package com.acme.salary_management.employee;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_country", columnList = "country"),
        @Index(name = "idx_employee_country_job_title", columnList = "country, job_title")
})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    protected Employee() {
        // required by JPA
    }

    public Employee(String fullName, String email, String jobTitle, String department,
                    String country, String currency, BigDecimal salary, LocalDate hireDate) {
        this.fullName = fullName;
        this.email = email;
        this.jobTitle = jobTitle;
        this.department = department;
        this.country = country;
        this.currency = currency;
        this.salary = salary;
        this.hireDate = hireDate;
    }

    public void update(String fullName, String email, String jobTitle, String department,
                       String country, String currency, BigDecimal salary, LocalDate hireDate) {
        this.fullName = fullName;
        this.email = email;
        this.jobTitle = jobTitle;
        this.department = department;
        this.country = country;
        this.currency = currency;
        this.salary = salary;
        this.hireDate = hireDate;
    }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getJobTitle() { return jobTitle; }
    public String getDepartment() { return department; }
    public String getCountry() { return country; }
    public String getCurrency() { return currency; }
    public BigDecimal getSalary() { return salary; }
    public LocalDate getHireDate() { return hireDate; }
}