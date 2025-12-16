import { DataType } from '@grabbill/lib';

export const emailContactField = {
  id: 1,
  name: 'email',
  label: 'Email',
  dataType: DataType.EMAIL,
  required: true,
  seqOrder: -2,
  referenced: false,
};

export const defaultContactFields = [
  emailContactField,
  {
    id: 2,
    name: 'mobileNo',
    label: 'Mobile No',
    dataType: DataType.TEXT,
    required: false,
    seqOrder: -1,
    referenced: false,
  },
];
