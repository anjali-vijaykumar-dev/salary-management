import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EmployeeApi } from './employee-api';
import { EmployeeQuery } from './api.models';

describe('EmployeeApi', () => {
  let api: EmployeeApi;
  let http: HttpTestingController;

  const query: EmployeeQuery = {
    name: '', country: 'India', jobTitle: '', page: 2, size: 20, sortBy: 'salary', direction: 'desc',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(EmployeeApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends paging and sorting, and only the filters that are set', () => {
    api.search(query).subscribe();

    const req = http.expectOne(r => r.url === '/api/employees');
    expect(req.request.params.get('country')).toBe('India');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('sortBy')).toBe('salary');
    expect(req.request.params.get('direction')).toBe('desc');
    expect(req.request.params.has('name')).toBe(false);
    expect(req.request.params.has('jobTitle')).toBe(false);
    req.flush({ content: [], page: 2, size: 20, totalElements: 0, totalPages: 0 });
  });

  it('trims the name before searching', () => {
    api.search({ ...query, name: '  asha  ' }).subscribe();

    const req = http.expectOne(r => r.url === '/api/employees');
    expect(req.request.params.get('name')).toBe('asha');
    req.flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
  });

  it('loads the salary history of an employee', () => {
    api.salaryHistory(7).subscribe();

    http.expectOne('/api/employees/7/salary-history').flush([]);
  });
});
