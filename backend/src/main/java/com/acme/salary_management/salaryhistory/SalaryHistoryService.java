package com.acme.salary_management.salaryhistory;

import com.acme.salary_management.employee.EmployeeNotFoundException;
import com.acme.salary_management.employee.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SalaryHistoryService {

    private final SalaryRevisionRepository revisions;
    private final EmployeeRepository employees;

    public SalaryHistoryService(SalaryRevisionRepository revisions, EmployeeRepository employees) {
        this.revisions = revisions;
        this.employees = employees;
    }

    public List<SalaryRevisionResponse> historyFor(Long employeeId) {
        if (!employees.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }
        return revisions.findByEmployeeIdOrderByIdDesc(employeeId).stream()
                .map(SalaryRevisionResponse::from)
                .toList();
    }
}