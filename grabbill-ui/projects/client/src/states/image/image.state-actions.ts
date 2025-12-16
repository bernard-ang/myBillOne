export class GetRootImageFolder {
  static readonly type = '[Image] Get Root Image Folder';
}

export class CreateFolder {
  static readonly type = '[Image] Create Folder';

  constructor(public name: string) {}
}


export class DeleteFolder {
  static readonly type = '[Image] Delete Folder';

  constructor(public name: string) {}
}

export class UploadImage {
  static readonly type = '[Image] Upload Image';

  constructor(public fileFormData: FormData, public folder?: string) {}
}

export class DeleteImage {
  static readonly type = '[Image] Delete Image';

  constructor(public id: number) {}
}
