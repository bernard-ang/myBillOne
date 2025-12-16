import { AbstractControl, ValidationErrors } from '@angular/forms';

export const bodyValidator = (control: AbstractControl): ValidationErrors | null => {
  const value: string = control.value;
  const regex = /\{\{([\w\d]+)\}\}/g;

  if (value) {
    const matches = value.match(regex);
    if (matches) {
      const invalidVariables = [];
      for (let i = 0; i < matches.length; i++) {
        const match = matches[i];
        const cleanMatch = match.replace('{{', '').replace('}}', '');
        const matchNumber = Number(cleanMatch);

        if (isNaN(matchNumber)) {
          invalidVariables.push(match);
        } else if (matchNumber > i + 1) {
          invalidVariables.push(match);
        }
      }

      if (invalidVariables.length > 0) {
        let obj: any = {};
        obj['Invalid variable ' + invalidVariables.join(', ')] = { value: control.value };
        return obj;
      }
    }
  }

  return null;
};
