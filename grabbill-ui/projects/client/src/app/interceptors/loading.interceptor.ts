import { HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { debounceTime, finalize, Observable } from 'rxjs';
import { Store } from '@ngxs/store';
import { SetPageLoading } from '../../states/common/common.state-actions';

@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  private totalRequests = 0;

  constructor(protected store: Store) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<any> {
    if (req.method === 'POST' || req.method === 'PUT' || req.method === 'PATCH' || req.method === 'DELETE') {
      if (req.method === 'POST' && req.url.endsWith('/files')) {
        return next.handle(req);
      }
      this.totalRequests++;
      this.store.dispatch(new SetPageLoading(true));
      return next.handle(req).pipe(
        debounceTime(3000),
        finalize(() => {
          this.totalRequests--;
          if (this.totalRequests == 0) {
            this.store.dispatch(new SetPageLoading(false));
          }
        })
      );
    }
    return next.handle(req);
  }
}
