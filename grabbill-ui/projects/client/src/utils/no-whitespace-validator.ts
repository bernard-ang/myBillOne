import { AbstractControl, ValidationErrors } from "@angular/forms";

export const noWhitespaceValidator = (control: AbstractControl): ValidationErrors | null => {
  const value: string = control.value;
  if (value?.includes(' ')) {
    return { 'No whitespace': { value: control.value } };
  }
  return null;
};
