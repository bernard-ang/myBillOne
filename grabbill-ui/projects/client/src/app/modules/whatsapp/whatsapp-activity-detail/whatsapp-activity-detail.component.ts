import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  getErrorMessage,
  ProcessStatus,
  WhatsAppActivityModel,
  WhatsAppRecordModel,
  WhatsAppTypeModel,
} from '@grabbill/lib';
import { ActivatedRoute, Params } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import {
  GetWhatsAppActivity,
  GetWhatsAppType,
  ResetWhatsAppActivity,
} from '../../../../states/whatsapp/whatsapp.state-actions';
import { getStatusTag } from 'projects/client/src/utils/get-status-tag';
import { getCode } from 'projects/client/src/utils/get-code';
import { WhatsAppState } from '../../../../states/whatsapp/whatsapp.state';

@Component({
  selector: 'grabbill-client-whatsapp-activity-detail',
  templateUrl: './whatsapp-activity-detail.component.html',
  styleUrls: ['./whatsapp-activity-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WhatsappActivityDetailComponent extends NgxsBaseComponent {
  @Select(WhatsAppState.whatsAppType)
  whatsAppType$!: Observable<WhatsAppTypeModel>;

  @Select(WhatsAppState.whatsAppActivity)
  whatsAppActivity$!: Observable<WhatsAppActivityModel>;

  typeId?: number;
  activityId?: number;
  email?: string;

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
          this.store.dispatch(new GetWhatsAppType(this.typeId!));
          this.store.dispatch(new GetWhatsAppActivity(this.typeId!, this.activityId!));
        })
      ),
      this.whatsAppActivity$.pipe(
        tap((activity) => {
          if (activity && activity.status === ProcessStatus.DRAFT) {
            this.navigate(['/', 'whatsapp', 'detail', this.typeId!, 'activity', this.activityId!, 'edit']);
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
      )
    );
  }

  getCode(type: WhatsAppTypeModel): string {
    return getCode(type.code);
  }

  getStatusTag(status: ProcessStatus): string {
    return getStatusTag(status);
  }

  getFilterRecords(records: WhatsAppRecordModel[]): WhatsAppRecordModel[] {
    return records.filter((record) => (this.email ? record.indexRow.text1.includes(this.email) : true));
  }

  doSearch(event: any) {
    this.email = event.target.value;
    this.cd.markForCheck();
  }
}
