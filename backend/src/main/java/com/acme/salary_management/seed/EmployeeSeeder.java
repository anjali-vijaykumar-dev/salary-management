package com.acme.salary_management.seed;

import com.acme.salary_management.employee.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Loads demo employees on startup, only when the database is empty,
 * so restarting the app never duplicates data.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class EmployeeSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSeeder.class);
    private static final long FIXED_SEED = 42L; // same demo data on every fresh start

    private final EmployeeRepository repository;
    private final int count;

    public EmployeeSeeder(EmployeeRepository repository, @Value("${app.seed.count:10000}") int count) {
        this.repository = repository;
        this.count = count;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed();
    }

    void seed() {
        if (repository.count() > 0) {
            log.info("Employees already present, skipping seed");
            return;
        }
        long start = System.currentTimeMillis();
        repository.saveAll(new EmployeeDataGenerator(new Random(FIXED_SEED)).generate(count));
        log.info("Seeded {} employees in {} ms", count, System.currentTimeMillis() - start);
    }
}