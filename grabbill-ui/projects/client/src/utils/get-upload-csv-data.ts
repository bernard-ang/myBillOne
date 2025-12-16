import { map, of, switchMap, throwError } from 'rxjs';
import { BaseIndexFieldModel, DataType } from '@grabbill/lib';
import { parse, ParseResult } from 'papaparse';
import { getIndexFieldName } from './get-index-field-name';
import { parse as dateParse } from 'date-fns';

export const getUploadCsvData = (file: File, fields: BaseIndexFieldModel[], csvSeparator: string) => {
  return of(file).pipe(switchMap(readCsv(fields, csvSeparator)), map(mapObjectsToIndexRow(fields)));
};

const readCsv = (fields: BaseIndexFieldModel[], csvSeparator: string) => {
  return (file: File) =>
    new Promise<any[]>((resolve, reject) => {
      parse(file as any, {
        header: true,
        skipEmptyLines: true,
        delimiter: csvSeparator,
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

const validateCsvHeaders = (fields: BaseIndexFieldModel[], headers: string[]) => {
  const applicableFields = fields.filter((field) => field.applicable);
  const headerErrors: string[] = [];
  for (let i = 0; i < applicableFields.length; i++) {
    const field = fields[i];
    const header = headers[i] ? headers[i].trim() : headers[i];
    if (field.header !== header) {
      headerErrors.push(`Expecting [${field.header}] but is [${header}]`);
    }
  }
  if (headerErrors.length > 0) {
    throwError(() => headerErrors);
  }
};

const mapObjectsToIndexRow = (fields: BaseIndexFieldModel[]) => {
  return (rows: any[]) => {
    return rows.map((row) => {
      let indexRow: any = {};
      fields
        .filter((field) => field.applicable)
        .map((field) => {
          const value = row[field.header] ? row[field.header].trim() : row[field.header];

          if (field.dataType === DataType.NUMBER) {
            if (value && !isNaN(value)) {
              indexRow[getIndexFieldName(field)] = parseInt(value, 10) || 0;
            } else {
              indexRow[getIndexFieldName(field)] = value;
            }
          } else if (field.dataType === DataType.DATE) {
            // --- format dates (e.g. 13/05/2022)
            if (row[field.header]) {
              indexRow[getIndexFieldName(field)] = dateParse(value, 'dd/MM/yyyy', new Date());
            }
          } else {
            indexRow[getIndexFieldName(field)] = value;
          }
        });
      return indexRow;
    });
  };
};
