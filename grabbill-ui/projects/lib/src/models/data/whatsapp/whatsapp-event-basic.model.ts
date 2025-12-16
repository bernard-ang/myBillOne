

export interface WhatsappEventBasicModel {
  id: number;
  eventId: string;
  whatsappMessageId: string;
  type: string;
  messageType: string;
  status: string;
  content: string;
  processed: boolean;
  processedCount: number;
  eventTimestamp: Date;
  mobileNo: string;
}
