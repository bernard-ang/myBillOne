import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';

@Component({
  selector: 'grabbill-client-sms-stat',
  templateUrl: './sms-stat.component.html',
  styleUrls: ['./sms-stat.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SmsStatComponent {
  @Input()
  totalSms!: number;

  @Input()
  smsSent!: number;

  @Input()
  smsError!: number;

  @Input()
  totalCreditUsed!: number;
}
