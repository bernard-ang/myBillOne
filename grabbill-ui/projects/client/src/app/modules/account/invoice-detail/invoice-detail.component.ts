import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { Observable, of, switchMap, tap } from 'rxjs';
import { getErrorMessage, InvoiceModel, InvoiceStatus, InvoiceType, SubscriptionMode } from "@grabbill/lib";
import { InvoiceState } from '../../../../states/invoice/invoice.state';
import { ActivatedRoute, Params } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { GetInvoice, ResetInvoice } from '../../../../states/invoice/invoice.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { environment } from '../../../../environments/environment';
import { getInvoiceStatusTag } from '../../../../utils/get-status-tag';
import prettyBytes from 'pretty-bytes';
import { getInvoiceAddress } from "@grabbill/lib";

@Component({
  selector: 'grabbill-client-invoice-detail',
  templateUrl: './invoice-detail.component.html',
  styleUrls: ['./invoice-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InvoiceDetailComponent extends NgxsBaseComponent {
  @Select(InvoiceState.invoice)
  invoice$!: Observable<InvoiceModel>;

  invoiceId?: number;
  invoiceType = InvoiceType;

  subscriptionMode = SubscriptionMode;
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
          this.invoiceId = params['id'];
          this.store.dispatch(new GetInvoice(this.invoiceId!));
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetInvoice),
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

  getStatusTag(status: InvoiceStatus) {
    return getInvoiceStatusTag(status);
  }

  getAddress(invoice: InvoiceModel) {
    return getInvoiceAddress(invoice);
  }

  formatBytes(bytes: number) {
    return prettyBytes(bytes, { locale: true });
  }
}
