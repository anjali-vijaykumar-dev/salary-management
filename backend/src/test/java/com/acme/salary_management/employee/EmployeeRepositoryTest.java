package com.acme.salary_management.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @Test
    void savesAndLoadsAnEmployee() {
        Employee employee = new Employee("Asha Rao", "asha.rao@acme.com", "Software Engineer",
                "Engineering", "India", "INR", new BigDecimal("1800000"), LocalDate.of(2022, 4, 1));

        Employee saved = repository.save(employee);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId()))
                .get()
                .extracting(Employee::getEmail, Employee::getCountry)
                .containsExactly("asha.rao@acme.com", "India");
    }
}