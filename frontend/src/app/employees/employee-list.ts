import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, catchError, debounceTime, of, switchMap, tap } from 'rxjs';
import { EmployeeApi } from '../core/employee-api';
import { InsightsApi } from '../core/insights-api';
import { CountrySalaryStats, Employee, EmployeeQuery } from '../core/api.models';
import { EmployeeFormDialog } from './employee-form-dialog';
import { SalaryHistoryDialog } from './salary-history-dialog';

@Component({
  selector: 'app-employee-list',
  imports: [
    CurrencyPipe, DatePipe, DecimalPipe, MatTableModule, MatPaginatorModule, MatSortModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatProgressBarModule,
  ],
  templateUrl: './employee-list.html',
})
export class EmployeeList implements OnInit {
  private readonly api = inject(EmployeeApi);
  private readonly insightsApi = inject(InsightsApi);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  private readonly load$ = new Subject<void>();
  private readonly nameInput$ = new Subject<string>();

  readonly columns = ['fullName', 'email', 'jobTitle', 'country', 'salary', 'hireDate', 'actions'];
  readonly employees = signal<Employee[]>([]);
  readonly total = signal(0);
  readonly loading = signal(false);
  readonly countries = signal<CountrySalaryStats[]>([]);
  readonly jobTitles = signal<string[]>([]);
  readonly query = signal<EmployeeQuery>({
    name: '', country: '', jobTitle: '', page: 0, size: 20, sortBy: 'fullName', direction: 'asc',
  });

  ngOnInit(): void {
    // switchMap cancels an older request when filters change quickly, so stale results never overwrite new ones
    this.load$.pipe(
      tap(() => this.loading.set(true)),
      switchMap(() => this.api.search(this.query()).pipe(
        catchError(() => {
          this.snackBar.open('Could not load employees', 'OK', { duration: 3000 });
          return of(null);
        }),
      )),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(page => {
      this.loading.set(false);
      if (page) {
        this.employees.set(page.content);
        this.total.set(page.totalElements);
      }
    });

    // wait until the user stops typing before searching
    this.nameInput$.pipe(debounceTime(300), takeUntilDestroyed(this.destroyRef))
      .subscribe(name => this.updateQuery({ name, page: 0 }));

    this.loadFilterOptions();
    this.load$.next();
  }

  onNameInput(value: string): void {
    this.nameInput$.next(value);
  }

  onCountryChange(country: string): void {
    this.updateQuery({ country, page: 0 });
  }

  onJobTitleChange(jobTitle: string): void {
    this.updateQuery({ jobTitle, page: 0 });
  }

  clearFilters(): void {
    this.updateQuery({ name: '', country: '', jobTitle: '', page: 0 });
  }

  onPage(event: PageEvent): void {
    this.updateQuery({ page: event.pageIndex, size: event.pageSize });
  }

  onSort(sort: Sort): void {
    this.updateQuery({
      sortBy: sort.direction ? sort.active : 'fullName',
      direction: sort.direction || 'asc',
      page: 0,
    });
  }

  openCreate(): void {
    this.openForm();
  }

  openEdit(employee: Employee): void {
    this.openForm(employee);
  }

  openHistory(employee: Employee): void {
    this.dialog.open(SalaryHistoryDialog, { width: '680px', data: employee });
  }

  delete(employee: Employee): void {
    if (!confirm(`Delete ${employee.fullName}? This cannot be undone.`)) {
      return;
    }
    this.api.delete(employee.id).subscribe({
      next: () => {
        this.snackBar.open('Employee deleted', 'OK', { duration: 3000 });
        this.load$.next();
      },
      error: () => this.snackBar.open('Could not delete employee', 'OK', { duration: 3000 }),
    });
  }

  private openForm(employee?: Employee): void {
    this.dialog.open(EmployeeFormDialog, { width: '640px', data: { employee, countries: this.countries() } })
      .afterClosed()
      .subscribe(saved => {
        if (saved) {
          this.snackBar.open(employee ? 'Employee updated' : 'Employee added', 'OK', { duration: 3000 });
          this.load$.next();
          this.loadFilterOptions();
        }
      });
  }

  private updateQuery(changes: Partial<EmployeeQuery>): void {
    this.query.update(current => ({ ...current, ...changes }));
    this.load$.next();
  }

  private loadFilterOptions(): void {
    this.insightsApi.countries().subscribe(countries => this.countries.set(countries));
    this.insightsApi.jobTitles().subscribe(titles => this.jobTitles.set(titles));
  }
}
