import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { InsightsApi } from '../core/insights-api';
import { CountryInsights, CountrySalaryStats } from '../core/api.models';

@Component({
  selector: 'app-insights-page',
  imports: [CurrencyPipe, DecimalPipe, MatTableModule],
  templateUrl: './insights-page.html',
})
export class InsightsPage implements OnInit {
  private readonly api = inject(InsightsApi);

  readonly countries = signal<CountrySalaryStats[]>([]);
  readonly selected = signal<CountryInsights | null>(null);
  readonly totalHeadcount = computed(() => this.countries().reduce((sum, c) => sum + c.headcount, 0));

  readonly countryColumns = ['country', 'headcount', 'minSalary', 'averageSalary', 'maxSalary'];
  readonly jobColumns = ['jobTitle', 'headcount', 'minSalary', 'averageSalary', 'maxSalary'];

  ngOnInit(): void {
    this.api.countries().subscribe(countries => {
      this.countries.set(countries);
      if (countries.length > 0) {
        this.selectCountry(countries[0].country);
      }
    });
  }

  selectCountry(country: string): void {
    this.api.country(country).subscribe(insights => this.selected.set(insights));
  }
}
