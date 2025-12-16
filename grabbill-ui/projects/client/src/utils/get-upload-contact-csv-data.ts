import { map, of, switchMap, throwError } from "rxjs";
import { ContactFieldModel, DataType } from "@grabbill/lib";
import { parse, ParseResult } from "papaparse";
import { getIndexFieldName } from "./get-index-field-name";
import { addHours, parse as dateParse } from "date-fns";

export const getUploadContactCsvData = (file: File, fields: ContactFieldModel[]) => {
  return of(file).pipe(switchMap(readContactCsv(fields)), map(mapContactObjectsToIndexRow(fields)));
};

const readContactCsv = (fields: ContactFieldModel[]) => {
  return (file: File) =>
    new Promise<any[]>((resolve, reject) => {
      parse(file as any, {
        header: true,
        skipEmptyLines: true,
        delimiter: ',',
        complete: (results: ParseResult<any>) => {
          const headers = results.meta.fields;

          if (results.errors.length > 0) {
            reject(results.errors.map((error) => error.message));
          }

          if (!headers || headers.length === 0) {
            reject(['Header not found']);
          } else {
            validateCsvHeaders(fields, headers);
            resolve(results.data);
          }
        },
        error: (error: Error) => {
          reject([error.message]);
        },
      });
    });
};

const validateCsvHeaders = (fields: ContactFieldModel[], headers: string[]) => {
  const headerErrors: string[] = [];
  for (let i = 0; i < fields.length; i++) {
    const field = fields[i];
    const header = headers[i] ? headers[i].trim() : headers[i];
    if (field.name !== header) {
      headerErrors.push(`Expecting [${field.name}] but is [${header}]`);
    }
  }
  if (headerErrors.length > 0) {
    throwError(() => headerErrors);
  }
};

const mapContactObjectsToIndexRow = (fields: ContactFieldModel[]) => {
  return (rows: any[]) => {
    return rows.map((row) => {
      let contact: any = {};
      fields
        .map((field) => {
          const value = row[field.name] ? row[field.name].trim() : row[field.name];

          if (field.dataType === DataType.NUMBER) {
            if (value && !isNaN(value)) {
              contact[getIndexFieldName(field)] = parseInt(value, 10) || 0;
            } else {
              contact[getIndexFieldName(field)] = value;
            }
          } else if (field.dataType === DataType.DATE) {
            // --- format dates (e.g. 13/05/2022)
            if (row[field.name]) {
              let date = dateParse(value, 'dd/MM/yyyy', new Date());
              // adjust back malaysia hours
              date = addHours(date, 8)
              contact[getIndexFieldName(field)] = date;
              console.log(contact[getIndexFieldName(field)]);
            }
          } else {
            contact[getIndexFieldName(field)] = value;
          }
        });
      console.log(contact);
      return contact;
    });
  };
};
