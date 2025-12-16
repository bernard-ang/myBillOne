import { AbstractControl, UntypedFormArray } from '@angular/forms';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { ChangeDetectorRef } from '@angular/core';

export const doDrop = (formArray: UntypedFormArray, event: CdkDragDrop<string[]>, cd: ChangeDetectorRef) => {
  const currentItem = formArray.at(event.previousIndex);
  formArray.removeAt(event.previousIndex);
  formArray.insert(event.currentIndex, currentItem);
  cd.markForCheck();
};

export const doDeleteIndex = (formArray: UntypedFormArray, index: number, cd: ChangeDetectorRef): void => {
  formArray.removeAt(index);
  cd.markForCheck();
};

export const doDeleteAllIndex = (formArray: UntypedFormArray): void => {
  for (let i = formArray.length - 1; i >= 0; i--) {
    formArray.removeAt(i);
  }
};

export const getFormArray = (form: AbstractControl, path: string): UntypedFormArray => {
  return form.get(path) as UntypedFormArray;
}
