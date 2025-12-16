import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { Observable, of, switchMap, tap } from "rxjs";
import {
  BaseIndexRowModel,
  getErrorMessage,
  ProcessStatus,
  WhatsAppActivityModel,
  WhatsAppRecordModel,
  WhatsAppTypeModel
} from "@grabbill/lib";
import { ActivatedRoute, Params } from "@angular/router";
import { NzMessageService } from "ng-zorro-antd/message";
import { SetPageLoading, ShowMessage } from "../../../../states/common/common.state-actions";
import { saveAs } from "file-saver";
import { environment } from "../../../../environments/environment";
import { WhatsAppState } from "../../../../states/whatsapp/whatsapp.state";
import {
  DownloadWhatsAppFile,
  GetWhatsAppActivity,
  GetWhatsAppType,
  ResetWhatsAppActivity
} from "../../../../states/whatsapp/whatsapp.state-actions";
import { getStatusTag } from "projects/client/src/utils/get-status-tag";
import { getIndexRowObjects } from "../../../../utils/get-index-row-objects";

@Component({
  selector: 'grabbill-client-whatsapp-record-detail',
  templateUrl: './whatsapp-record-detail.component.html',
  styleUrls: ['./whatsapp-record-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WhatsappRecordDetailComponent extends NgxsBaseComponent {
  @Select(WhatsAppState.whatsAppType)
  whatsAppType$!: Observable<WhatsAppTypeModel>;

  @Select(WhatsAppState.whatsAppActivity)
  whatsAppActivity$!: Observable<WhatsAppActivityModel>;

  typeId?: number;
  activityId?: number;
  recordId?: number;
  record?: WhatsAppRecordModel;

  constructor(
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetWhatsAppActivity());
    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.recordId = parseInt(params['recordId']);
          this.store.dispatch(new GetWhatsAppType(this.typeId!));
          this.store.dispatch(new GetWhatsAppActivity(this.typeId!, this.activityId!));
        })
      ),
      this.whatsAppActivity$.pipe(
        tap((activity) => {
          if (activity) {
            this.record = activity.records.filter((value) => value.id === this.recordId)[0];
            this.cd.markForCheck();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetWhatsAppType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'whatsapp', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetWhatsAppActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'whatsapp', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DownloadWhatsAppFile),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(WhatsAppState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      )
    );
  }

  getStatusTag(status: ProcessStatus): string {
    return getStatusTag(status);
  }

  getIndexRowObjects(row: BaseIndexRowModel, type: WhatsAppTypeModel) {
    return getIndexRowObjects(row, type).map((obj) => ({
      span: 2,
      title: obj.label,
      value: obj.value,
      type: 'string' as 'string',
    }));
  }

  hasFile(row: BaseIndexRowModel, activity: WhatsAppActivityModel) {
    return activity.files.filter((file) => file.name === row.text2).length > 0;
  }

  doDownloadFile(row: BaseIndexRowModel) {
    this.store.dispatch(
      new DownloadWhatsAppFile(this.typeId!, this.activityId!, row.file!.id, row.file!.name!)
    );
  }

  getDateFormat() {
    return environment.config.dateFormat;
  }
}
