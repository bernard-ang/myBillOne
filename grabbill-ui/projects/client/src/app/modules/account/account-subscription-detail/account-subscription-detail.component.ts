import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { InvoiceState } from '../../../../states/invoice/invoice.state';
import { Observable, of, switchMap, tap } from 'rxjs';
import { getErrorMessage, InvoiceModel } from '@grabbill/lib';
import { ActivatedRoute, Params } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { GetInvoiceBySubscriptionId, ResetInvoice } from '../../../../states/invoice/invoice.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { environment } from '../../../../environments/environment';
import prettyBytes from 'pretty-bytes';

@Component({
  selector: 'grabbill-client-account-subscription-detail',
  templateUrl: './account-subscription-detail.component.html',
  styleUrls: ['./account-subscription-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountSubscriptionDetailComponent extends NgxsBaseComponent {
  @Select(InvoiceState.invoice)
  invoice$!: Observable<InvoiceModel>;

  accountSubscriptionId?: number;

  constructor(
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private modal: NzModalService,
    private actions$: Actions,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetInvoice());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.accountSubscriptionId = params['id'];
          this.store.dispatch(new GetInvoiceBySubscriptionId(this.accountSubscriptionId!));
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetInvoiceBySubscriptionId),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      )
    );
  }

  dateFormat() {
    return environment.config.dateFormat;
  }

  formatBytes(bytes: number) {
    return prettyBytes(bytes, { locale: true });
  }
}
