import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { AuthState } from '../../../../states/auth/auth.state';
import { Observable, tap } from 'rxjs';
import { NzMessageService } from 'ng-zorro-antd/message';
import { getErrorMessage, resolveErrorMessage, updateAndMarkControlAsDirty, UserAuthorityModel } from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { UpdateAccount } from '../../../../states/account/account.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
// @ts-ignore
import { Country } from 'country-state-city';
import { malaysiaPhoneNoValidator } from '../../../../utils/phone-regex';

@Component({
  selector: 'grabbill-client-account-detail',
  templateUrl: './account-detail.component.html',
  styleUrls: ['./account-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountDetailComponent extends NgxsBaseComponent {
  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

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
      companyName: ['', [Validators.required, Validators.maxLength(255)]],
      companyContactNo: ['', [Validators.required, malaysiaPhoneNoValidator()]],
      addrLine1: ['', [Validators.maxLength(255)]],
      addrLine2: ['', [Validators.maxLength(255)]],
      city: ['', [Validators.maxLength(255)]],
      state: ['', [Validators.maxLength(255)]],
      postcode: ['', [Validators.maxLength(255)]],
      country: ['', [Validators.maxLength(255)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.user$.pipe(
        tap((user) => {
          this.form.setValue({
            companyName: user.account.companyName,
            companyContactNo: user.account.companyContactNo,
            country: Country.getCountryByCode(user.account.countryIsoCode),
            state: user.account.state || '',
            postcode: user.account.postcode || '',
            city: user.account.city || '',
            addrLine1: user.account.addrLine1 || '',
            addrLine2: user.account.addrLine2 || '',
          });
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateAccount),
        tap((data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', 'Update account successfully'));
            this.cd.markForCheck();
          }
        })
      )
    );
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

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }
}
