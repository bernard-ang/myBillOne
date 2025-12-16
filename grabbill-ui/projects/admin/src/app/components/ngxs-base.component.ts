import { BaseComponent } from './base.component';
import { NavigationExtras, Params, RouterStateSnapshot } from '@angular/router';
import { Navigate } from '@ngxs/router-plugin';
import { Select, Store } from '@ngxs/store';
import { Directive, OnDestroy, OnInit } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';
import { filter, map, Observable, switchMap, tap } from 'rxjs';
import { AdminCommonState } from '../../states/admin-common/admin-common.state';
import { ResetMessage } from '../../states/admin-common/admin-common.state-actions';

@Directive()
export abstract class NgxsBaseComponent extends BaseComponent implements OnInit, OnDestroy {
  @Select(AdminCommonState.message)
  message$!: Observable<{ messageType: string; message: string }>;

  activeRoute$ = this.store
    .select<{ state: RouterStateSnapshot }>((state) => state['router'])
    .pipe(
      map((val) => {
        const {
          url,
          root: { queryParams },
        } = val.state;
        let { root: route } = val.state;
        while (route.firstChild) {
          route = route.firstChild;
        }

        const { params, data } = route;
        return { url, params, queryParams, data };
      })
    );

  protected constructor(protected store: Store, protected messageService?: NzMessageService) {
    super();
  }

  ngOnInit(): void {
    if (this.messageService) {
      this.autoUnsubscribe(
        this.message$.pipe(
          filter((message: { messageType: string; message: string }) => !!message),
          tap((message: { messageType: string; message: string }) => {
            if (this.messageService) {
              this.messageService
                .create(message.messageType, message.message, { nzDuration: 2000 })
                .onClose.pipe(switchMap(() => this.store.dispatch(new ResetMessage())));
            }
          })
        )
      );
    }
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    this.store.dispatch(new ResetMessage());
  }

  navigate(path: unknown[], queryParams?: Params, extras?: NavigationExtras) {
    return this.store.dispatch(new Navigate(path, queryParams, extras));
  }
}
