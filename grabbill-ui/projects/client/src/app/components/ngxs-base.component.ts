import { BaseComponent } from './base.component';
import { NavigationExtras, Params } from '@angular/router';
import { Navigate } from '@ngxs/router-plugin';
import { Select, Store } from '@ngxs/store';
import { Directive, OnDestroy, OnInit } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';
import { filter, Observable, switchMap, tap } from 'rxjs';
import { CommonState } from '../../states/common/common.state';
import { ResetMessage } from '../../states/common/common.state-actions';

@Directive()
export abstract class NgxsBaseComponent extends BaseComponent implements OnInit, OnDestroy {
  @Select(CommonState.message)
  message$!: Observable<{ messageType: string; message: string }>;

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
                .create(message.messageType, message.message, { nzDuration: 3000 })
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
