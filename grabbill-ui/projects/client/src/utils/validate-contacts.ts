import { ContactFieldModel, ContactRequestModel, DataType } from '@grabbill/lib';
import { getIndexFieldName } from './get-index-field-name';
import { UntypedFormControl, Validators } from '@angular/forms';

export interface ContactRow {
  name: string;
  row: number;
}

export interface ContactRowError {
  row: number;
  messages: string[];
}

export const validateContactRows = (contacts: ContactRequestModel[], fields: ContactFieldModel[]) => {
  const errors: ContactRowError[] = [];

  contacts.map((contact, index) => {
    const error: ContactRowError = {
      row: index + 2,
      messages: [],
    };

    fields.map((field) => {
      const fieldName = getIndexFieldName(field);
      const value = (contact as any)[fieldName];

      if (field.required && !value) {
        error.messages.push(`${field.label} (${field.name}) is required`);
      }

      if (value) {
        if (field.dataType === DataType.NUMBER && isNaN(value)) {
          error.messages.push(`${field.label} (${field.name}) is not a number`);
        }

        if (field.dataType === DataType.DATE && !(value instanceof Date)) {
          error.messages.push(`${field.label} (${field.name}) is not a date`);
        }

        const control = new UntypedFormControl(value, Validators.email);
        if (field.dataType === DataType.EMAIL && control.errors && control.errors['email']) {
          error.messages.push(`${field.label} (${field.name}) is not an email`);
        }
      }
    });

    if (error.messages.length > 0) {
      errors.push(error);
    }
  });

  return errors;
};
