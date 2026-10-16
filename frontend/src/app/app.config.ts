import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './core/auth-interceptor';
import { AuthService } from './core/auth';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // Angular automatically sends the XSRF-TOKEN cookie back as the X-XSRF-TOKEN header on changes
    provideHttpClient(withInterceptors([authInterceptor])),
    // find out who is signed in before the first page renders
    provideAppInitializer(() => inject(AuthService).load()),
  ],
};
