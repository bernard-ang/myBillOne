import { Injectable } from '@angular/core';
import produce from 'immer';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { ApiResponseModel, RootImageFolderModel } from '@grabbill/lib';
import { ImageStateModel } from './image-state.model';
import { ImageApi } from '../../api/image.api';
import { CreateFolder, DeleteFolder, DeleteImage, GetRootImageFolder, UploadImage } from './image.state-actions';

@State<ImageStateModel>({
  name: "image",
  defaults: {}
})
@Injectable()
export class ImageState {
  constructor (private imageApi: ImageApi) {
  }

  @Selector()
  static rootFolder (state: ImageStateModel) {
    return state.rootFolder;
  }

  @Action(GetRootImageFolder)
  getRootImageFolder (context: StateContext<ImageStateModel>) {
    return this.imageApi.listAll().pipe(
      tap((response: ApiResponseModel<RootImageFolderModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.rootFolder = response.data;
          })
        );
      })
    );
  }

  @Action(CreateFolder)
  createFolder (context: StateContext<ImageStateModel>, { name }: CreateFolder) {
    return this.imageApi.createFolder(name).pipe(
      tap((response: ApiResponseModel<RootImageFolderModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.rootFolder = response.data;
          })
        );
      })
    );
  }

  @Action(DeleteFolder)
  deleteFolder (context: StateContext<ImageStateModel>, { name }: DeleteFolder) {
    return this.imageApi.deleteFolder(name).pipe(
      tap((response: ApiResponseModel<RootImageFolderModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.rootFolder = response.data;
          })
        );
      })
    );
  }

  @Action(UploadImage)
  uploadImage (context: StateContext<ImageStateModel>, { fileFormData, folder }: UploadImage) {
    return this.imageApi.uploadImage(fileFormData, folder).pipe(
      tap(() => {
        context.dispatch(new GetRootImageFolder())
      })
    );
  }

  @Action(DeleteImage)
  deleteImage (context: StateContext<ImageStateModel>, { id }: DeleteImage) {
    return this.imageApi.deleteImage(id).pipe(
      tap(() => {
        context.dispatch(new GetRootImageFolder())
      })
    );
  }

}
