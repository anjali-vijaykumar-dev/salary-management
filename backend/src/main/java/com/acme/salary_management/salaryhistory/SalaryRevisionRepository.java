package com.acme.salary_management.salaryhistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalaryRevisionRepository extends JpaRepository<SalaryRevision, Long> {

    List<SalaryRevision> findByEmployeeIdOrderByIdDesc(Long employeeId);
}