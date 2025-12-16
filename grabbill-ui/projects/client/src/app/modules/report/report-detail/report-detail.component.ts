import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import {
  BaseActivityBasicModel,
  BaseTypeBasicModel,
  BaseTypeModel,
  DomainType,
  getErrorMessage,
  makePageable, Privilege,
  resolveErrorMessage,
  SearchResultPayloadModel,
  updateAndMarkControlAsDirty, UserAuthorityModel
} from "@grabbill/lib";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { NzMessageService } from "ng-zorro-antd/message";
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from "rxjs";
import { ReportState } from "../../../../states/report/report.state";
import {
  GenerateReport,
  QueryActivities,
  QueryTypes,
  ResetReport
} from "projects/client/src/states/report/report.state-actions";
import { ShowMessage } from "../../../../states/common/common.state-actions";
import { saveAs } from "file-saver";
import { NzModalService } from "ng-zorro-antd/modal";
import { AuthState } from "../../../../states/auth/auth.state";
import { hasPrivilege } from "../../../../utils/has-privilege";
import { isSaasMode } from "../../../../utils/deployment-mode";

@Component({
  selector: 'grabbill-client-report-detail',
  templateUrl: './report-detail.component.html',
  styleUrls: ['./report-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReportDetailComponent extends NgxsBaseComponent {
  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  @Select(ReportState.typeSearchResult)
  typeSearchResult$!: Observable<SearchResultPayloadModel<BaseTypeBasicModel>>;

  @Select(ReportState.activitySearchResult)
  activitySearchResult$!: Observable<SearchResultPayloadModel<BaseActivityBasicModel>>;

  @Select(ReportState.type)
  type$!: Observable<BaseTypeModel>;
  typeId?: number = undefined;
  reportForm: UntypedFormGroup;
  isFormLoading = false;
  isTypeLoading = false;
  isActivityLoading = false;
  currentDomain?: DomainType;
  currentType?: BaseTypeModel;
  previousTypeValue = '';
  previousActivityValue = '';
  typeSearchChange$ = new BehaviorSubject('');
  activitySearchChange$ = new BehaviorSubject('');
  typeOptions: BaseTypeBasicModel[] = [];
  activitiesOptions: BaseActivityBasicModel[] = [];
  isSaas = isSaasMode()

  urlReportTypes = [
    { label: 'URL Click Summary', value: 'URL_CLICK_SUMMARY', checked: true },
    { label: 'URL Click Records', value: 'URL_CLICK_RECORDS', checked: true },
  ];

  activityReportTypes = [
    { label: 'Activity Summary', value: 'ACTIVITY_SUMMARY', checked: true },
    { label: 'Activity Records', value: 'ACTIVITY_RECORDS', checked: true },
  ];

  constructor(
    private fb: UntypedFormBuilder,
    protected override store: Store,
    protected override messageService: NzMessageService,
    private actions$: Actions,
    private cd: ChangeDetectorRef,
    private modal: NzModalService
  ) {
    super(store, messageService);

    this.reportForm = this.fb.group({
      domain: [{ value: '' }, [Validators.required]],
      type: [{ value: '', disabled: true }, [Validators.required]],
      activities: [{ value: [], disabled: true }, [Validators.required]],
      reportTypes: [[...this.activityReportTypes, ...this.urlReportTypes], [Validators.required]],
      encryptPassword: [{ value: [{ label: 'Attachment Password', value: 'ENCRYPT_ATTACHMENT_PASSWORD', checked: true }], disabled: true }]
    });
  }

  override ngOnInit(): void {
    this.store.dispatch(new ResetReport());
    super.ngOnInit();

    this.autoUnsubscribe(
      this.typeSearchResult$.pipe(
        tap((searchResult) => {
          this.typeOptions = searchResult.items;
          this.isTypeLoading = false;
          this.cd.markForCheck();
        })
      ),
      this.activitySearchResult$.pipe(
        tap((searchResult) => {
          this.activitiesOptions = searchResult.items;
          this.isActivityLoading = false;
          this.cd.markForCheck();
        })
      ),
      this.type$.pipe(
        tap((type) => {
          this.currentType = type;
          this.cd.markForCheck();
        })
      ),
      this.typeSearchChange$.asObservable().pipe(
        tap((name) => {
          if (this.currentDomain) {
            this.isTypeLoading = true;
            this.store.dispatch(new QueryTypes(this.currentDomain, makePageable(5, 1, 'lastModifiedDate', 'desc'), name));
          }
        })
      ),
      this.activitySearchChange$.asObservable().pipe(
        tap((name) => {
          if (this.currentDomain) {
            this.isTypeLoading = true;
            this.store.dispatch(
              new QueryActivities(this.currentDomain, makePageable(5, 1, 'lastModifiedDate', 'desc'), this.typeId!, name)
            );
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GenerateReport),
        switchMap((data: ActionCompletion) => {
          this.isFormLoading = false;
          this.cd.markForCheck();
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(ReportState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(QueryTypes, QueryActivities),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.isTypeLoading = false;
            this.isActivityLoading = false;
            this.cd.markForCheck();
          }
          return of(false);
        })
      )
    );
  }

  getDomainTypeOptions(user: UserAuthorityModel): DomainType[] {
    const options = [];

    if (hasPrivilege(user, Privilege.DGTL_FILING_ACTIVITY_VIEW)) {
      options.push(DomainType.DIGITAL_FILING);
    }

    if (hasPrivilege(user, Privilege.EMAIL_CAMPAIGN_ACTIVITY_VIEW)) {
      options.push(DomainType.EMAIL_CAMPAIGN);
    }

    if (hasPrivilege(user, Privilege.TRX_EMAIL_ACTIVITY_VIEW)) {
      if(this.isSaas) {
        options.push(DomainType.TRANSACTIONAL_EMAIL);
      } else {
        options.push(DomainType.MT_TRANSACTIONAL_EMAIL);
      }
    }

    if (hasPrivilege(user, Privilege.WA_ACTIVITY_VIEW)) {
      if(this.isSaas) {
        options.push(DomainType.WHATSAPP);
      } else {
        options.push(DomainType.MT_WHATSAPP);
      }
    }

    if (hasPrivilege(user, Privilege.SMS_ACTIVITY_VIEW)) {
      options.push(DomainType.SMS);
    }
    return options;
  }

  getDomainLabel(domain: DomainType) {
    return (domain as any).replaceAll('MT_', '').replaceAll('_', ' ');
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doDomainChange(value: DomainType) {
    this.currentDomain = value;

    if (this.currentDomain === DomainType.DIGITAL_FILING || this.currentDomain === DomainType.SMS || this.currentDomain === DomainType.WHATSAPP) {
      this.reportForm.get('reportTypes')?.setValue(this.activityReportTypes);
    } else {
      this.reportForm.get('reportTypes')?.setValue([...this.activityReportTypes, ...this.urlReportTypes]);
    }

    const encryptPasswordField = this.reportForm.get('encryptPassword');
    this.currentDomain === DomainType.TRANSACTIONAL_EMAIL || DomainType.WHATSAPP ? encryptPasswordField?.enable(): encryptPasswordField?.disable()

    this.currentType = undefined;
    this.isTypeLoading = true;
    this.reportForm.get('type')?.enable();
    this.reportForm.get('type')?.setValue('');
    this.reportForm.get('activities')?.disable();
    this.reportForm.get('activities')?.setValue([]);
    this.cd.markForCheck();
    this.store.dispatch(new QueryTypes(this.currentDomain, makePageable(5, 1, 'lastModifiedDate', 'desc')));
  }

  doSearchType(value: string) {
    if (this.previousTypeValue !== value) {
      this.previousTypeValue = value;
      this.typeSearchChange$.next(value);
    }
  }

  doTypeChange(value: number) {
    this.typeId = value;
    this.isActivityLoading = true;
    this.reportForm.get('activities')?.enable();
    this.reportForm.get('activities')?.setValue([]);
    this.cd.markForCheck();
    if (this.typeId) {
      this.store.dispatch(new QueryActivities(this.currentDomain!, makePageable(5, 1, 'lastModifiedDate', 'desc'), value));
    }
  }

  doSearchActivity(value: string) {
    if (this.previousActivityValue !== value) {
      this.previousActivityValue = value;
      this.activitySearchChange$.next(value);
    }
  }

  generateReport() {
    this.isFormLoading = true;
    this.cd.markForCheck();
    if (this.reportForm.valid) {
      const { domain, type, reportTypes, activities, encryptPassword } = this.reportForm.getRawValue();

      const filterReportType = reportTypes.filter((current: { value: string; checked: boolean }) => current.checked);

      if (filterReportType.length === 0) {
        this.modal.error({
          nzTitle: 'No worksheet selected',
          nzContent: 'Please select a worksheet',
        });
        this.isFormLoading = false;
        this.cd.markForCheck();
      } else {
        this.store.dispatch(
          new GenerateReport(
            domain,
            type,
            activities,
            reportTypes
              .filter((current: { value: string; checked: boolean }) => current.checked)
              .map((current: { value: string; checked: boolean }) => current.value),
            encryptPassword
              .filter((current: { value: string; checked: boolean }) => current.checked)
              .map((current: { value: string; checked: boolean }) => current.value)
          )
        );
      }
    } else {
      updateAndMarkControlAsDirty(this.reportForm);
      this.isFormLoading = false;
      this.cd.markForCheck();
    }
  }

  doResetForm() {
    this.reportForm.reset({
      domain: '',
      type: '',
      activities: '',
      reportTypes: [...this.activityReportTypes, ...this.urlReportTypes],
    });
    this.reportForm.get('type')?.disable();
    this.reportForm.get('activities')?.disable();
  }

  isTransactionalEmail() {
    return this.reportForm.get('domain')?.value  === DomainType.TRANSACTIONAL_EMAIL;
  }
}
