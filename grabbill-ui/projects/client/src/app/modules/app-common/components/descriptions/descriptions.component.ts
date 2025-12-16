import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'grabbill-client-descriptions',
  templateUrl: './descriptions.component.html',
  styleUrls: ['./descriptions.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DescriptionsComponent {
  @Input()
  title!: string;

  @Input()
  items!: {
    title: string;
    span: number;
    value: any;
    type: 'boolean' | 'string' | 'date' | 'number' | 'html' | 'tag' | 'pre' | 'files';
    object?: any;
    typeFn?: (item: any, ori?: any) => any;
  }[];

  @Input()
  containerClass = 'pb-8';

  getDateFormat() {
    return environment.config.dateFormat;
  }
}
