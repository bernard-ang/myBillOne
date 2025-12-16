import { read, WorkBook, WorkSheet } from 'xlsx';
import { map, Observable, of, switchMap, throwError, toArray } from 'rxjs';
import { filter } from 'rxjs/operators';
import { ContactFieldModel, DataType } from '@grabbill/lib';
import { getIndexFieldName } from './get-index-field-name';
import { parse as dateParse } from 'date-fns';

export const getUploadContactExcelData = (file: File, fields: ContactFieldModel[]) => {
  return of(file).pipe(
    switchMap(readWorkbook),
    filter((workbook) => !!workbook),
    switchMap(parseWorkbook(fields)),
    map(mapRowDataToIndexRow(fields)),
    toArray()
  );
};

const readWorkbook = (file: File) =>
  new Promise<WorkBook>((resolve) => {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      const data = e.target.result;
      const workbook = read(data, { type: 'buffer', cellDates: true });
      resolve(workbook);
    };

    reader.readAsArrayBuffer(file);
  });

const parseWorkbook = (fields: ContactFieldModel[]) => {
  return (workbook: WorkBook) => {
    const worksheet = workbook.Sheets[workbook.SheetNames[0]];

    const errorColumns = validateColumnHeaders(fields, worksheet);
    if (errorColumns.length > 0) {
      return throwError(() => errorColumns);
    }

    return getRowsData(fields, worksheet);
  };
};

const validateColumnHeaders = (fields: ContactFieldModel[], worksheet: WorkSheet): Array<string | null> => {
  let letterCharCode = 'A'.charCodeAt(0);

  return fields
    .map((field) => {
      const row1Cell = worksheet[`${String.fromCharCode(letterCharCode)}1`];

      letterCharCode++;

      if (row1Cell && row1Cell.v.trim() === field.name) {
        return null;
      } else {
        return `Expecting [${field.name}] in column ${String.fromCharCode(letterCharCode - 1)}1 but is [${
          (row1Cell && row1Cell.v) || ''
        }]`;
      }
    })
    .filter((column) => column !== null);
};

const getRowsData = (fields: ContactFieldModel[], worksheet: WorkSheet): Observable<any[]> => {
  let currentRow = 2;
  const rows: any[] = [];

  while (true) {
    let letterCharCode = 'A'.charCodeAt(0);
    let rowHasData = false;

    const rowData = fields.map(() => {
      const cell = worksheet[`${String.fromCharCode(letterCharCode)}${currentRow}`];
      letterCharCode++;
      if (cell) {
        if (!rowHasData && cell.w && cell.w.trim().length > 0) {
          rowHasData = true;
        }
        return cell.w.trim();
      }
    });

    if (!rowHasData) {
      break;
    } else {
      rows.push(rowData);
    }
    currentRow += 1;
  }

  return of(...rows);
};

const mapRowDataToIndexRow = (fields: ContactFieldModel[]) => {
  return (row: any[]) => {
    return fields
      .map((field, idx) => ({ field: field, value: row[idx] }))
      .reduce((previousValue, currentValue) => {
        if (currentValue.field.dataType === DataType.NUMBER) {
          // --- format numbers
          if (currentValue.value && !isNaN(currentValue.value)) {
            previousValue[getIndexFieldName(currentValue.field)] = parseInt(currentValue.value, 10) || 0;
          } else {
            previousValue[getIndexFieldName(currentValue.field)] = currentValue.value;
          }
        } else if (currentValue.field.dataType === DataType.DATE) {
          // --- format dates (e.g. 5/30/22)
          if (currentValue.value) {
            previousValue[getIndexFieldName(currentValue.field)] = dateParse(currentValue.value, 'M/d/yy', new Date());
          }
        } else {
          // --- use as string
          previousValue[getIndexFieldName(currentValue.field)] = currentValue.value;
        }

        return previousValue;
      }, {} as any);
  };
};
