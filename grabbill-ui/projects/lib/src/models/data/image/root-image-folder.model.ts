import { ImageFolderModel } from './image-folder.model';
import { ImageModel } from './image.model';

export interface RootImageFolderModel {
  imageCount: number;
  imageFolders: ImageFolderModel[];
  images: ImageModel[];
}
