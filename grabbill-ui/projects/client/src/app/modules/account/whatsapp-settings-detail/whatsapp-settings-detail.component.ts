import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import {
  AccountModel,
  getErrorMessage,
  resolveErrorMessage,
  updateAndMarkControlAsDirty, UserModel,
  WhatsappCategory,
  WhatsappTemplateModel
} from "@grabbill/lib";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { WhatsAppState } from "../../../../states/whatsapp/whatsapp.state";
import { Observable, of, switchMap, tap } from "rxjs";
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { NzMessageService } from "ng-zorro-antd/message";
import { NzModalService } from "ng-zorro-antd/modal";
import {
  CreateWhatsAppTemplate,
  DeleteWhatsAppTemplate,
  GetWhatsAppTemplates,
  UpdateAutoReplyMessage
} from "../../../../states/whatsapp/whatsapp.state-actions";
import { ShowMessage } from "../../../../states/common/common.state-actions";
import { NzUploadFile } from "ng-zorro-antd/upload";
import { environment } from "../../../../environments/environment";
import prettyBytes from "pretty-bytes";
import { AuthState } from "../../../../states/auth/auth.state";
import { bodyValidator } from "../../../../utils/template-body-validator";
import {
  getWhatsAppTemplateBody,
  getWhatsAppTemplateButton,
  getWhatsAppTemplateFooter,
  getWhatsAppTemplateHeader
} from "../../../../utils/whatsapp-template";
import { isOnPremiseMode } from "../../../../utils/deployment-mode";
import {
  RegisterWabaWebhook,
  TestWabaLogin,
  UnregisterWabaWebhook,
  UpdateWabaInfo
} from "../../../../states/account/account.state-actions";

