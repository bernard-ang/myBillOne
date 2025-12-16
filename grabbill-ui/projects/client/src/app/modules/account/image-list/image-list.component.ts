import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, ValidationErrors, ValidatorFn, Validators } from "@angular/forms";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { NzMessageService } from "ng-zorro-antd/message";
import { NzUploadFile } from "ng-zorro-antd/upload";
import { NzModalService } from "ng-zorro-antd/modal";
import { Observable, of, switchMap } from "rxjs";
import { getErrorMessage, resolveErrorMessage, RootImageFolderModel, updateAndMarkControlAsDirty } from "@grabbill/lib";
import { environment } from "../../../../environments/environment";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import {
  CreateFolder,
  DeleteFolder,
  DeleteImage,
  GetRootImageFolder,
  UploadImage
} from "../../../../states/image/image.state-actions";
import { ImageState } from "../../../../states/image/image.state";
import { ShowMessage } from "../../../../states/common/common.state-actions";
import prettyBytes from "pretty-bytes";

export const makeNameValidator = (store: Store): ValidatorFn => {
  return (control: AbstractControl): ValidationErrors => {
    const rootFolder = store.selectSnapshot(ImageState.rootFolder);
    if (!rootFolder) {
      return {};
    }

    const folderNames = rootFolder.imageFolders.map((folder) => folder.name);

    return folderNames.includes(control.value) ? { nameExist: true } : {};
  };
};

@Component({
  selector: 'grabbill-client-image-list',
  templateUrl: './image-list.component.html',
  styleUrls: ['./image-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageListComponent extends NgxsBaseComponent {
  @Select(ImageState.rootFolder)
  rootFolder$!: Observable<RootImageFolderModel>;

  isCreateFolderModalVisible = false;

  createFolderForm: UntypedFormGroup;

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private modal: NzModalService,
    private cd: ChangeDetectorRef,
    private fb: UntypedFormBuilder,
    private actions$: Actions
  ) {
    super(store, messageService);
    this.createFolderForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255), makeNameValidator(store)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(GetRootImageFolder),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(CreateFolder),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Folder created`));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteFolder),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Folder deleted`));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UploadImage),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Image uploaded`));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteImage),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Image deleted`));
          }
          return of(false);
        })
      )
    );

    this.store.dispatch(new GetRootImageFolder());
  }

  doCloseCreateFolderModal() {
    this.isCreateFolderModalVisible = false;
    this.cd.markForCheck();
  }

  doOpenCreateFolderModal() {
    this.createFolderForm.reset();
    this.isCreateFolderModalVisible = true;
  }

  doDeleteFolder(name: string) {
    this.modal.confirm({
      nzTitle: `Delete folder ${name}`,
      nzContent:
        'After delete the following folder, old email which reference the following folder image will no longer available.',
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeleteFolder(name));
      },
      nzCancelText: 'No',
    });
  }

  doDeleteImage(name: string, id: number) {
    this.modal.confirm({
      nzTitle: `Delete image ${name}`,
      nzContent:
        'After delete the following image, old email which reference the following image will no longer available.',
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeleteImage(id));
      },
      nzCancelText: 'No',
    });
  }

  doCreateFolder() {
    if (this.createFolderForm.valid) {
      const folderName = this.createFolderForm.getRawValue().name;
      this.store.dispatch(new CreateFolder(folderName));
      this.isCreateFolderModalVisible = false;
    } else {
      updateAndMarkControlAsDirty(this.createFolderForm);
      this.cd.markForCheck();
    }
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  beforeUploadFolderImage(folder: string) {
    return (file: NzUploadFile): boolean => {
      return this.uploadImage(file, folder);
    };
  }

  beforeUploadImage = (file: NzUploadFile): boolean => {
    return this.uploadImage(file);
  };

  private uploadImage(file: NzUploadFile | (NzUploadFile & File), folder?: string) {
    if (file.size! > environment.config.maxImageSizeBytes) {
      this.modal.error({
        nzTitle: 'Invalid image size',
        nzContent: `Maximum image size is ${prettyBytes(environment.config.maxImageSizeBytes)}`,
      });
      return false;
    }

    this.cd.markForCheck();

    if (file instanceof File) {
      const formData = new FormData();
      formData.append('file', file);
      this.store.dispatch(new UploadImage(formData, folder));
    }

    this.cd.markForCheck();
    return false;
  }
}
