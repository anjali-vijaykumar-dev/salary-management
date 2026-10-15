import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CountryInsights, CountrySalaryStats } from './api.models';

@Injectable({ providedIn: 'root' })
export class InsightsApi {
  private readonly http = inject(HttpClient);

  countries(): Observable<CountrySalaryStats[]> {
    return this.http.get<CountrySalaryStats[]>('/api/insights/countries');
  }

  country(name: string): Observable<CountryInsights> {
    return this.http.get<CountryInsights>(`/api/insights/countries/${encodeURIComponent(name)}`);
  }

  jobTitles(): Observable<string[]> {
    return this.http.get<string[]>('/api/insights/job-titles');
  }
}