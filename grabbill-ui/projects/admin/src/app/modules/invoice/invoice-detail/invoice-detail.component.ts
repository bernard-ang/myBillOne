import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { Select, Store } from '@ngxs/store';
import { Observable, tap } from 'rxjs';
import {
  getInvoiceAddress,
  getInvoiceStatusTag,
  getPaymentStatusTag,
  InvoiceModel,
  InvoicePaymentTransactionsPayloadModel, InvoiceType,
  PaymentTransactionModel, SubscriptionMode
} from "@grabbill/lib";
import { ActivatedRoute, Params } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { environment } from '../../../../environments/environment';
import { InvoiceManagementState } from '../../../../states/invoice-management/invoice-management.state';
import prettyBytes from 'pretty-bytes';
import { GetInvoice, ResetInvoice } from '../../../../states/invoice-management/invoice-management.state-actions';

@Component({
  selector: 'grabbill-admin-invoice-detail',
  templateUrl: './invoice-detail.component.html',
  styleUrls: ['./invoice-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InvoiceDetailComponent extends NgxsBaseComponent {
  @Select(InvoiceManagementState.invoice)
  invoice$!: Observable<InvoicePaymentTransactionsPayloadModel>;

  id?: number;

  invoiceType = InvoiceType;
  subscriptionMode = SubscriptionMode;

  constructor(
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
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
          this.id = params['id'];
          this.store.dispatch(new GetInvoice(this.id!));
        })
      )
    );
  }

  getStatusTag(invoice: InvoiceModel): string {
    return getInvoiceStatusTag(invoice.status);
  }

  getPaymentStatusTag(paymentTransaction: PaymentTransactionModel): string {
    return getPaymentStatusTag(paymentTransaction.paymentStatus);
  }

  dateFormat() {
    return environment.config.dateFormat;
  }

  dateTimeFormat() {
    return environment.config.dateTimeFormat;
  }

  formatBytes(bytes: number) {
    return prettyBytes(bytes, { locale: true });
  }

  getAddress(invoice: InvoiceModel) {
    return getInvoiceAddress(invoice);
  }
}
