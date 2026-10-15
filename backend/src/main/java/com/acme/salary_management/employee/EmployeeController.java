package com.acme.salary_management.employee;

import com.acme.salary_management.common.BadRequestException;
import com.acme.salary_management.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private static final Set<String> SORTABLE_FIELDS = Set.of("fullName", "salary", "hireDate", "country", "jobTitle");
    private static final int MAX_PAGE_SIZE = 100;

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EmployeeResponse> list(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) String country,
                                               @RequestParam(required = false) String jobTitle,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(defaultValue = "fullName") String sortBy,
                                               @RequestParam(defaultValue = "asc") String direction) {
        return service.search(new EmployeeSearchCriteria(name, country, jobTitle),
                pageRequest(page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/employees/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable pageRequest(int page, int size, String sortBy, String direction) {
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Cannot sort by '" + sortBy + "'. Allowed: " + SORTABLE_FIELDS);
        }
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        // secondary sort by id keeps paging stable when values repeat
        return PageRequest.of(Math.max(page, 0), safeSize, Sort.by(sortDirection, sortBy).and(Sort.by("id")));
    }
}