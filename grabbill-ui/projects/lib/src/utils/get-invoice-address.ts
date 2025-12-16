import { InvoiceModel } from '../models';

export const getInvoiceAddress = (invoice: InvoiceModel) => {
  let address = '';
  if (invoice.billToAddrLine1) {
    address += invoice.billToAddrLine1 + '\n';
  }
  if (invoice.billToAddrLine2) {
    address += invoice.billToAddrLine2 + '\n';
  }
  if (invoice.billToCity) {
    address += invoice.billToCity + '\n';
  }
  if (invoice.billToPostcode) {
    address += invoice.billToPostcode + '\n';
  }
  if (invoice.billToState) {
    address += invoice.billToState + '\n';
  }
  if (invoice.billToCountry) {
    address += invoice.billToCountry + '\n';
  }

  return address;
};
