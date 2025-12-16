import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseModel, DownloadFile, ImagesPayloadModel, RootImageFolderModel } from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { ImageApi } from '../api/image.api';

@Injectable({
  providedIn: 'root'
})
export class ImageService implements ImageApi {
  readonly baseRoute = `int/images`;

  constructor (private http: ApiHttpService) {
  }

  listAll (): Observable<ApiResponseModel<RootImageFolderModel>> {
    return this.http.get(this.baseRoute);
  }

  uploadImage (fileFormData: FormData, folder?: string): Observable<ApiResponseModel<ImagesPayloadModel>> {
    if (folder) {
      fileFormData.set('folder', folder);
    }

    return this.http.post<ApiResponseModel<ImagesPayloadModel>>(
      this.baseRoute,
      fileFormData
    );
  }

  downloadImage (id: string): Observable<DownloadFile> {
    return this.http.getBlob(`${this.baseRoute}/${id}`);
  }

  deleteImage (id: number): Observable<ApiResponseModel<ImagesPayloadModel>> {
    return this.http.delete<ApiResponseModel<ImagesPayloadModel>>(`${this.baseRoute}/${id}`);
  }

  createFolder (name: string): Observable<ApiResponseModel<RootImageFolderModel>> {
    return this.http.post<ApiResponseModel<RootImageFolderModel>>(`${this.baseRoute}/create-folder`, { name });
  }

  deleteFolder (name: string): Observable<ApiResponseModel<RootImageFolderModel>> {
    return this.http.delete<ApiResponseModel<RootImageFolderModel>>(`${this.baseRoute}/delete-folder`, { name });
  }
}
