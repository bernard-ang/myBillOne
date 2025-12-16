import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { Params } from '@angular/router';

@Component({
  selector: 'grabbill-client-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BreadcrumbComponent {
  @Input()
  rootTitle!: string;

  @Input()
  rootLink!: any[];

  @Input()
  rootQueryParam?: Params;

  @Input()
  hasCrumb2 = false;

  @Input()
  crumb2Object?: { name: string } | null;

  @Input()
  crumb2Link?: any[];

  @Input()
  crumb2QueryParam?: Params;

  @Input()
  hasCrumb3 = false;

  @Input()
  crumb3Object?: { name: string } | null;

  @Input()
  crumb3Link?: any[];

  @Input()
  crumb3QueryParam?: Params;
}
