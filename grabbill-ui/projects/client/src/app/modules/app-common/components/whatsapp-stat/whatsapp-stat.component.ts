import { Component, OnInit, ChangeDetectionStrategy, Input } from "@angular/core";

@Component({
  selector: 'grabbill-client-whatsapp-stat',
  templateUrl: './whatsapp-stat.component.html',
  styleUrls: ['./whatsapp-stat.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WhatsappStatComponent {

  @Input()
  total: number = 0;

  @Input()
  totalSent: number = 0;

  @Input()
  totalSkip: number = 0;

  @Input()
  totalDelivered: number = 0;

  @Input()
  totalRead: number = 0;

  @Input()
  totalAcknowledge: number = 0;

  @Input()
  totalFailed: number = 0;
}
