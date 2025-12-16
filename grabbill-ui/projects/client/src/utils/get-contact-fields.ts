import { ContactFieldModel, ContactGroupBasicModel, ContactGroupModel, ContactModel, DataType } from '@grabbill/lib';
import { FormField } from '../app/modules/app-common/components/drawer-form/drawer-form.component';
import { Validators } from '@angular/forms';

export const getContactFields = (
  contactFields: ContactFieldModel[],
  contact?: ContactModel
) => {
  const drawerFields: FormField[] = [];

  drawerFields.push({
    label: 'Email',
    name: `email`,
    type: DataType.TEXT,
    required: true,
    validators: [Validators.email, Validators.maxLength(255)],
    value: contact?.email,
  });

  drawerFields.push({
    label: 'Mobile No',
    name: `mobileNo`,
    type: DataType.TEXT,
    required: false,
    validators: [Validators.maxLength(255)],
    value: contact?.mobileNo,
  });

  for (const field of contactFields) {
    switch (field.dataType) {
      case DataType.NUMBER:
        const numberName = `number${field.seqOrder}`;
        drawerFields.push({
          label: field.label,
          name: numberName,
          type: DataType.NUMBER,
          required: field.required,
          validators: [Validators.max(99999999999)],
          value: contact ? (contact as any)[numberName] : undefined,
        });
        break;
      case DataType.DATE:
        const dateName = `date${field.seqOrder}`;
        drawerFields.push({
          label: field.label,
          name: dateName,
          type: DataType.DATE,
          required: field.required,
          value: contact ? (contact as any)[dateName] : undefined,
        });
        break;
      default:
        const textName = `text${field.seqOrder}`;
        drawerFields.push({
          label: field.label,
          name: textName,
          type: DataType.TEXT,
          required: field.required,
          validators: [Validators.maxLength(255)],
          value: contact ? (contact as any)[textName] : undefined,
        });
        break;
    }
  }

  return drawerFields;
};

export const getContactFormDrawerFields = (
  contactFields: ContactFieldModel[],
  contact?: ContactModel,
  contactGroups: ContactGroupBasicModel[] = []
) => {
  const drawerFields: FormField[] = getContactFields(contactFields, contact);

  drawerFields.push({
    label: 'Groups',
    name: `groups`,
    type: DataType.MULTIPLE,
    required: false,
    value: contact?.groups || [],
    options: contactGroups.map((group) => ({
      label: group.name,
      value: group.id,
    })),
  });

  return drawerFields;
};
