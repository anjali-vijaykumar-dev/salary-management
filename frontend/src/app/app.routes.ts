import { Routes } from '@angular/router';
import { EmployeeList } from './employees/employee-list';
import { InsightsPage } from './insights/insights-page';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  { path: 'employees', component: EmployeeList, title: 'Employees' },
  { path: 'insights', component: InsightsPage, title: 'Salary insights' },
  { path: '**', redirectTo: 'employees' },
];
