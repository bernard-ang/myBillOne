import { ValidatorFn, Validators } from '@angular/forms';

export const malaysiaPhoneNoRegex = '^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$';

export const malaysiaMobileNoRegex = '^(\\+?6?01)[02-46-9]-*[0-9]{7}$|^(\\+?6?01)[1]-*[0-9]{8}$';

export const whatsappNoRegex =
  /\+?(9[976]\d|8[987530]\d|6[987]\d|5[90]\d|42\d|3[875]\d|2[98654321]\d|9[8543210]|8[6421]|6[6543210]|5[87654321]|4[987654310]|3[9643210]|2[70]|7|1)\d{1,14}$/;

export const malaysiaPhoneNoValidator = (): ValidatorFn => {
  return Validators.pattern(malaysiaPhoneNoRegex);
};

export const malaysiaMobileNoValidator = (): ValidatorFn => {
  return Validators.pattern(malaysiaMobileNoRegex);
};

export const whatsappNoValidator = (): ValidatorFn => {
  return Validators.pattern(whatsappNoRegex);
};
