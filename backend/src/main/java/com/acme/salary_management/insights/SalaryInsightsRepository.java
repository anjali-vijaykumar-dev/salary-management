package com.acme.salary_management.insights;

import com.acme.salary_management.employee.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Read-only aggregation queries. All grouping happens in the database, not in Java. */
public interface SalaryInsightsRepository extends Repository<Employee, Long> {

    @Query("""
            select new com.acme.salary_management.insights.CountrySalaryStats(
                e.country, e.currency, count(e), min(e.salary), max(e.salary), avg(e.salary))
            from Employee e
            group by e.country, e.currency
            order by e.country
            """)
    List<CountrySalaryStats> salaryStatsByCountry();

    @Query("""
            select new com.acme.salary_management.insights.CountrySalaryStats(
                e.country, e.currency, count(e), min(e.salary), max(e.salary), avg(e.salary))
            from Employee e
            where e.country = :country
            group by e.country, e.currency
            """)
    List<CountrySalaryStats> salaryStatsForCountry(@Param("country") String country);

    @Query("""
            select new com.acme.salary_management.insights.JobTitleSalaryStats(
                e.jobTitle, count(e), min(e.salary), max(e.salary), avg(e.salary))
            from Employee e
            where e.country = :country
            group by e.jobTitle
            order by avg(e.salary) desc
            """)
    List<JobTitleSalaryStats> salaryStatsByJobTitle(@Param("country") String country);

    @Query("select distinct e.jobTitle from Employee e order by e.jobTitle")
    List<String> distinctJobTitles();
}