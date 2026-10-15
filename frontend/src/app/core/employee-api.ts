import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Employee, EmployeeQuery, EmployeeRequest, Page, SalaryRevision } from './api.models';

@Injectable({ providedIn: 'root' })
export class EmployeeApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/employees';

  search(query: EmployeeQuery): Observable<Page<Employee>> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sortBy', query.sortBy)
      .set('direction', query.direction);
    if (query.name.trim()) {
      params = params.set('name', query.name.trim());
    }
    if (query.country) {
      params = params.set('country', query.country);
    }
    if (query.jobTitle) {
      params = params.set('jobTitle', query.jobTitle);
    }
    return this.http.get<Page<Employee>>(this.baseUrl, { params });
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, request);
  }

  update(id: number, request: EmployeeRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  salaryHistory(id: number): Observable<SalaryRevision[]> {
    return this.http.get<SalaryRevision[]>(`${this.baseUrl}/${id}/salary-history`);
  }
}