@Component({
  selector: "grabbill-client-whatsapp-settings-detail",
  templateUrl: "./whatsapp-settings-detail.component.html",
  styleUrls: [ "./whatsapp-settings-detail.component.less" ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WhatsappSettingsDetailComponent extends NgxsBaseComponent {
  @Select(WhatsAppState.templates)
  templates$!: Observable<WhatsappTemplateModel[]>;

  @Select(AuthState.user)
  user$!: Observable<UserModel>;

  data: WhatsappTemplateModel[] = [];

  isTableLoading = true;

  isCreateFormVisible = false;
  isCreateFormLoading = false;
  isUploadSampleDisable = true;
  createForm: UntypedFormGroup;

  autoReplyForm: UntypedFormGroup;
  isSavingAutoReplyMessage = false;

  isUpdateWabaModalVisible = false;
  isUpdateWabaFormLoading = false;
  updateWabaForm: UntypedFormGroup;

  category = WhatsappCategory;

  file?: File;

  nameFilter: string = "";

  account?: AccountModel;

  isOnPremise = isOnPremiseMode();

  constructor (
    protected override store: Store,
    protected override messageService: NzMessageService,
    private modal: NzModalService,
    private cd: ChangeDetectorRef,
    private fb: UntypedFormBuilder,
    private actions$: Actions
  ) {
    super(store, messageService);
    this.createForm = this.fb.group({
      name: [ "", [ Validators.required, Validators.maxLength(512) ] ],
      category: [ "", [ Validators.required ] ],
      hasAttachment: [ false, [] ],
      sample: [ null ],
      body: [ "", [ Validators.required, Validators.maxLength(1024), bodyValidator ] ],
      footer: [ "", Validators.maxLength(60) ],
      hasAcknowledgementButton: [ false, [] ]
    });
    this.autoReplyForm = this.fb.group({
      message: [ "", [ Validators.required ] ]
    });
    this.updateWabaForm = this.fb.group({
      wabaEmail: [ "", [ Validators.required ] ],
      wabaPassword: [ undefined ],
      wabaId: [ undefined ],
      wabaGuid: [ undefined, [ Validators.required ] ],
      wabaName: [ undefined ],
      wabaPhone: [ undefined ],
      wabaPhoneId: [ undefined ]
    });
  }

  override ngOnInit (): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.user$.pipe(
        tap(user => {
          this.account = user.account;
          this.autoReplyForm.get("message")?.setValue(this.account.wabaAutoReplyMessage);
          if (this.account.wabaGuid) {
            this.store.dispatch(new GetWhatsAppTemplates());
          }
        })
      ),
      this.createForm.get("hasAttachment")!.valueChanges.pipe(
        tap((value) => {
          this.isUploadSampleDisable = !value;
          this.cd.markForCheck();
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetWhatsAppTemplates),
        switchMap((data: ActionCompletion) => {
          this.isTableLoading = false;
          this.cd.markForCheck();
          if (data.result.error) {
            this.store.dispatch(new ShowMessage("error", getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteWhatsAppTemplate),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage("error", getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage("info", `Template deleted`));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(CreateWhatsAppTemplate),
        switchMap((data: ActionCompletion) => {
          this.isCreateFormLoading = false;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage("error", getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isCreateFormVisible = false;
            this.store.dispatch(new ShowMessage("info", `Template created`));
          }
          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateAutoReplyMessage),
        switchMap((data: ActionCompletion) => {
          this.isSavingAutoReplyMessage = false;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage("error", getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage("info", `Auto reply message updated`));
          }
          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateWabaInfo),
        switchMap((data: ActionCompletion) => {
          this.isUpdateWabaFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage("error", getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isUpdateWabaModalVisible = false;
            this.store.dispatch(new ShowMessage("info", `Waba info updated`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(RegisterWabaWebhook),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage("error", getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage("info", `Webhook registered successfully`));
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
              nzTitle: "Test WABA Login",
              nzContent: getErrorMessage(data.result.error)
            });
          } else if (data.result.successful) {
            this.modal.info({
              nzTitle: "Test WABA Login",
              nzContent: "WABA login successfully"
            });
          }

          this.cd.markForCheck();
          return of(false);
        })
      )
    );
  }

  doFilter (event: Event) {
    this.nameFilter = (event.target as any).value;
    this.cd.markForCheck();
  }

  getFilter (templates: WhatsappTemplateModel[]) {
    return templates.filter((template) => template.name.includes(this.nameFilter));
  }

  getBody (template: WhatsappTemplateModel) {
    return getWhatsAppTemplateBody(template);
  }

  getHeader (template: WhatsappTemplateModel) {
    return getWhatsAppTemplateHeader(template);
  }

  getFooter (template: WhatsappTemplateModel) {
    return getWhatsAppTemplateFooter(template);
  }

  getButton (template: WhatsappTemplateModel) {
    return getWhatsAppTemplateButton(template);
  }

  doDelete (name: string, waTemplateId: string) {
    this.modal.confirm({
      nzTitle: `Delete template ${name}`,
      nzOkText: "Yes",
      nzOkType: "primary",
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeleteWhatsAppTemplate(waTemplateId));
      },
      nzCancelText: "No"
    });
  }

  doOpenCreateTemplate () {
    this.createForm.reset();
    this.isCreateFormVisible = true;
    this.file = undefined;
    this.cd.markForCheck();
  }

  closeCreateForm () {
    this.isCreateFormVisible = false;
    this.cd.markForCheck();
  }

  doCreateTemplate () {
    this.isCreateFormLoading = true;
    this.cd.markForCheck();

    if (this.createForm.valid) {
      const value = this.createForm.getRawValue();
      const formData = new FormData();
      const hasAttachment = value.hasAttachment || false;

      formData.append(
        "request",
        JSON.stringify({
          name: value.name,
          category: value.category,
          hasAttachment: hasAttachment,
          body: value.body,
          footer: value.footer,
          hasAcknowledgementButton: value.hasAcknowledgementButton || false
        })
      );

      if (hasAttachment) {
        if (this.file) {
          formData.append("sample", this.file);
        } else {
          this.modal.error({
            nzTitle: "Missing Sample Document"
          });
          this.isCreateFormLoading = false;
          this.cd.markForCheck();
          return;
        }
      }

      this.store.dispatch(new CreateWhatsAppTemplate(formData));
    } else {
      updateAndMarkControlAsDirty(this.createForm);
      this.isCreateFormLoading = false;
      this.cd.markForCheck();
    }
  }

  getErrorMessage (control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  beforeUpload = (file: NzUploadFile): boolean => {
    if (file.size! > environment.config.maxFileSizeBytes) {
      this.modal.error({
        nzTitle: "Invalid size",
        nzContent: `Maximum PDF size is ${prettyBytes(environment.config.maxImageSizeBytes)}`
      });
      return false;
    }

    this.cd.markForCheck();

    if (file instanceof File) {
      this.file = file;
    }

    this.cd.markForCheck();
    return false;
  };

  doUpdateAutoReplyMessage () {
    this.isSavingAutoReplyMessage = true;
    this.cd.markForCheck();

    if (this.autoReplyForm.valid) {
      const value = this.autoReplyForm.getRawValue();

      this.store.dispatch(new UpdateAutoReplyMessage(value.message));
    } else {
      updateAndMarkControlAsDirty(this.autoReplyForm);
      this.isSavingAutoReplyMessage = false;
      this.cd.markForCheck();
    }
  }

  doOpenUpdateWabaInfoModal (account: AccountModel) {
    this.updateWabaForm.setValue({
      wabaEmail: account.wabaEmail || null,
      wabaPassword: null,
      wabaId: account.wabaId || null,
      wabaGuid: account.wabaGuid || null,
      wabaName: account.wabaName || null,
      wabaPhone: account.wabaPhone || null,
      wabaPhoneId: account.wabaPhoneId || null
    });
    this.isUpdateWabaModalVisible = true;
    this.cd.markForCheck();
  }

  doCloseUpdateWabaInfoModal () {
    this.isUpdateWabaModalVisible = false;
    this.cd.markForCheck();
  }

  doUpdateWabaInfo () {
    this.isUpdateWabaFormLoading = true;
    this.cd.markForCheck();

    if (this.updateWabaForm.valid) {
      const value = this.updateWabaForm.getRawValue();
      this.store.dispatch(
        new UpdateWabaInfo({
          wabaEmail: value.wabaEmail,
          wabaPassword: value.wabaPassword && value.wabaPassword.trim() !== "" ? value.wabaPassword.trim() : null,
          wabaId: value.wabaId,
          wabaGuid: value.wabaGuid,
          wabaName: value.wabaName,
          wabaPhoneId: value.wabaPhoneId,
          wabaPhone: value.wabaPhone
        })
      );
    } else {
      updateAndMarkControlAsDirty(this.updateWabaForm);
      this.isUpdateWabaFormLoading = false;
      this.cd.markForCheck();
    }
  }

  doTestWabaLogin () {
    this.modal.confirm({
      nzTitle: `Test WABA Login`,
      nzOkText: "Yes",
      nzOkType: "primary",
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new TestWabaLogin());
      },
      nzCancelText: "No"
    });
  }

  doRegisterWebhook () {
    this.modal.confirm({
      nzTitle: `Register Webhook`,
      nzOkText: "Yes",
      nzOkType: "primary",
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new RegisterWabaWebhook());
      },
      nzCancelText: "No"
    });
  }

  doUnregisterWebhook () {
    this.modal.confirm({
      nzTitle: `Unregister Webhook`,
      nzOkText: "Yes",
      nzOkType: "primary",
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new UnregisterWabaWebhook());
      },
      nzCancelText: "No"
    });
  }

  getStatusColor (status: string) {
    switch (status) {
      case "APPROVED":
        return "success";
      case "PENDING":
        return "warning";
      default:
        return "error";
    }
  }
}
