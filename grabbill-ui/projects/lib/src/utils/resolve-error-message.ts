import { AbstractControl } from '@angular/forms';

export const resolveErrorMessage = (control: AbstractControl): string => {
  if (control.errors && !control.pristine) {
    const messages = Object.keys(control.errors).map((key) => {
      const obj = control.getError(key);
      switch (key) {
        case 'required':
          return 'Required';
        case 'minlength':
          return `Minimum length is ${obj.requiredLength}`;
        case 'maxlength':
          return `Maximum length is ${obj.requiredLength}`;
        case 'min':
          return `Minimum value is ${obj.min}`;
        case 'max':
          return `Maximum value is ${obj.max}`;
        case 'matDatepickerParse':
          return 'Date required';
        case 'pattern':
          return 'Invalid pattern';
        case 'email':
          return 'Invalid email';
        case 'nameExist':
          return 'Name already exist';
        case 'alphanumericWithSpaceOnly':
          return 'Alphanumeric with space only';
        case 'alphanumericWithSpaceDashColonOnly':
          return 'Alphanumeric with space, dash or colon only';
        default:
          return key;
      }
    });

    return messages[0];
  }

  return '';
};
