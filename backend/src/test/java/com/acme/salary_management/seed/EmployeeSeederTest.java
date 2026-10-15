package com.acme.salary_management.seed;

import com.acme.salary_management.employee.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeSeederTest {

    @Autowired
    private EmployeeRepository repository;

    @Test
    void seedsEmployeesWhenDatabaseIsEmpty() {
        new EmployeeSeeder(repository, 50).seed();

        assertThat(repository.count()).isEqualTo(50);
    }

    @Test
    void doesNotSeedAgainWhenEmployeesAlreadyExist() {
        EmployeeSeeder seeder = new EmployeeSeeder(repository, 50);

        seeder.seed();
        seeder.seed();

        assertThat(repository.count()).isEqualTo(50);
    }
}