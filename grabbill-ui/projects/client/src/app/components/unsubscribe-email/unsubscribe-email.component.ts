import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { tap } from 'rxjs';
import { NzMessageService } from 'ng-zorro-antd/message';
import { getErrorMessage, resolveErrorMessage } from '@grabbill/lib';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { ShowMessage } from '../../../states/common/common.state-actions';
import { UnsubscribeEmail } from '../../../states/unsubscribe-email/unsubscribe-email.state-actions';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'grabbill-client-unsubscribe-email',
  templateUrl: './unsubscribe-email.component.html',
  styleUrls: ['./unsubscribe-email.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnsubscribeEmailComponent extends NgxsBaseComponent {
  isLoading = false;
  isUnsubscribeSuccessful = false;
  form: UntypedFormGroup;

  linkId?: string;

  constructor(
    private fb: UntypedFormBuilder,
    private actions$: Actions,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
    this.form = this.fb.group({
      reason: [null, [Validators.required]],
      otherReason: [null, [Validators.required]],
    });
  }

  get environment() {
    return environment.config;
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.linkId = this.route.snapshot.queryParams['linkId'];

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(UnsubscribeEmail),
        tap((data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isUnsubscribeSuccessful = true;
            this.cd.markForCheck();
          }
        })
      )
    );
  }

  submitForm(): void {
    if (this.form.valid) {
      this.isLoading = true;
      this.cd.markForCheck();
      const value = this.form.getRawValue();
      this.store.dispatch(new UnsubscribeEmail(this.linkId!, value.reason === 'O' ? value.otherReason : value.reason));
    } else {
      Object.values(this.form.controls).forEach((control) => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }
  }

  doChange(event: string) {
    if (event === 'O') {
      this.form.get('otherReason')?.enable();
    } else {
      this.form.get('otherReason')?.disable();
    }
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }
}
