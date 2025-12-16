import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { Observable, of, switchMap } from "rxjs";
import {
  getErrorMessage,
  PageableModel,
  Privilege,
  SearchResultPayloadModel,
  UserAuthorityModel,
  WhatsAppTypeBasicModel
} from "@grabbill/lib";
import { NzMessageService } from "ng-zorro-antd/message";
import { NzModalService } from "ng-zorro-antd/modal";
import { AuthState } from "../../../../states/auth/auth.state";
import { SetPageLoading, ShowMessage } from "../../../../states/common/common.state-actions";
import { format } from "date-fns";
import { environment } from "../../../../environments/environment";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { WhatsAppState } from "../../../../states/whatsapp/whatsapp.state";
import {
  DeleteWhatsAppType, DuplicateWhatsAppType,
  LoadMoreWhatsAppTypes,
  QueryWhatsAppTypes,
  ResetWhatsAppTypes
} from "../../../../states/whatsapp/whatsapp.state-actions";
import { hasPrivilege } from '../../../../utils/has-privilege';

@Component({
  selector: 'grabbill-client-whatsapp-type-list',
  templateUrl: './whatsapp-type-list.component.html',
  styleUrls: ['./whatsapp-type-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WhatsappTypeListComponent extends NgxsBaseComponent {

  @Select(WhatsAppState.whatsAppTypeSearchResult)
  whatsAppTypeSearchResult$!: Observable<SearchResultPayloadModel<WhatsAppTypeBasicModel>>;

  @Select(WhatsAppState.whatsAppTypePageable)
  whatsAppTypePageable$!: Observable<PageableModel>;

  user?: UserAuthorityModel;

  isListLoading = false;
  queryAction = QueryWhatsAppTypes;
  resetAction = ResetWhatsAppTypes;
  loadMoreAction = LoadMoreWhatsAppTypes;
  state = WhatsAppState;

  constructor(
    protected override messageService: NzMessageService,
    public override store: Store,
    public actions$: Actions,
    private cd: ChangeDetectorRef,
    private modal: NzModalService
  ) {
    super(store, messageService);
  }

  public get privilege(): typeof Privilege {
    return Privilege;
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(DeleteWhatsAppType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `WhatsApp deleted`));
          }
          this.isListLoading = false;
          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DuplicateWhatsAppType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `WhatsApp duplicated`));
            this.navigate([
              '/',
              'whatsapp',
              'detail',
              this.store.selectSnapshot(WhatsAppState.whatsAppType)!.id,
              'edit',
            ]);
          }
          return of(false);
        })
      )
    );
  }

  doSelect(type: WhatsAppTypeBasicModel) {
    this.navigate(['/', 'whatsapp', 'detail', type.id], { tab: 'activities' });
  }

  doDelete(whatsAppType: WhatsAppTypeBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${whatsAppType.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteWhatsAppType(whatsAppType.id));
      },
      nzCancelText: 'No',
    });
  }

  doDuplicate(whatsAppType: WhatsAppTypeBasicModel) {
    this.modal.confirm({
      nzTitle: `Duplicate ${whatsAppType.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new SetPageLoading(true));
        this.store.dispatch(new DuplicateWhatsAppType(whatsAppType.id));
      },
      nzCancelText: 'No',
    });
  }

  getMessage() {
    return (whatsAppType: WhatsAppTypeBasicModel): string => {
      if (whatsAppType.lastSentDate != null) {
        const lastUploadDate = new Date(whatsAppType.lastSentDate);
        return `Last sent at ${format(lastUploadDate, environment.config.dateFormat)} by ${
          whatsAppType.lastSentBy === this.user?.email ? 'me' : whatsAppType.lastSentBy
        }`;
      }
      if (whatsAppType.lastModifiedDate != null) {
        const lastModifiedDate = new Date(whatsAppType.lastModifiedDate);
        return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
          whatsAppType.lastModifiedBy === this.user?.email ? 'me' : whatsAppType.lastModifiedBy
        }`;
      }
      if (whatsAppType.createdDate != null) {
        const createdDate = new Date(whatsAppType.createdDate);
        return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
          whatsAppType.createdBy === this.user?.email ? 'me' : whatsAppType.createdBy
        }`;
      }

      return '';
    };
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }

}
