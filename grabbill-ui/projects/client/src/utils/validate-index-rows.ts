import { BaseIndexFieldModel, BaseIndexRowModel, DataType, DomainType } from '@grabbill/lib';
import { getIndexFieldName } from './get-index-field-name';
import { UntypedFormControl, Validators } from '@angular/forms';
import { filenameValidator, pdfFilenameValidator } from './filename-validator';
import { malaysiaMobileNoValidator, whatsappNoValidator } from './phone-regex';

export interface RowFile {
  name: string;
  row: number;
}

export interface IndexRowError {
  row: number;
  messages: string[];
}

export const validateBaseIndexRows = (
  domainType: DomainType,
  indexRows: BaseIndexRowModel[],
  fields: BaseIndexFieldModel[],
  validCode?: string
) => {
  return validateIndexRows(domainType, indexRows,  fields, false, false, [], validCode);
}

export const validateIndexRows = (
  domainType: DomainType,
  indexRows: BaseIndexRowModel[],
  fields: BaseIndexFieldModel[],
  skipInvalidEmail = false,
  validatePhoneNumber = false,
  validTemplateNames: string[] = [],
  validCode?: string
) => {
  const errors: IndexRowError[] = [];
  const files: RowFile[] = [];

  indexRows.map((row, index) => {
    const error: IndexRowError = {
      row: index + 2,
      messages: [],
    };

    files.push({ name: row.text1, row: index + 2 });

    fields.map((field) => {
      const fieldName = getIndexFieldName(field);
      const value = (row as any)[fieldName];

      if (validCode && field.label === 'Code' && field.header === 'code' && value !== validCode) {
        error.messages.push(`${field.label} (${field.header})'s value (${value}) is invalid expected (${validCode}).`);
      }

      if (field.required && field.applicable && !value) {
        error.messages.push(`${field.label} (${field.header}) is required.`);
      }

      if (value && field.applicable) {
        if (field.dataType === DataType.NUMBER && isNaN(value)) {
          error.messages.push(`${field.label} (${value}) is not a number.`);
        }

        if (field.dataType === DataType.DATE && !(value instanceof Date)) {
          error.messages.push(`${field.label} (${value}) is not a date. Expected date format (DD/MM/YYYY).`);
        }

        if (!skipInvalidEmail) {
          const control = new UntypedFormControl(value, Validators.email);
          if (field.dataType === DataType.EMAIL && control.errors && control.errors['email']) {
            error.messages.push(`${field.label} (${value}) is not an email.`);
          }
        }

        if (validatePhoneNumber && field.dataType === DataType.TEXT && field.label === 'Mobile No') {
          const control = new UntypedFormControl(value, malaysiaMobileNoValidator());
          if (field.dataType === DataType.TEXT && control.errors && control.errors['pattern']) {
            error.messages.push(`${field.label} (${value}) is not valid Malaysia mobile number.`);
          }
        }

        if (validatePhoneNumber && field.dataType === DataType.TEXT && field.label === 'WhatsApp No') {
          if (domainType === DomainType.WHATSAPP || domainType === DomainType.MT_WHATSAPP) {
            const control = new UntypedFormControl(value, whatsappNoValidator());
            if (field.dataType === DataType.TEXT && control.errors && control.errors['pattern']) {
              error.messages.push(`${field.label} (${value}) is not valid whatsapp number.`);
            }
          }
        }

        if (field.dataType === DataType.TEXT && field.label === 'Attachment Filename') {
          if (domainType === DomainType.TRANSACTIONAL_EMAIL || domainType === DomainType.WHATSAPP || domainType === DomainType.MT_WHATSAPP) {
            const control = new UntypedFormControl(value, pdfFilenameValidator);
            if (control.errors) {
              error.messages.push(`${field.label} (${value}) is not valid pdf filename.`);
            }
          } else if (domainType === DomainType.DIGITAL_FILING) {
            const control = new UntypedFormControl(value, filenameValidator);
            if (control.errors) {
              error.messages.push(`${field.label} (${value}) is not valid filename.`);
            }
          }
        }

        if (field.dataType === DataType.TEXT && field.label === 'Template') {
          if (domainType === DomainType.MT_WHATSAPP) {
            if (!validTemplateNames.includes(value)) {
              error.messages.push(`${field.label} (${value}) is not valid template.`);
            }
          }
        }

        if (field.dataType === DataType.TEXT && field.label === 'Template Name') {
          if (domainType === DomainType.MT_TRANSACTIONAL_EMAIL) {
            if (!validTemplateNames.includes(value)) {
              error.messages.push(`${field.label} (${value}) is not valid template name.`);
            }
          }
        }
      }
    });

    if (error.messages.length > 0) {
      errors.push(error);
    }
  });

  return errors;
};
