package com.acme.salary_management.seed;

import com.acme.salary_management.employee.Employee;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeDataGeneratorTest {

    private final EmployeeDataGenerator generator = new EmployeeDataGenerator(new Random(42));

    @Test
    void generatesTheRequestedNumberOfEmployees() {
        assertThat(generator.generate(100)).hasSize(100);
    }

    @Test
    void generatesUniqueEmails() {
        List<Employee> employees = generator.generate(1000);

        assertThat(employees).extracting(Employee::getEmail).doesNotHaveDuplicates();
    }

    @Test
    void usesTheCurrencyOfTheEmployeesCountry() {
        Map<String, String> currencyByCountry = EmployeeDataGenerator.COUNTRIES.stream()
                .collect(Collectors.toMap(EmployeeDataGenerator.CountryProfile::country,
                        EmployeeDataGenerator.CountryProfile::currency));

        assertThat(generator.generate(500)).allSatisfy(employee ->
                assertThat(employee.getCurrency()).isEqualTo(currencyByCountry.get(employee.getCountry())));
    }

    @Test
    void generatesPositiveSalaries() {
        assertThat(generator.generate(500)).allSatisfy(employee ->
                assertThat(employee.getSalary()).isGreaterThan(BigDecimal.ZERO));
    }

    @Test
    void coversEveryCountry() {
        assertThat(generator.generate(1000))
                .extracting(Employee::getCountry)
                .containsAll(EmployeeDataGenerator.COUNTRIES.stream()
                        .map(EmployeeDataGenerator.CountryProfile::country).toList());
    }

    @Test
    void producesTheSameDataForTheSameSeed() {
        List<Employee> first = new EmployeeDataGenerator(new Random(7)).generate(50);
        List<Employee> second = new EmployeeDataGenerator(new Random(7)).generate(50);

        assertThat(first).extracting(Employee::getEmail, Employee::getSalary)
                .isEqualTo(second.stream().map(e -> org.assertj.core.groups.Tuple.tuple(e.getEmail(), e.getSalary())).toList());
    }
}