import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';

export const updateAndMarkControlAsDirty = (form: UntypedFormGroup) => {
  const controls = form.controls;
  Object.values(controls).forEach((control) => {
    if (control.invalid) {
      control.markAsDirty();
      control.updateValueAndValidity({ onlySelf: true });
    }
    if (control instanceof UntypedFormArray) {
      const formArray = control as UntypedFormArray;
      for (const arrayControl of formArray.controls) {
        if (arrayControl instanceof UntypedFormGroup) {
          updateAndMarkControlAsDirty(arrayControl);
        }
      }
    }
  });
};
