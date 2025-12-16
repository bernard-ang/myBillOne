import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { WhatsAppState } from '../../../../states/whatsapp/whatsapp.state';
import { Observable, of, switchMap } from 'rxjs';
import {
  getErrorMessage,
  PageableModel,
  Privilege,
  SearchResultPayloadModel,
  UserAuthorityModel,
  WhatsAppTypeBasicModel,
} from '@grabbill/lib';
import {
  DeleteMultiTemplateWhatsAppType,
  DuplicateMultiTemplateWhatsAppType,
  LoadMoreMultiTemplateWhatsAppTypes,
  QueryMultiTemplateWhatsAppTypes,
  ResetMultiTemplateWhatsAppTypes,
} from '../../../../states/whatsapp/whatsapp.state-actions';
import { NzMessageModule, NzMessageService } from "ng-zorro-antd/message";
import { NzModalService } from 'ng-zorro-antd/modal';
import { AuthState } from '../../../../states/auth/auth.state';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { format } from 'date-fns';
import { environment } from '../../../../environments/environment';
import { hasPrivilege } from '../../../../utils/has-privilege';
import { AppCommonModule } from "../../app-common/app-common.module";
import { NzDividerComponent } from "ng-zorro-antd/divider";
import { NzToolTipModule } from "ng-zorro-antd/tooltip";
import { NzListModule } from "ng-zorro-antd/list";
import { IconsProviderModule } from "../../../icons-provider.module";
import { CommonModule } from "@angular/common";

@Component({
  selector: 'grabbill-client-multi-template-whatsapp-type-list',
  standalone: true,
  imports: [
    CommonModule,
    AppCommonModule,
    NzDividerComponent,
    NzToolTipModule,
    NzListModule,
    NzMessageModule,
    IconsProviderModule,
  ],
  templateUrl: './multi-template-whatsapp-type-list.component.html',
  styleUrl: './multi-template-whatsapp-type-list.component.less',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MultiTemplateWhatsappTypeListComponent extends NgxsBaseComponent {
  @Select(WhatsAppState.multiTemplateWhatsAppTypeSearchResult)
  multiTemplateWhatsAppTypeSearchResult$!: Observable<SearchResultPayloadModel<WhatsAppTypeBasicModel>>;

  @Select(WhatsAppState.multiTemplateWhatsAppTypePageable)
  multiTemplateWhatsAppTypePageable$!: Observable<PageableModel>;

  user?: UserAuthorityModel;

  isListLoading = false;
  queryAction = QueryMultiTemplateWhatsAppTypes;
  resetAction = ResetMultiTemplateWhatsAppTypes;
  loadMoreAction = LoadMoreMultiTemplateWhatsAppTypes;
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
        ofActionCompleted(DeleteMultiTemplateWhatsAppType),
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
        ofActionCompleted(DuplicateMultiTemplateWhatsAppType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `WhatsApp duplicated`));
            this.navigate([
              '/',
              'mt-whatsapp',
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
    this.navigate(['/', 'mt-whatsapp', 'detail', type.id], { tab: 'activities' });
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
        this.store.dispatch(new DeleteMultiTemplateWhatsAppType(whatsAppType.id));
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
        this.store.dispatch(new DuplicateMultiTemplateWhatsAppType(whatsAppType.id));
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

