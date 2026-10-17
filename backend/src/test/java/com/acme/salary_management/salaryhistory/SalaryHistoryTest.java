package com.acme.salary_management.salaryhistory;

import com.acme.salary_management.employee.EmployeeNotFoundException;
import com.acme.salary_management.employee.EmployeeRequest;
import com.acme.salary_management.employee.EmployeeResponse;
import com.acme.salary_management.employee.EmployeeService;
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
@Import({EmployeeService.class, SalaryRevisionRecorder.class, SalaryHistoryService.class})
class SalaryHistoryTest {

    @Autowired
    private EmployeeService employees;

    @Autowired
    private SalaryHistoryService history;

    @Test
    void recordsARevisionWhenTheSalaryChanges() {
        EmployeeResponse asha = employees.create(request("Software Engineer", "1000000", null));

        employees.update(asha.id(), request("Software Engineer", "1250000", "Annual appraisal"));

        List<SalaryRevisionResponse> revisions = history.historyFor(asha.id());
        assertThat(revisions).hasSize(1);
        SalaryRevisionResponse revision = revisions.get(0);
        assertThat(revision.oldSalary()).isEqualByComparingTo("1000000");
        assertThat(revision.newSalary()).isEqualByComparingTo("1250000");
        assertThat(revision.currency()).isEqualTo("INR");
        assertThat(revision.reason()).isEqualTo("Annual appraisal");
        assertThat(revision.changePercent()).isEqualByComparingTo("25.0");
        assertThat(revision.changedAt()).isNotNull();
    }

    @Test
    void doesNotRecordARevisionWhenOnlyOtherFieldsChange() {
        EmployeeResponse asha = employees.create(request("Software Engineer", "1000000", null));

        employees.update(asha.id(), request("Senior Software Engineer", "1000000", null));

        assertThat(history.historyFor(asha.id())).isEmpty();
    }

    @Test
    void returnsTheNewestRevisionFirst() {
        EmployeeResponse asha = employees.create(request("Software Engineer", "1000000", null));

        employees.update(asha.id(), request("Software Engineer", "1100000", "Probation completed"));
        employees.update(asha.id(), request("Senior Software Engineer", "1500000", "Promotion"));

        assertThat(history.historyFor(asha.id()))
                .extracting(SalaryRevisionResponse::reason)
                .containsExactly("Promotion", "Probation completed");
    }

    @Test
    void throwsNotFoundForAnUnknownEmployee() {
        assertThatThrownBy(() -> history.historyFor(999L)).isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void aNewEmployeeDoesNotInheritTheHistoryOfADeletedOne() {
        EmployeeResponse old = employees.create(request("Software Engineer", "6000000", null));
        employees.update(old.id(), request("Software Engineer", "60000000", "HIKE"));
        employees.delete(old.id());

        EmployeeResponse kavya = employees.create(new EmployeeRequest("Kavya", "kavya@acme.com", "Software Engineer",
                "Engineering", "India", "INR", new BigDecimal("3000000"), LocalDate.of(2022, 4, 1), null));

        assertThat(history.historyFor(kavya.id())).isEmpty();
    }

    private EmployeeRequest request(String jobTitle, String salary, String reason) {
        return new EmployeeRequest("Asha Rao", "asha@acme.com", jobTitle, "Engineering", "India", "INR",
                new BigDecimal(salary), LocalDate.of(2022, 4, 1), reason);
    }
}