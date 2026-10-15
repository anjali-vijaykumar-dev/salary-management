package com.acme.salary_management.employee;

import com.acme.salary_management.common.PageResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repository;
    private final ApplicationEventPublisher events;

    public EmployeeService(EmployeeRepository repository, ApplicationEventPublisher events) {
        this.repository = repository;
        this.events = events;
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        Employee employee = new Employee(request.fullName(), request.email(), request.jobTitle(),
                request.department(), request.country(), request.currency(), request.salary(), request.hireDate());
        return EmployeeResponse.from(repository.save(employee));
    }

    public EmployeeResponse get(Long id) {
        return EmployeeResponse.from(findOrThrow(id));
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findOrThrow(id);
        boolean emailChanged = !employee.getEmail().equalsIgnoreCase(request.email());
        if (emailChanged && repository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        BigDecimal oldSalary = employee.getSalary();
        employee.update(request.fullName(), request.email(), request.jobTitle(), request.department(),
                request.country(), request.currency(), request.salary(), request.hireDate());
        if (oldSalary.compareTo(request.salary()) != 0) {
            events.publishEvent(new SalaryChangedEvent(id, oldSalary, request.salary(),
                    request.currency(), request.salaryChangeReason()));
        }
        return EmployeeResponse.from(employee);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        repository.deleteById(id);
    }



    public PageResponse<EmployeeResponse> search(EmployeeSearchCriteria criteria, Pageable pageable) {
        return PageResponse.from(repository.findAll(EmployeeSpecifications.matching(criteria), pageable)
                .map(EmployeeResponse::from));
    }

    private Employee findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }
}