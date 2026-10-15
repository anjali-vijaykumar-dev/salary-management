package com.acme.salary_management.insights;

import com.acme.salary_management.employee.Employee;
import com.acme.salary_management.employee.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(InsightsService.class)
class InsightsServiceTest {

    @Autowired
    private EmployeeRepository employees;

    @Autowired
    private InsightsService insights;

    @BeforeEach
    void setUp() {
        employees.saveAll(List.of(
                employee("a@acme.com", "India", "INR", "Software Engineer", "1000000"),
                employee("b@acme.com", "India", "INR", "Software Engineer", "2000000"),
                employee("c@acme.com", "India", "INR", "Engineering Manager", "3000000"),
                employee("d@acme.com", "Singapore", "SGD", "Software Engineer", "90000")));
    }

    @Test
    void computesHeadcountAndMinMaxAverageSalaryPerCountry() {
        List<CountrySalaryStats> stats = insights.countries();

        assertThat(stats).extracting(CountrySalaryStats::country).containsExactly("India", "Singapore");
        CountrySalaryStats india = stats.get(0);
        assertThat(india.currency()).isEqualTo("INR");
        assertThat(india.headcount()).isEqualTo(3);
        assertThat(india.minSalary()).isEqualByComparingTo("1000000");
        assertThat(india.maxSalary()).isEqualByComparingTo("3000000");
        assertThat(india.averageSalary()).isEqualByComparingTo("2000000");
    }

    @Test
    void breaksDownACountryByJobTitleHighestAverageFirst() {
        CountryInsights india = insights.country("India");

        assertThat(india.summary().headcount()).isEqualTo(3);
        assertThat(india.byJobTitle()).extracting(JobTitleSalaryStats::jobTitle)
                .containsExactly("Engineering Manager", "Software Engineer");
        JobTitleSalaryStats engineers = india.byJobTitle().get(1);
        assertThat(engineers.headcount()).isEqualTo(2);
        assertThat(engineers.averageSalary()).isEqualByComparingTo("1500000");
    }

    @Test
    void throwsNotFoundForACountryWithNoEmployees() {
        assertThatThrownBy(() -> insights.country("Atlantis")).isInstanceOf(CountryNotFoundException.class);
    }

    @Test
    void listsDistinctJobTitlesAlphabetically() {
        assertThat(insights.jobTitles()).containsExactly("Engineering Manager", "Software Engineer");
    }

    private Employee employee(String email, String country, String currency, String jobTitle, String salary) {
        return new Employee("Test User", email, jobTitle, "Engineering", country, currency,
                new BigDecimal(salary), LocalDate.of(2020, 1, 1));
    }
}