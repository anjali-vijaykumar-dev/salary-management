import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, finalize, of, tap } from 'rxjs';

export interface CurrentUser {
  name: string;
  roles: string[];
}

/** Knows who is signed in, so the UI can hide actions the user is not allowed to perform. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  readonly user = signal<CurrentUser | null>(null);
  readonly hasAccess = computed(() => (this.user()?.roles.length ?? 0) > 0);
  readonly canEdit = computed(() => this.user()?.roles.includes('HR_MANAGER') ?? false);

  load(): Observable<CurrentUser | null> {
    return this.http.get<CurrentUser>('/api/me').pipe(
      tap(user => this.user.set(user)),
      catchError(() => of(null)),
    );
  }

  logout(): void {
    this.http.post('/logout', {}, { responseType: 'text' })
      .pipe(finalize(() => window.location.assign('/login?logout')))
      .subscribe({ error: () => undefined });
  }
}
