// @ts-ignore
import * as XlsxPopulate from 'xlsx-populate/browser/xlsx-populate-no-encryption';
import { format } from 'date-fns';
import { openBlob } from './open-blob';
import { BaseIndexFieldModel, BaseTypeDataModel, DataType } from '@grabbill/lib';
import { getIndexFieldValue } from './get-index-row-values';

const EXPORT_FILENAME = 'index-data';

export interface FormIndexData extends BaseTypeDataModel {
  id: number;
  seqOrder: number;
}

export type ExcelColumn = {
  letter: string;
  field: BaseIndexFieldModel;
};

export const generateIndexRowArray = (excelColumns: ExcelColumn[], generateSampleData: boolean, code = 'code') => {
  return (indexRows: FormIndexData[]): any[][] => {
    const indexArray = indexRows.map((indexRow: FormIndexData) => {
      const valueArr: any[] = [];
      excelColumns.map((column) => {
        let value: any = getIndexFieldValue(column.field, indexRow, false);
        if (column.field.dataType === DataType.DATE) {
          // overwrite type definition when writing to excel (will be as string)
          value = format(value, 'dd/MM/yyyy') as any;
        }

        valueArr.push(value);
      });

      return valueArr;
    });

    if (generateSampleData) {
      const dataArray: any[] = [];

      excelColumns.map((column) => {
        const value = getSampleIndexData(column.field, code);
        dataArray.push(value);
      });

      indexArray.push(dataArray);
    }

    return indexArray;
  };
};

export const getSampleIndexData = (field: BaseIndexFieldModel, code = 'code') => {
  let value: any;
  if (field.label === 'Attachment Filename') {
    value = 'file.pdf';
  } else if (field.label === 'Attachment Password') {
    value = '123';
  } else if (field.label === 'Code') {
    value = code;
  } else {
    switch (field.dataType) {
      case DataType.DATE:
        value = '13/05/2022';
        break;
      case DataType.NUMBER:
        value = 100;
        break;
      case DataType.EMAIL:
        value = 'test@mybillone.com';
        break;
      case DataType.FILENAME:
        value = 'file.pdf';
        break;
      default:
        value = 'sample';
    }
  }

  return value;
}

export const createWorkbook = (excelColumns: ExcelColumn[]) => {
  return (dataArr: any[][]) =>
    new Promise<any>((resolve) => {
      XlsxPopulate.fromBlankAsync().then((workbook: any) => {
        // modify the workbook
        const data = [excelColumns.map((column) => column.field.header), ...dataArr];

        const worksheet = workbook.sheet('Sheet1');
        excelColumns.map((column) => {
          worksheet.column(column.letter).width(20);
          worksheet.cell(`${column.letter}1`).style('fill', 'e3f2fd');
        });
        worksheet.row(1).style('bold', true);

        // load header array
        worksheet.cell('A1').value(data);

        return resolve(workbook);
      });
    });
};

export const downloadWorkbook = (filename: string = EXPORT_FILENAME) => {
  return (workbook: any) => {
    return workbook.outputAsync().then((blob: any) => {
      const fileName = `${filename}-${format(new Date(), 'yyyyMMdd')}.xlsx`;
      openBlob(blob, fileName);
    });
  };
};
