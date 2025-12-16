import { ImageModel } from "./image.model";

export interface ImageFolderModel {
  id: string;
  name: string;
  images: ImageModel[];
}

