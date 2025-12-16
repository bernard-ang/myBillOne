import { BaseIndexFieldModel, ContactFieldModel, DataType } from '@grabbill/lib';
import { map, Observable } from 'rxjs';

export const observeContactFields = (
  contactFields$: Observable<ContactFieldModel[]>
): Observable<BaseIndexFieldModel[]> => {
  return contactFields$.pipe(
    map((fields) => {
      if (fields.length > 0) {
        return [
          {
            id: 1000,
            label: 'Email',
            header: 'email',
            applicable: true,
            dataType: DataType.EMAIL,
            seqOrder: -1,
            referenced: true,
            required: true,
          },
          {
            id: 1000,
            label: 'Mobile No',
            header: 'mobileNo',
            applicable: true,
            dataType: DataType.TEXT,
            seqOrder: -1,
            referenced: true,
            required: true,
          },
          ...fields.map((field) => ({
            id: field.id,
            label: field.label,
            header: field.name,
            applicable: true,
            dataType: field.dataType,
            seqOrder: field.seqOrder,
            referenced: field.referenced,
            required: field.required,
          })),
        ];
      }
      return [];
    })
  );
};
