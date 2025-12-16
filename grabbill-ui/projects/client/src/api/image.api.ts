import { Observable } from 'rxjs';
import { ApiResponseModel, DownloadFile, ImagesPayloadModel, RootImageFolderModel } from '@grabbill/lib';

export abstract class ImageApi {
  abstract listAll(): Observable<ApiResponseModel<RootImageFolderModel>>;

  abstract uploadImage(file: FormData, folder?: string): Observable<ApiResponseModel<ImagesPayloadModel>>;

  abstract downloadImage(id: string): Observable<DownloadFile>;

  abstract deleteImage(id: number): Observable<ApiResponseModel<ImagesPayloadModel>>;

  abstract createFolder(name: string): Observable<ApiResponseModel<RootImageFolderModel>>;

  abstract deleteFolder(name: string): Observable<ApiResponseModel<RootImageFolderModel>>;

}
