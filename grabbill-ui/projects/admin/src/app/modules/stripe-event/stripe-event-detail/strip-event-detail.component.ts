import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { Select, Store } from '@ngxs/store';
import { Observable, tap } from 'rxjs';
import { StripeEventModel } from '@grabbill/lib';
import { ActivatedRoute, Params } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import { environment } from '../../../../environments/environment';
import { StripeManagementState } from '../../../../states/stripe-management/stripe-management.state';
import { GetStripeEvent, ResetStripeEvent } from '../../../../states/stripe-management/stripe-management.state-actions';

@Component({
  selector: 'grabbill-admin-strip-event-detail',
  templateUrl: './strip-event-detail.component.html',
  styleUrls: ['./strip-event-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StripEventDetailComponent extends NgxsBaseComponent {
  @Select(StripeManagementState.event)
  stripeEvent$!: Observable<StripeEventModel>;

  id?: number;

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
    this.store.dispatch(new ResetStripeEvent());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.id = params['id'];
          this.store.dispatch(new GetStripeEvent(this.id!));
        })
      )
    );
  }

  dateTimeFormat() {
    return environment.config.dateTimeFormat;
  }
}
