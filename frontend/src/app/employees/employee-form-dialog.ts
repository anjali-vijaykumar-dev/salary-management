import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { EmployeeApi } from '../core/employee-api';
import { ApiError, CountrySalaryStats, Employee, EmployeeRequest } from '../core/api.models';

export interface EmployeeFormData {
  employee?: Employee;
  countries: CountrySalaryStats[];
}

@Component({
  selector: 'app-employee-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit employee' : 'Add employee' }}</h2>
    <form [formGroup]="form" (ngSubmit)="save()">
      <mat-dialog-content class="form-grid">
        <mat-form-field appearance="outline">
          <mat-label>Full name</mat-label>
          <input matInput formControlName="fullName" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Email</mat-label>
          <input matInput type="email" formControlName="email" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Job title</mat-label>
          <input matInput formControlName="jobTitle" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Department</mat-label>
          <input matInput formControlName="department" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Country</mat-label>
          <mat-select formControlName="country" (selectionChange)="onCountryChange($event.value)">
            @for (c of countries; track c.country) {
              <mat-option [value]="c.country">{{ c.country }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Currency</mat-label>
          <input matInput formControlName="currency" />
          <mat-hint>Set from country</mat-hint>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Annual salary</mat-label>
          <input matInput type="number" min="1" formControlName="salary" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Hire date</mat-label>
          <input matInput type="date" formControlName="hireDate" />
        </mat-form-field>

        @if (isEdit) {
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Reason for salary change (optional)</mat-label>
            <input matInput formControlName="salaryChangeReason" />
            <mat-hint>Saved in salary history if the salary changes</mat-hint>
          </mat-form-field>
        }

        @if (errorMessage()) {
          <p class="form-error">{{ errorMessage() }}</p>
        }
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-flat-button type="submit" [disabled]="saving()">Save</button>
      </mat-dialog-actions>
    </form>
  `,
  styles: `
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 4px 16px; }
    .full-width, .form-error { grid-column: 1 / -1; }
    .form-error { color: #b3261e; margin: 0; }
  `,
})
export class EmployeeFormDialog {
  private readonly data = inject<EmployeeFormData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject<MatDialogRef<EmployeeFormDialog>>(MatDialogRef);
  private readonly api = inject(EmployeeApi);
  private readonly fb = inject(FormBuilder);

  readonly isEdit = !!this.data.employee;
  readonly countries = this.data.countries;
  readonly saving = signal(false);
  readonly errorMessage = signal('');

  readonly form = this.fb.nonNullable.group({
    fullName: [this.data.employee?.fullName ?? '', [Validators.required, Validators.maxLength(100)]],
    email: [this.data.employee?.email ?? '', [Validators.required, Validators.email]],
    jobTitle: [this.data.employee?.jobTitle ?? '', Validators.required],
    department: [this.data.employee?.department ?? '', Validators.required],
    country: [this.data.employee?.country ?? '', Validators.required],
    // currency follows the country, so HR cannot pair India with USD by mistake
    currency: [{ value: this.data.employee?.currency ?? '', disabled: true }, Validators.required],
    salary: [this.data.employee?.salary ?? 0, [Validators.required, Validators.min(1)]],
    hireDate: [this.data.employee?.hireDate ?? '', Validators.required],
    salaryChangeReason: ['', Validators.maxLength(255)],
  });

  onCountryChange(country: string): void {
    const match = this.countries.find(c => c.country === country);
    this.form.controls.currency.setValue(match?.currency ?? '');
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.errorMessage.set('');

    const value = this.form.getRawValue();
    const request: EmployeeRequest = { ...value, salaryChangeReason: value.salaryChangeReason || null };
    const call = this.isEdit
      ? this.api.update(this.data.employee!.id, request)
      : this.api.create(request);

    call.subscribe({
      next: () => this.dialogRef.close(true),
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(this.describe(err));
      },
    });
  }

  private describe(err: HttpErrorResponse): string {
    const body = err.error as ApiError | null;
    if (body?.fieldErrors && Object.keys(body.fieldErrors).length > 0) {
      return Object.entries(body.fieldErrors).map(([field, message]) => `${field}: ${message}`).join(', ');
    }
    return body?.message ?? 'Something went wrong. Please try again.';
  }
}
