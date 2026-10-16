import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth';

describe('AuthService', () => {
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lets an HR manager edit', () => {
    auth.load().subscribe();
    http.expectOne('/api/me').flush({ name: 'hr.manager', roles: ['HR_MANAGER'] });

    expect(auth.hasAccess()).toBe(true);
    expect(auth.canEdit()).toBe(true);
  });

  it('gives an HR viewer read-only access', () => {
    auth.load().subscribe();
    http.expectOne('/api/me').flush({ name: 'hr.viewer', roles: ['HR_VIEWER'] });

    expect(auth.hasAccess()).toBe(true);
    expect(auth.canEdit()).toBe(false);
  });

  it('gives no access to a user without a role', () => {
    auth.load().subscribe();
    http.expectOne('/api/me').flush({ name: 'stranger@gmail.com', roles: [] });

    expect(auth.hasAccess()).toBe(false);
  });
});
