import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import {
  ApiResponseModel,
  DownloadFile,
  getPageableQueryString,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { environment } from '../environments/environment';
import { format } from 'date-fns';

@Injectable({
  providedIn: 'root',
})
export class ApiHttpService {
  readonly baseRoute = `${environment.config.baseApiUrl}`;

  constructor(private http: HttpClient) {}

  query<T = ApiResponseModel<SearchResultPayloadModel<any>>>(
    path: string,
    pageable: PageableModel,
    filters: { [key: string]: any | undefined } = {},
    options = { localDate: false }
  ): Observable<T> {
    let queryString = getPageableQueryString(pageable);
    for (const [key, value] of Object.entries(filters)) {
      if (value) {
        if (value instanceof Date) {
          if (options.localDate) {
            queryString += `&${key}=${encodeURIComponent(format(value, 'yyyy-MM-dd'))}`;
          } else {
            queryString += `&${key}=${encodeURIComponent((value as Date).toISOString())}`;
          }
        } else {
          queryString += `&${key}=${encodeURIComponent(value)}`;
        }
      }
    }

    return this.http.get<T>(`${this.baseRoute}/${path}?${queryString}`, {
      withCredentials: true,
    });
  }

  get<T = ApiResponseModel<any>>(path: string): Observable<T> {
    return this.http.get<T>(`${this.baseRoute}/${path}`, {
      withCredentials: true,
    });
  }

  post<T = ApiResponseModel<any>>(path: string, body: any): Observable<T> {
    return this.http.post<T>(`${this.baseRoute}/${path}`, body, {
      withCredentials: true,
    });
  }

  put<T = ApiResponseModel<any>>(path: string, body: any): Observable<T> {
    return this.http.put<T>(`${this.baseRoute}/${path}`, body, {
      withCredentials: true,
    });
  }

  delete<T = ApiResponseModel<any>>(path: string, body?: any): Observable<T> {
    return this.http.delete<T>(`${this.baseRoute}/${path}`, {
      withCredentials: true,
      body,
    });
  }

  getBlob<T = DownloadFile>(path: string): Observable<DownloadFile> {
    return this.http
      .get<Blob>(`${this.baseRoute}/${path}`, {
        responseType: 'blob' as 'json',
        withCredentials: true,
        observe: 'response',
      })
      .pipe(
        map((response) => {
          return {
            name: response.headers.get('content-disposition')!.split('filename=')[1].replace(new RegExp('"', 'g'), ''),
            blob: response.body!,
          } as DownloadFile;
        })
      );
  }

  getImage<Blob>(path: string): Observable<Blob> {
    return this.http
      .get<Blob>(`${this.baseRoute}/${path}`, {
        responseType: 'blob' as 'json',
        withCredentials: true,
        observe: 'response',
      })
      .pipe(
        map((response) => {
          return response.body!;
        })
      );
  }
}
