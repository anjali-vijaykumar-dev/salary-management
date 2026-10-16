import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

/** When the session has expired (401), send the user to the login page. */
export const authInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        window.location.assign('/login');
      }
      return throwError(() => error);
    }),
  );
