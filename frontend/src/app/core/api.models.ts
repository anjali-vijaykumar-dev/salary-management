export interface Employee {
  id: number;
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  country: string;
  currency: string;
  salary: number;
  hireDate: string;
}

export interface EmployeeRequest {
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  country: string;
  currency: string;
  salary: number;
  hireDate: string;
  salaryChangeReason?: string | null;
}

export interface EmployeeQuery {
  name: string;
  country: string;
  jobTitle: string;
  page: number;
  size: number;
  sortBy: string;
  direction: 'asc' | 'desc';
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CountrySalaryStats {
  country: string;
  currency: string;
  headcount: number;
  minSalary: number;
  maxSalary: number;
  averageSalary: number;
}

export interface JobTitleSalaryStats {
  jobTitle: string;
  headcount: number;
  minSalary: number;
  maxSalary: number;
  averageSalary: number;
}

export interface CountryInsights {
  summary: CountrySalaryStats;
  byJobTitle: JobTitleSalaryStats[];
}

export interface SalaryRevision {
  id: number;
  oldSalary: number;
  newSalary: number;
  currency: string;
  reason: string | null;
  changePercent: number | null;
  changedAt: string;
}

export interface ApiError {
  status: number;
  message: string;
  fieldErrors: Record<string, string>;
}