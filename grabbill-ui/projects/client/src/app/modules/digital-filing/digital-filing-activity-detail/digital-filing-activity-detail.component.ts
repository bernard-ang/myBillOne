import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { ActivatedRoute, Params } from "@angular/router";
import { NzModalService } from "ng-zorro-antd/modal";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { NzMessageService } from "ng-zorro-antd/message";
import { Observable, of, switchMap, tap } from "rxjs";
import { saveAs } from "file-saver";
import {
  BaseIndexRowModel,
  BaseRecordModel,
  DigitalFilingActivityModel,
  DigitalFilingTypeModel,
  getErrorMessage,
  ProcessStatus
} from "@grabbill/lib";
import { environment } from "../../../../environments/environment";
import {
  DownloadDigitalFilingFile,
  GetDigitalFilingActivity,
  GetDigitalFilingType,
  ResetDigitalFilingActivity
} from "../../../../states/digital-filing/digital-filing.state-actions";
import { DigitalFilingState } from "../../../../states/digital-filing/digital-filing.state";
import { SetPageLoading, ShowMessage } from "../../../../states/common/common.state-actions";
import { getCode } from "../../../../utils/get-code";
import { getStatusTag } from "../../../../utils/get-status-tag";
import { getIndexFieldLabels } from "../../../../utils/get-index-field-labels";
import { getIndexRowValues } from "../../../../utils/get-index-row-values";

@Component({
  selector: 'grabbill-client-digital-filing-activity-detail',
  templateUrl: './digital-filing-activity-detail.component.html',
  styleUrls: ['./digital-filing-activity-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DigitalFilingActivityDetailComponent extends NgxsBaseComponent {
  @Select(DigitalFilingState.digitalFilingType)
  digitalFilingType$!: Observable<DigitalFilingTypeModel>;

  @Select(DigitalFilingState.digitalFilingActivity)
  digitalFilingActivity$!: Observable<DigitalFilingActivityModel>;

  typeId?: number;
  activityId?: number;
  searchFilename?: string;

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
    this.store.dispatch(new ResetDigitalFilingActivity());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.store.dispatch(new GetDigitalFilingType(this.typeId!));
          this.store.dispatch(new GetDigitalFilingActivity(this.typeId!, this.activityId!));
        })
      ),
      this.digitalFilingActivity$.pipe(
        tap((activity) => {
          if (activity && activity.status === ProcessStatus.DRAFT) {
            this.navigate(['digital-filing', 'detail', this.typeId!, 'activity', this.activityId!, 'edit']);
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetDigitalFilingType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate([ '/', 'digital-filing', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetDigitalFilingActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate([ '/', 'digital-filing', 'detail', this.typeId! ], { tab: 'activities' });
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DownloadDigitalFilingFile),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(DigitalFilingState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      )
    );
  }

  getDateFormat() {
    return environment.config.dateFormat;
  }

  getCode(digitalFilingType: DigitalFilingTypeModel): string {
    return getCode(digitalFilingType.code);
  }

  getStatusTag(activity: DigitalFilingActivityModel): string {
    return getStatusTag(activity.status);
  }

  getIndexFieldLabels(digitalFileType: DigitalFilingTypeModel) {
    return getIndexFieldLabels(digitalFileType);
  }

  getIndexRowValues(row: BaseIndexRowModel, digitalFileType: DigitalFilingTypeModel) {
    return getIndexRowValues(row, digitalFileType);
  }

  doSearchAttachment(event: any) {
    this.searchFilename = event.target.value;
  }

  getFilterRecords(records: BaseRecordModel[]): BaseRecordModel[] {
    return records.filter((record) =>
      this.searchFilename ? record.indexRow.text1.includes(this.searchFilename) : true
    );
  }

  hasFile(row: BaseIndexRowModel, activity: DigitalFilingActivityModel) {
    return activity.files.filter((file) => file.name === row.text1).length > 0;
  }

  doDownloadFile(row: BaseIndexRowModel) {
    this.store.dispatch(new SetPageLoading(true));
    this.store.dispatch(new DownloadDigitalFilingFile(this.typeId!, this.activityId!, row.file?.id!, row.file?.name!));
  }

  doSearch(event: any) {
    this.searchFilename = event.target.value;
    this.cd.markForCheck();
  }
}
