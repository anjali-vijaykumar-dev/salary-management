package com.acme.salary_management.seed;

import com.acme.salary_management.employee.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Builds realistic, reproducible employee data.
 * Salaries are based on a per-country base salary (local currency)
 * multiplied by a job-title factor, with +/-20% variation.
 */
public class EmployeeDataGenerator {

    record CountryProfile(String country, String currency, int baseSalary) {}

    record JobProfile(String title, String department, double multiplier) {}

    static final List<CountryProfile> COUNTRIES = List.of(
            new CountryProfile("India", "INR", 1_500_000),
            new CountryProfile("United States", "USD", 120_000),
            new CountryProfile("United Kingdom", "GBP", 65_000),
            new CountryProfile("Germany", "EUR", 70_000),
            new CountryProfile("Canada", "CAD", 95_000),
            new CountryProfile("Australia", "AUD", 110_000),
            new CountryProfile("Singapore", "SGD", 90_000));

    static final List<JobProfile> JOBS = List.of(
            new JobProfile("Software Engineer", "Engineering", 1.0),
            new JobProfile("Senior Software Engineer", "Engineering", 1.4),
            new JobProfile("Engineering Manager", "Engineering", 1.9),
            new JobProfile("QA Engineer", "Engineering", 0.85),
            new JobProfile("Data Analyst", "Data", 0.9),
            new JobProfile("Product Manager", "Product", 1.5),
            new JobProfile("UX Designer", "Design", 0.95),
            new JobProfile("HR Specialist", "Human Resources", 0.75),
            new JobProfile("Sales Executive", "Sales", 0.8),
            new JobProfile("Accountant", "Finance", 0.8));

    private static final List<String> FIRST_NAMES = List.of(
            "Aarav", "Priya", "Rahul", "Ananya", "Vikram", "Sneha", "James", "Emily", "Michael", "Sarah",
            "David", "Olivia", "Daniel", "Sophie", "Lukas", "Hannah", "Felix", "Mia", "Liam", "Chloe",
            "Noah", "Grace", "Ethan", "Zoe", "Wei", "Mei", "Kenji", "Yuki", "Arjun", "Isha");

    private static final List<String> LAST_NAMES = List.of(
            "Sharma", "Patel", "Iyer", "Reddy", "Nair", "Gupta", "Smith", "Johnson", "Brown", "Williams",
            "Taylor", "Wilson", "Muller", "Schmidt", "Fischer", "Weber", "Martin", "Roy", "Lee", "Chen",
            "Tan", "Wong", "Kumar", "Singh", "Clarke", "Evans", "Walker", "Hall", "Young", "King");

    private static final LocalDate EARLIEST_HIRE_DATE = LocalDate.of(2012, 1, 1);
    private static final int HIRE_WINDOW_DAYS = 5000;

    private final Random random;

    public EmployeeDataGenerator(Random random) {
        this.random = random;
    }

    public List<Employee> generate(int count) {
        List<Employee> employees = new ArrayList<>(count);
        for (int sequence = 1; sequence <= count; sequence++) {
            employees.add(generateOne(sequence));
        }
        return employees;
    }

    private Employee generateOne(int sequence) {
        String firstName = pick(FIRST_NAMES);
        String lastName = pick(LAST_NAMES);
        CountryProfile country = pick(COUNTRIES);
        JobProfile job = pick(JOBS);
        // sequence number guarantees a unique email even when names repeat
        String email = (firstName + "." + lastName + "." + sequence + "@acme.com").toLowerCase();

        return new Employee(firstName + " " + lastName, email, job.title(), job.department(),
                country.country(), country.currency(), salaryFor(country, job), hireDate());
    }

    private BigDecimal salaryFor(CountryProfile country, JobProfile job) {
        double variation = 0.8 + random.nextDouble() * 0.4;
        long salary = Math.round(country.baseSalary() * job.multiplier() * variation);
        long roundedToThousand = Math.round(salary / 1000.0) * 1000;
        return BigDecimal.valueOf(roundedToThousand);
    }

    private LocalDate hireDate() {
        return EARLIEST_HIRE_DATE.plusDays(random.nextInt(HIRE_WINDOW_DAYS));
    }

    private <T> T pick(List<T> items) {
        return items.get(random.nextInt(items.size()));
    }
}