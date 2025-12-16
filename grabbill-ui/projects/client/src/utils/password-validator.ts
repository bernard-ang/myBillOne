import { AbstractControl, ValidationErrors } from '@angular/forms';
import { environment } from '../environments/environment'

export const passwordValidator = (control: AbstractControl): ValidationErrors | null => {
  if (!environment.production) {
    return null;
  }

  const value: string = control.value;

  const hasLowercase = /[a-z]/.test(value);
  const hasUppercase = /[A-Z]/.test(value);
  const hasNumber = /[0-9]/.test(value);
  const hasSpecialCharacter = /[ `!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~]/.test(value);

  if (hasLowercase && hasUppercase && hasNumber && hasSpecialCharacter) {
    return null;
  }
  return { 'One lowercase, one uppercase, one number and one special character is required': { value: control.value } };
};
