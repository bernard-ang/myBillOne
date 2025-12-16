import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import {
  AccountBasicModel,
  getErrorMessage,
  getJobStatusTag,
  JobBasicModel,
  PageableModel,
  PlanModel,
  SearchResultPayloadModel,
  SubscriptionMode,
} from '@grabbill/lib';
import { AccountManagementState } from '../../../../states/account-management/account-management.state';
import {
  DownloadAccountReport,
  QueryAccounts,
  ResetAccounts,
  SwitchAccountPlan,
  UpdateAccountPaymentExemptionStatus,
  UpdateAccountStatus,
} from '../../../../states/account-management/account-management.state-actions';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { filter } from 'rxjs/operators';
import { NzModalService } from 'ng-zorro-antd/modal';
import { environment } from '../../../../environments/environment';
import { ShowMessage } from '../../../../states/admin-common/admin-common.state-actions';
import { GetPlans } from '../../../../states/admin-plan/admin-plan.state-actions';
import { AdminPlanState } from '../../../../states/admin-plan/admin-plan.state';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import prettyBytes from 'pretty-bytes';
import { saveAs } from 'file-saver';

@Component({
  selector: 'grabbill-admin-account-list',
  templateUrl: './admin-account-list.component.html',
  styleUrls: ['./admin-account-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminAccountListComponent extends NgxsBaseComponent {
  @Select(AccountManagementState.accountSearchResult)
  accountSearchResult$!: Observable<SearchResultPayloadModel<AccountBasicModel>>;

  @Select(AccountManagementState.accountPageable)
  accountPageable$!: Observable<PageableModel>;

  @Select(AdminPlanState.plans)
  plans$!: Observable<PlanModel[]>;

  isInitialize = false;
  isTableLoading = false;
  searchChange$ = new BehaviorSubject('');
  isSwitchPlanModalVisible = false;
  switchPlanForm: UntypedFormGroup;
  accountId?: number;
  accountName?: string;
  planOptions: { key: string; label: string; plan: PlanModel }[] = [];
  storageOptions: { key: number; label: string }[] = [];
  txOptions: { key: number; label: number }[] = [];
  ecOptions: { key: number; label: number }[] = [];
  subscriptionOptions = [SubscriptionMode.MONTHLY, SubscriptionMode.ANNUALLY];

  isReportModalVisible = false;
  reportForm: UntypedFormGroup;

  dateRangeChange$: BehaviorSubject<(Date | null)[]> = new BehaviorSubject<(Date | null)[]>([]);
  form: UntypedFormGroup;

  isFilterModalVisible = false;
  filterForm: UntypedFormGroup;
  filters: { [index: string]: any } = {};
  filterOptions = [
    {
      label: 'Plan Name',
      key: 'planName',
      type: 'TEXT',
      options: [],
    },
  ];
  options: string[] = [];

  constructor(
    private cd: ChangeDetectorRef,
    private modal: NzModalService,
    private actions$: Actions,
    private fb: UntypedFormBuilder,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);

    this.switchPlanForm = this.fb.group({
      plan: ['', [Validators.required]],
      storage: [undefined, [Validators.required]],
      transactionalEmail: [undefined, [Validators.required]],
      emailCampaign: [undefined, [Validators.required]],
      subscriptionMode: [undefined, [Validators.required]],
    });

    this.reportForm = this.fb.group({
      dates: ['', [Validators.required]],
      affiliateCode: ['', [Validators.maxLength(255)]],
    });

    this.form = this.fb.group({
      range: [],
    });

    this.filterForm = this.fb.group({
      field: ['', [Validators.required]],
      type: [''],
      value: [undefined, [Validators.required]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetAccounts());
    this.store.dispatch(new GetPlans());

    this.autoUnsubscribe(
      this.accountSearchResult$.pipe(
        tap(() => {
          this.isTableLoading = false;
        })
      ),
      this.plans$.pipe(
        tap((plans) => {
          if (plans && plans.length > 0) {
            this.planOptions = plans
              .filter((plan) => plan.name !== 'Enterprise')
              .map((plan) => ({ key: plan.id.toString(10), label: plan.name, plan: plan }));
            this.storageOptions = plans[0].storageOptions.map((option) => ({
              key: option.size,
              label: this.formatBytes(option.size),
            }));
            this.txOptions = plans[0].transactionalEmailOptions.map((option) => ({
              key: option.size,
              label: option.size,
            }));
            this.ecOptions = plans[0].emailCampaignOptions.map((option) => ({ key: option.size, label: option.size }));

            this.switchPlanForm.setValue({
              plan: this.planOptions[0].key,
              storage: this.storageOptions[0].key,
              transactionalEmail: this.txOptions[0].key,
              emailCampaign: this.ecOptions[0].key,
              subscriptionMode: this.subscriptionOptions[0],
            });
            this.cd.markForCheck();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateAccountStatus),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Account status updated`));
            this.isTableLoading = true;
            this.store.dispatch(
              new QueryAccounts(
                this.store.selectSnapshot(AccountManagementState.accountPageable),
                this.store.selectSnapshot(AccountManagementState.filters),
                this.store.selectSnapshot(AccountManagementState.nameSelector),
                this.store.selectSnapshot(AccountManagementState.startDate),
                this.store.selectSnapshot(AccountManagementState.endDate)
              )
            );
            this.cd.markForCheck();
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateAccountPaymentExemptionStatus),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Account payment exemption status updated`));
            this.isTableLoading = true;
            this.store.dispatch(
              new QueryAccounts(
                this.store.selectSnapshot(AccountManagementState.accountPageable),
                this.store.selectSnapshot(AccountManagementState.filters),
                this.store.selectSnapshot(AccountManagementState.nameSelector),
                this.store.selectSnapshot(AccountManagementState.startDate),
                this.store.selectSnapshot(AccountManagementState.endDate)
              )
            );
            this.cd.markForCheck();
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(SwitchAccountPlan),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Switch plan successfully`));
            this.isTableLoading = true;
            this.store.dispatch(
              new QueryAccounts(
                this.store.selectSnapshot(AccountManagementState.accountPageable),
                this.store.selectSnapshot(AccountManagementState.filters),
                this.store.selectSnapshot(AccountManagementState.nameSelector),
                this.store.selectSnapshot(AccountManagementState.startDate),
                this.store.selectSnapshot(AccountManagementState.endDate)
              )
            );
            this.cd.markForCheck();
          }

          return of(false);
        })
      ),
      this.searchChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((name: string) => {
            this.isTableLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new QueryAccounts(
                {
                  ...this.store.selectSnapshot(AccountManagementState.accountPageable),
                  page: 1,
                },
                this.store.selectSnapshot(AccountManagementState.filters),
                name,
                this.store.selectSnapshot(AccountManagementState.startDate),
                this.store.selectSnapshot(AccountManagementState.endDate)
              )
            );
          })
        ),
      this.dateRangeChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((range: (Date | null)[]) => {
            this.isTableLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new QueryAccounts(
                {
                  ...this.store.selectSnapshot(AccountManagementState.accountPageable),
                  page: 1,
                },
                this.store.selectSnapshot(AccountManagementState.filters),
                this.store.selectSnapshot(AccountManagementState.nameSelector),
                range[0] ? range[0] : undefined,
                range[1] ? range[1] : undefined
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(DownloadAccountReport),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(AccountManagementState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      )
    );
  }

  doSearch(event: any) {
    this.searchChange$.next(event.target.value);
  }

  doDateRange(event: (Date | null)[]) {
    this.dateRangeChange$.next(event);
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;

    const targetSort = event.sort.find((sort) => sort.value !== null);
    const pageable = produce(this.store.selectSnapshot(AccountManagementState.accountPageable), (draft) => {
      draft.page = event.pageIndex;
      draft.sort = targetSort ? targetSort.key : 'lastModifiedDate';
      draft.direction = targetSort ? (targetSort.value === 'ascend' ? 'ASC' : 'DESC') : 'DESC';
    });
    this.store.dispatch(new QueryAccounts(
      pageable,
      this.store.selectSnapshot(AccountManagementState.filters),
      this.store.selectSnapshot(AccountManagementState.nameSelector),
      this.store.selectSnapshot(AccountManagementState.startDate),
      this.store.selectSnapshot(AccountManagementState.endDate)
    ));
    this.isInitialize = true;
  }

  doUpdateAccountStatus(id: number, accountName: string, active: boolean) {
    this.modal.confirm({
      nzTitle: `${active ? 'Activate' : 'Deactivate'} Account ${accountName}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new UpdateAccountStatus(id, active));
      },
      nzCancelText: 'No',
    });
  }

  doUpdateAccountPaymentExemptionStatus(id: number, accountName: string, paymentExemption: boolean) {
    this.modal.confirm({
      nzTitle: `${paymentExemption ? 'Exempt' : 'Activate'} Account Payment ${accountName}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new UpdateAccountPaymentExemptionStatus(id, paymentExemption));
      },
      nzCancelText: 'No',
    });
  }

  doOpenSwitchPlanModal(id: number, accountName: string) {
    this.accountId = id;
    this.accountName = accountName;
    this.isSwitchPlanModalVisible = true;
    this.cd.markForCheck();
  }

  doSwitchPlan() {
    this.isSwitchPlanModalVisible = false;
    this.cd.markForCheck();
    this.modal.confirm({
      nzTitle: `Switch Account Plan ${this.accountName}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(
          new SwitchAccountPlan(this.accountId!, {
            planId: this.switchPlanForm.value.plan,
            storageSize: this.switchPlanForm.value.storage,
            transactionalEmailSize: this.switchPlanForm.value.transactionalEmail,
            emailCampaignSize: this.switchPlanForm.value.emailCampaign,
            subscriptionMode: this.switchPlanForm.value.subscriptionMode,
          })
        );
      },
      nzCancelText: 'No',
    });
  }

  getDateTimeFormat() {
    return environment.config.dateTimeFormat;
  }

  doCloseSwitchPlanModal() {
    this.isSwitchPlanModalVisible = false;
    this.cd.markForCheck();
  }

  doPlanChange(value: string) {
    const option = this.planOptions.filter((option) => option.key === value)[0];
    this.storageOptions = option.plan.storageOptions.map((option) => ({
      key: option.size,
      label: this.formatBytes(option.size),
    }));
    this.txOptions = option.plan.transactionalEmailOptions.map((option) => ({ key: option.size, label: option.size }));
    this.ecOptions = option.plan.emailCampaignOptions.map((option) => ({ key: option.size, label: option.size }));

    this.switchPlanForm.setValue({
      plan: value,
      storage: this.storageOptions[0].key,
      transactionalEmail: this.txOptions[0].key,
      emailCampaign: this.ecOptions[0].key,
      subscriptionMode: this.switchPlanForm.value.subscriptionMode,
    });

    this.cd.markForCheck();
  }

  formatBytes(bytes: number) {
    return prettyBytes(bytes, { locale: true });
  }

  doOpenDownloadReportModal() {
    this.reportForm.reset();
    this.isReportModalVisible = true;
    this.cd.markForCheck();
  }

  doCloseReportModal() {
    this.isReportModalVisible = false;
    this.cd.markForCheck();
  }

  doDownloadReport() {
    const { dates, affiliateCode } = this.reportForm.getRawValue();
    this.isReportModalVisible = false;
    this.cd.markForCheck();
    this.store.dispatch(new DownloadAccountReport(dates[0], dates[1], affiliateCode));
  }

  doOpenFilterModal() {
    this.filterForm.reset();
    this.filterForm.get('field')!.setValue('planName');
    this.doFilterFieldChange('planName');
    this.isFilterModalVisible = true;
    this.cd.markForCheck();
  }

  doCloseFilterModal() {
    this.isFilterModalVisible = false;
    this.cd.markForCheck();
  }

  doFilterFieldChange(value: string) {
    const option = this.filterOptions.filter((option) => option.key === value)[0];
    this.options = option.options;
    this.filterForm.get('type')!.setValue(option.type);
    this.cd.markForCheck();
  }

  doAddFilter() {
    let updateFilters = { ...this.filters };
    const field = this.filterForm.get('field')!.value;
    updateFilters[field] = this.filterForm.get('value')!.value;

    this.filters = updateFilters;

    this.isTableLoading = true;
    this.isFilterModalVisible = false;
    this.cd.markForCheck();

    this.store.dispatch(
      new QueryAccounts(
        this.store.selectSnapshot(AccountManagementState.accountPageable),
        this.filters,
        this.store.selectSnapshot(AccountManagementState.nameSelector),
        this.store.selectSnapshot(AccountManagementState.startDate),
        this.store.selectSnapshot(AccountManagementState.endDate)
      )
    );
  }

  getFilters() {
    return Object.entries(this.filters);
  }

  doRemoveFilter(filter: string) {
    this.isTableLoading = true;
    this.cd.markForCheck();

    let updateFilters: { [index: string]: any } = {};
    for (const entry of Object.entries(this.filters)) {
      if (entry[0] !== filter) {
        updateFilters[entry[0]] = entry[1];
      }
    }
    this.filters = updateFilters;

    this.store.dispatch(
      new QueryAccounts(
        this.store.selectSnapshot(AccountManagementState.accountPageable),
        this.filters,
        this.store.selectSnapshot(AccountManagementState.nameSelector),
        this.store.selectSnapshot(AccountManagementState.startDate),
        this.store.selectSnapshot(AccountManagementState.endDate)
      )
    );
  }

  getFilterLabel(filter: string) {
    return filter;
  }

  getStatusTag(job: JobBasicModel): string {
    return getJobStatusTag(job.status);
  }
}
