package com.acme.salary_management.insights;

import com.acme.salary_management.employee.EmployeeRepository;
import com.acme.salary_management.seed.EmployeeDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs the insight queries against 10,000 employees and checks they meet the 500 ms target. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(InsightsService.class)
class InsightsPerformanceTest {

    private static final long MAX_MILLIS = 500;

    @Autowired
    private EmployeeRepository employees;

    @Autowired
    private InsightsService insights;

    @Test
    void insightQueriesStayUnder500msWith10000Employees() {
        employees.saveAll(new EmployeeDataGenerator(new Random(42)).generate(10_000));

        List<CountrySalaryStats> countries = timed("all countries", insights::countries);
        timed("India by job title", () -> insights.country("India"));

        assertThat(countries.stream().mapToLong(CountrySalaryStats::headcount).sum()).isEqualTo(10_000);
    }

    private <T> T timed(String label, Supplier<T> query) {
        long start = System.nanoTime();
        T result = query.get();
        long millis = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("[perf] %s: %d ms%n", label, millis);
        assertThat(millis).as(label + " took " + millis + " ms").isLessThan(MAX_MILLIS);
        return result;
    }
}