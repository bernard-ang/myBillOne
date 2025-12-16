import { HttpErrorResponse, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, filter, switchMap, take, tap } from 'rxjs/operators';
import { Store } from '@ngxs/store';
import { UpdateError } from '../../states/common/common.state-actions';
import { Logout, RefreshToken } from '../../states/auth/auth.state-actions';
import { ApiErrorMessageModel, ApiResponseModel, ErrorModel } from '@grabbill/lib';
import { AuthState } from '../../states/auth/auth.state';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
  refreshTokenInProgress = false;
  refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(private router: Router, protected store: Store) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<any> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error(error);
        this.store.dispatch(new UpdateError());
        let currentError: ErrorModel;
        if (error.status === 0) {
          this.router.navigate(['login']);
        }
        if (error.error instanceof ErrorEvent) {
          // client-side error
          return throwError(() => ({
            statusCode: 0,
            message: error.error.message,
          }));
        } else {
          // server-side error
          if (error.status !== 401) {
            const url = error.url ? error.url : undefined;
            this.store.dispatch(new UpdateError(url));

            const response = error.error as ApiResponseModel<ApiErrorMessageModel>;
            if (response.data && response.data.errorCode === 'GRB0013') {
              const email = this.store.selectSnapshot(AuthState.email);
              this.router.navigate(['/', 'register', 'verify-email'], { queryParams: { email } });
            }

            if (error.status === 0) {
              return throwError(() => error.statusText);
            } else if (error.error.data.errorDetails) {
              return throwError(() => error.error.data.errorDetails);
            } else {
              return throwError(() => error.error.data.message);
            }
          }

          return this.handleUnauthorizedAccess(next, req, error.error);
        }
      })
    );
  }

  private handleUnauthorizedAccess(next: HttpHandler, req: HttpRequest<any>, res: HttpErrorResponse) {
    if (req.url.includes('refresh')) {
      // 401 on refresh endpoint, logout & redirect to login page
      return this.store.dispatch(new Logout()).pipe(
        tap(() => {
          this.router.navigate(['login']);
          return throwError({ error: 'Refresh token failed' });
        })
      );
    } else {
      const response = res as any as ApiResponseModel<ApiErrorMessageModel>;
      if (response.data && response.data.errorCode === 'GRB0009') {
        if (this.refreshTokenInProgress) {
          return this.refreshTokenSubject.pipe(
            filter((result) => result !== null),
            take(1),
            switchMap(() => next.handle(req))
          );
        } else {
          this.refreshTokenInProgress = true;
          this.refreshTokenSubject.next(null);

          return this.store.dispatch(new RefreshToken()).pipe(
            switchMap(() => {
              this.refreshTokenInProgress = false;
              this.refreshTokenSubject.next('refreshed');

              return next.handle(req);
            }),
            catchError((err) => {
              this.refreshTokenInProgress = false;
              console.error(err);
              return err;
            })
          );
        }
      } else {
        if (!req.url.includes('login')) {
          // if not login page redirect to login page
          this.router.navigate(['login']);
        }
        return throwError(() => ({ errorDetails: 'Not authorized' }));
      }
    }
  }
}
