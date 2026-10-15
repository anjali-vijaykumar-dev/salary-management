import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { EmployeeApi } from '../core/employee-api';
import { Employee, SalaryRevision } from '../core/api.models';

@Component({
  selector: 'app-salary-history-dialog',
  imports: [CurrencyPipe, DatePipe, MatDialogModule, MatTableModule, MatButtonModule, MatProgressBarModule],
  template: `
    <h2 mat-dialog-title>Salary history – {{ employee.fullName }}</h2>
    <mat-dialog-content>
      @if (loading()) {
        <mat-progress-bar mode="indeterminate" />
      } @else if (revisions().length === 0) {
        <p>No salary changes recorded yet.</p>
      } @else {
        <table mat-table [dataSource]="revisions()">
          <ng-container matColumnDef="changedAt">
            <th mat-header-cell *matHeaderCellDef>Date</th>
            <td mat-cell *matCellDef="let r">{{ r.changedAt | date: 'medium' }}</td>
          </ng-container>
          <ng-container matColumnDef="change">
            <th mat-header-cell *matHeaderCellDef>Change</th>
            <td mat-cell *matCellDef="let r">
              {{ r.oldSalary | currency: r.currency : 'symbol' : '1.0-0' }} →
              {{ r.newSalary | currency: r.currency : 'symbol' : '1.0-0' }}
            </td>
          </ng-container>
          <ng-container matColumnDef="percent">
            <th mat-header-cell *matHeaderCellDef class="num">%</th>
            <td mat-cell *matCellDef="let r" class="num">{{ formatPercent(r.changePercent) }}</td>
          </ng-container>
          <ng-container matColumnDef="reason">
            <th mat-header-cell *matHeaderCellDef>Reason</th>
            <td mat-cell *matCellDef="let r">{{ r.reason || '—' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
})
export class SalaryHistoryDialog implements OnInit {
  readonly employee = inject<Employee>(MAT_DIALOG_DATA);
  private readonly api = inject(EmployeeApi);

  readonly columns = ['changedAt', 'change', 'percent', 'reason'];
  readonly revisions = signal<SalaryRevision[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.api.salaryHistory(this.employee.id).subscribe({
      next: revisions => {
        this.revisions.set(revisions);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  formatPercent(percent: number | null): string {
    if (percent === null) {
      return '—';
    }
    return `${percent > 0 ? '+' : ''}${percent.toFixed(1)}%`;
  }
}
