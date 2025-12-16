import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { Observable, of, switchMap, tap } from 'rxjs';
import { DiscountType, getErrorMessage, PrePlanSwitchInvoicePayloadModel, UserAuthorityModel } from "@grabbill/lib";
import { AccountState } from '../../../../states/account/account.state';
import { NzMessageService } from 'ng-zorro-antd/message';
import { SwitchPlan } from '../../../../states/account/account.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { environment } from '../../../../environments/environment';
import { AuthState } from '../../../../states/auth/auth.state';

@Component({
  selector: 'grabbill-client-register-switch-plan-summary',
  templateUrl: './register-switch-plan-summary.component.html',
  styleUrls: ['./register-switch-plan-summary.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterSwitchPlanSummaryComponent extends NgxsBaseComponent {
  isLoading = false;

  @Select(AccountState.prePlanSwitchInvoice)
  prePlanSwitchInvoice$!: Observable<PrePlanSwitchInvoicePayloadModel>;

  user?: UserAuthorityModel;

  discountType = DiscountType;

  constructor(
    private cd: ChangeDetectorRef,
    private actions$: Actions,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.user = this.store.selectSnapshot(AuthState.user)!;
    const subscription = this.user!.subscription;

    this.autoUnsubscribe(
      this.prePlanSwitchInvoice$.pipe(
        tap((invoice) => {
          if (!invoice) {
            this.navigate(['/', 'register', 'plan-selection']);
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(SwitchPlan),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            if (subscription) {
              return this.navigate(['/', 'account', 'plan'], { tab: 'overview' });
            } else {
              return this.navigate(['/']);
            }
          }

          return of(false);
        })
      )
    );
  }

  doSwitchPlan() {
    this.store.dispatch(new SwitchPlan());
  }

  public get dateFormat(): string {
    return environment.config.dateFormat;
  }
}
