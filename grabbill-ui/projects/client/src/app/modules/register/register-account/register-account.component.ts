import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { of, switchMap, tap } from 'rxjs';
// @ts-ignore
import { Country } from 'country-state-city';
import { NzMessageService } from 'ng-zorro-antd/message';
import { getErrorMessage, resolveErrorMessage, updateAndMarkControlAsDirty } from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { UpdateAccount } from '../../../../states/account/account.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { Me } from '../../../../states/auth/auth.state-actions';
import { malaysiaPhoneNoValidator } from '../../../../utils/phone-regex';

@Component({
  selector: 'grabbill-client-register-account',
  templateUrl: './register-account.component.html',
  styleUrls: ['./register-account.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterAccountComponent extends NgxsBaseComponent {
  isLoading = false;
  form: UntypedFormGroup;

  constructor(
    private fb: UntypedFormBuilder,
    protected override store: Store,
    protected override messageService: NzMessageService,
    private actions$: Actions,
    private cd: ChangeDetectorRef
  ) {
    super(store, messageService);
    this.form = this.fb.group({
      companyName: [null, [Validators.required, Validators.maxLength(255)]],
      companyContactNo: [null, [Validators.required, malaysiaPhoneNoValidator(), Validators.maxLength(255)]],
      country: ['Malaysia', [Validators.required, Validators.maxLength(255)]],
      affiliateCode: [null, [Validators.maxLength(255)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(UpdateAccount),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            return this.store.dispatch(new Me()).pipe(
              tap(() => {
                return this.navigate(['/', 'register', 'plan-selection']);
              })
            );
          }

          return of(false);
        })
      )
    );

    this.store.dispatch(new Me());
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  getCountries() {
    return Country.getAllCountries();
  }

  submitForm(): void {
    this.isLoading = true;
    this.cd.markForCheck();

    if (this.form.valid) {
      const formValues = this.form.value;
      const account = {
        ...formValues,
        country: formValues.country.name,
        countryIsoCode: formValues.country.isoCode,
      };
      this.store.dispatch(new UpdateAccount(account));
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }
}
