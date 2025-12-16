import { AbstractControl, ValidationErrors } from "@angular/forms";

export const pdfFilenameValidator = (control: AbstractControl): ValidationErrors | null => {
  const value: string = control.value;
  if (!value?.endsWith('.pdf')) {
    return { 'Attachment must be a pdf': { value: control.value } };
  }
  return null;
};

export const filenameValidator = (control: AbstractControl): ValidationErrors | null => {
  const value: string = control.value;

  const regex = /^[^\\/:*?"<>|]+\.[^\\/:*?"<>|]+$/;

  if (!regex.test(value)) {
    return { 'Invalid filename': { value: control.value } };
  }
  return null;
};
