import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../../../../client/src/app/components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ActivatedRoute, Params } from '@angular/router';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  GetAccount,
  GetAccountUsers,
  GetAccountWabaTemplates,
  RegisterWabaWebhook,
  ResetAccount,
  TestWabaLogin,
  UnregisterWabaWebhook,
  UpdateAccountStatus,
  UpdateWabaInfo,
} from '../../../../states/account-management/account-management.state-actions';
import { AccountManagementState } from '../../../../states/account-management/account-management.state';
import {
  AccountDetailsModel,
  getErrorMessage,
  getWabaStatusTag,
  resolveErrorMessage,
  updateAndMarkControlAsDirty,
  UserModel,
  WhatsappTemplateComponentModel,
  WhatsappTemplateModel,
  WhatsappTemplateStatus,
} from '@grabbill/lib';
import prettyBytes from 'pretty-bytes';
import { environment } from '../../../../environments/environment';
import { NzModalService } from 'ng-zorro-antd/modal';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ShowMessage } from '../../../../states/admin-common/admin-common.state-actions';

@Component({
  selector: 'grabbill-admin-account-detail',
  templateUrl: './admin-account-detail.component.html',
  styleUrls: ['./admin-account-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminAccountDetailComponent extends NgxsBaseComponent {
  @Select(AccountManagementState.account)
  account$!: Observable<AccountDetailsModel>;

  @Select(AccountManagementState.accountUsers)
  accountUsers$!: Observable<UserModel[]>;

  @Select(AccountManagementState.wabaTemplates)
  wabaTemplates$!: Observable<WhatsappTemplateModel[]>;

  id?: number;

  storagePercentage = 0;
  transactionalEmailPercentage = 0;
  emailCampaignPercentage = 0;

  isUpdateWabaModalVisible = false;
  isUpdateWabaFormLoading = false;
  updateWabaForm: UntypedFormGroup;

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private fb: UntypedFormBuilder,
    private modal: NzModalService,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions
  ) {
    super(store, messageService);

    this.updateWabaForm = this.fb.group({
      wabaEmail: ['', [Validators.required]],
      wabaPassword: [undefined],
      wabaId: [undefined],
      wabaGuid: [undefined, [Validators.required]],
      wabaName: [undefined],
      wabaPhone: [undefined],
      wabaPhoneId: [undefined],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetAccount());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.id = params['id'];
          this.store.dispatch(new GetAccount(this.id!));
          this.store.dispatch(new GetAccountUsers(this.id!));
          this.store.dispatch(new GetAccountWabaTemplates(this.id!));
        })
      ),
      this.account$.pipe(
        tap((account) => {
          if (account) {
            this.storagePercentage = Math.floor((account.storageUsed / account.storageLimit) * 100);
            this.transactionalEmailPercentage = Math.floor(
              (account.transactionalEmailSent / account.transactionalEmailLimit) * 100
            );
            this.emailCampaignPercentage = Math.floor((account.emailCampaignSent / account.emailCampaignLimit) * 100);
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateWabaInfo),
        switchMap((data: ActionCompletion) => {
          this.isUpdateWabaFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isUpdateWabaModalVisible = false;
            this.store.dispatch(new ShowMessage('info', `Waba info updated`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(RegisterWabaWebhook),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Webhook registered successfully`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(TestWabaLogin),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.modal.info({
              nzTitle: 'Test WABA Login',
              nzContent: getErrorMessage(data.result.error),
            });
          } else if (data.result.successful) {
            this.modal.info({
              nzTitle: 'Test WABA Login',
              nzContent: 'WABA login successfully',
            });
          }

          this.cd.markForCheck();
          return of(false);
        })
      )
    );
  }

  formatBytes(bytes: number) {
    return prettyBytes(bytes, { locale: true });
  }

  dateTimeFormat() {
    return environment.config.dateTimeFormat;
  }

  doUpdateAccountStatus(active: boolean) {
    this.modal.confirm({
      nzTitle: `${active ? 'Activate' : 'Deactivate'} Account`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new UpdateAccountStatus(this.id!, active));
      },
      nzCancelText: 'No',
    });
  }

  doOpenUpdateWabaInfoModal(account: AccountDetailsModel) {
    this.updateWabaForm.setValue({
      wabaEmail: account.wabaEmail || null,
      wabaPassword: null,
      wabaId: account.wabaId || null,
      wabaGuid: account.wabaGuid || null,
      wabaName: account.wabaName || null,
      wabaPhone: account.wabaPhone || null,
      wabaPhoneId: account.wabaPhoneId || null,
    });
    this.isUpdateWabaModalVisible = true;
    this.cd.markForCheck();
  }

  doCloseUpdateWabaInfoModal() {
    this.isUpdateWabaModalVisible = false;
    this.cd.markForCheck();
  }

  doUpdateWabaInfo() {
    this.isUpdateWabaFormLoading = true;
    this.cd.markForCheck();

    if (this.updateWabaForm.valid) {
      const value = this.updateWabaForm.getRawValue();
      this.store.dispatch(
        new UpdateWabaInfo(this.id!, {
          wabaEmail: value.wabaEmail,
          wabaPassword: value.wabaPassword && value.wabaPassword.trim() !== '' ? value.wabaPassword.trim() : null,
          wabaId: value.wabaId,
          wabaGuid: value.wabaGuid,
          wabaName: value.wabaName,
          wabaPhoneId: value.wabaPhoneId,
          wabaPhone: value.wabaPhone,
        })
      );
    } else {
      updateAndMarkControlAsDirty(this.updateWabaForm);
      this.isUpdateWabaFormLoading = false;
      this.cd.markForCheck();
    }
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doTestWabaLogin() {
    this.modal.confirm({
      nzTitle: `Test WABA Login`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new TestWabaLogin(this.id!));
      },
      nzCancelText: 'No',
    });
  }

  doRegisterWebhook() {
    this.modal.confirm({
      nzTitle: `Register Webhook`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new RegisterWabaWebhook(this.id!));
      },
      nzCancelText: 'No',
    });
  }

  doUnregisterWebhook() {
    this.modal.confirm({
      nzTitle: `Unregister Webhook`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new UnregisterWabaWebhook(this.id!));
      },
      nzCancelText: 'No',
    });
  }

  getWabaComponents(components: WhatsappTemplateComponentModel[]) {
    return components
      .map((item) => `[${item.type}]${item.format ? ' ' + item.format : ''}${item.text ? ' ' + item.text : ''}`)
      .join(',');
  }

  getWabaStatusTag(status: WhatsappTemplateStatus): string {
    return getWabaStatusTag(status);
  }
}
