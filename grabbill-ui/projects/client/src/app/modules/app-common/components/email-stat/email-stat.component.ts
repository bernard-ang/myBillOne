import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'grabbill-client-email-stat',
  templateUrl: './email-stat.component.html',
  styleUrls: ['./email-stat.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmailStatComponent {
  @Input()
  emailTotal: number = 0;

  @Input()
  emailSent: number = 0;

  @Input()
  emailOpened: number = 0;

  @Input()
  emailBounced: number = 0;

  @Input()
  emailUnsubscribed: number = 0;

  @Input()
  emailSkip: number = 0;
}
