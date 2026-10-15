package com.acme.salary_management.employee;

import com.acme.salary_management.common.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(EmployeeService.class)
class EmployeeServiceTest {

    @Autowired
    private EmployeeService service;

    @Test
    void createsAnEmployee() {
        EmployeeResponse created = service.create(request("Asha Rao", "asha@acme.com", "India", "Software Engineer", "1800000"));

        assertThat(created.id()).isNotNull();
        assertThat(service.get(created.id()).fullName()).isEqualTo("Asha Rao");
    }

    @Test
    void rejectsADuplicateEmailIgnoringCase() {
        service.create(request("Asha Rao", "asha@acme.com", "India", "Software Engineer", "1800000"));

        assertThatThrownBy(() -> service.create(request("Asha R", "ASHA@acme.com", "India", "QA Engineer", "1200000")))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void throwsNotFoundForAnUnknownEmployee() {
        assertThatThrownBy(() -> service.get(999L)).isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void updatesAnEmployee() {
        EmployeeResponse created = service.create(request("Asha Rao", "asha@acme.com", "India", "Software Engineer", "1800000"));

        EmployeeResponse updated = service.update(created.id(),
                request("Asha Rao", "asha@acme.com", "India", "Senior Software Engineer", "2500000"));

        assertThat(updated.jobTitle()).isEqualTo("Senior Software Engineer");
        assertThat(updated.salary()).isEqualByComparingTo("2500000");
    }

    @Test
    void rejectsUpdatingToAnotherEmployeesEmail() {
        service.create(request("Asha Rao", "asha@acme.com", "India", "Software Engineer", "1800000"));
        EmployeeResponse ben = service.create(request("Ben Lee", "ben@acme.com", "Singapore", "Data Analyst", "80000"));

        assertThatThrownBy(() -> service.update(ben.id(),
                request("Ben Lee", "asha@acme.com", "Singapore", "Data Analyst", "80000")))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void deletesAnEmployee() {
        EmployeeResponse created = service.create(request("Asha Rao", "asha@acme.com", "India", "Software Engineer", "1800000"));

        service.delete(created.id());

        assertThatThrownBy(() -> service.get(created.id())).isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void throwsNotFoundWhenDeletingAnUnknownEmployee() {
        assertThatThrownBy(() -> service.delete(999L)).isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void filtersByCountryAndJobTitle() {
        service.create(request("Asha Rao", "asha@acme.com", "India", "Software Engineer", "1800000"));
        service.create(request("Ravi Nair", "ravi@acme.com", "India", "Data Analyst", "1400000"));
        service.create(request("Ben Lee", "ben@acme.com", "Singapore", "Software Engineer", "90000"));

        PageResponse<EmployeeResponse> result = service.search(
                new EmployeeSearchCriteria(null, "India", "Software Engineer"), PageRequest.of(0, 10));

        assertThat(result.content()).extracting(EmployeeResponse::fullName).containsExactly("Asha Rao");
    }

    @Test
    void searchesByNameIgnoringCase() {
        service.create(request("Asha Rao", "asha@acme.com", "India", "Software Engineer", "1800000"));
        service.create(request("Ben Lee", "ben@acme.com", "Singapore", "Software Engineer", "90000"));

        PageResponse<EmployeeResponse> result = service.search(
                new EmployeeSearchCriteria("ASHA", null, null), PageRequest.of(0, 10));

        assertThat(result.content()).extracting(EmployeeResponse::fullName).containsExactly("Asha Rao");
    }

    @Test
    void returnsResultsOnePageAtATime() {
        for (int i = 1; i <= 5; i++) {
            service.create(request("Employee " + i, "e" + i + "@acme.com", "India", "Software Engineer", "1000000"));
        }

        PageResponse<EmployeeResponse> page = service.search(
                new EmployeeSearchCriteria(null, null, null), PageRequest.of(0, 2));

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.totalPages()).isEqualTo(3);
    }

    private EmployeeRequest request(String name, String email, String country, String jobTitle, String salary) {
        String currency = country.equals("India") ? "INR" : "SGD";
        return new EmployeeRequest(name, email, jobTitle, "Engineering", country, currency,
                new BigDecimal(salary), LocalDate.of(2022, 1, 10));
    }
}