package com.acme.salary_management.salaryhistory;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "salary_revisions",
        indexes = @Index(name = "idx_salary_revision_employee", columnList = "employee_id"))
public class SalaryRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // plain id, no foreign key: the audit trail survives even if the employee is later deleted
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "old_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal oldSalary;

    @Column(name = "new_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal newSalary;

    @Column(nullable = false, length = 3)
    private String currency;

    private String reason;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected SalaryRevision() {
        // required by JPA
    }

    public SalaryRevision(Long employeeId, BigDecimal oldSalary, BigDecimal newSalary,
                          String currency, String reason, Instant changedAt) {
        this.employeeId = employeeId;
        this.oldSalary = oldSalary;
        this.newSalary = newSalary;
        this.currency = currency;
        this.reason = reason;
        this.changedAt = changedAt;
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public BigDecimal getOldSalary() { return oldSalary; }
    public BigDecimal getNewSalary() { return newSalary; }
    public String getCurrency() { return currency; }
    public String getReason() { return reason; }
    public Instant getChangedAt() { return changedAt; }
}