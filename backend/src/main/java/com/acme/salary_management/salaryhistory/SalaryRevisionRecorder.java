package com.acme.salary_management.salaryhistory;

import com.acme.salary_management.employee.SalaryChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Listens for salary changes and stores a revision.
 * Runs synchronously in the same transaction as the update, so the salary change
 * and its history entry are saved together or not at all.
 */
@Component
public class SalaryRevisionRecorder {

    private final SalaryRevisionRepository repository;

    public SalaryRevisionRecorder(SalaryRevisionRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void on(SalaryChangedEvent event) {
        repository.save(new SalaryRevision(event.employeeId(), event.oldSalary(), event.newSalary(),
                event.currency(), event.reason(), Instant.now()));
    }
}